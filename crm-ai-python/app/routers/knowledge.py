"""知识库管理路由 — 支持 CRUD 和文档导入/向量化"""

from __future__ import annotations

import os
import uuid
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, File, HTTPException, UploadFile
from fastapi.responses import JSONResponse

from app.config import settings
from app.rag.document_processor import chunk_text, extract_text_from_file
from app.rag.vector_store import get_vector_store
from app.schemas import R

router = APIRouter(prefix="/ai/knowledge-base", tags=["知识库"])

# 内存存储（生产环境应使用数据库）
_knowledge_bases: dict[int, dict] = {}
_documents: dict[int, list[dict]] = {}  # kb_id → documents
_next_kb_id = 1
_next_doc_id = 1

# 预置示例知识库
_seeded = False


def _ensure_seeded():
    global _seeded, _next_kb_id, _next_doc_id
    if _seeded:
        return
    _seeded = True

    # 创建示例知识库
    for name, desc, kb_type, status in [
        ("产品知识库", "公司产品介绍、规格参数、使用说明", "public", "enabled"),
        ("销售话术库", "常见销售场景话术、客户问答模板", "public", "enabled"),
        ("商城FAQ", "小程序商城常见问题、退换货政策、物流说明", "public", "enabled"),
    ]:
        kb_id = _next_kb_id
        _next_kb_id += 1
        now = datetime.now()
        _knowledge_bases[kb_id] = {
            "id": kb_id,
            "name": name,
            "description": desc,
            "type": kb_type,
            "status": status,
            "created_at": now,
            "updated_at": now,
        }
        _documents[kb_id] = []


@router.get("", response_model=R)
async def list_knowledge_bases(page: int = 1, size: int = 10, keywords: str = ""):
    """分页获取知识库列表"""
    _ensure_seeded()
    items = list(_knowledge_bases.values())
    if keywords:
        items = [kb for kb in items if keywords.lower() in kb["name"].lower() or keywords.lower() in kb["description"].lower()]

    total = len(items)
    start = (page - 1) * size
    end = start + size
    page_items = items[start:end]

    return R(data={
        "records": [
            {
                "id": kb["id"],
                "name": kb["name"],
                "description": kb["description"],
                "type": kb["type"],
                "status": kb["status"],
                "createdAt": kb["created_at"].isoformat(),
                "updatedAt": kb["updated_at"].isoformat(),
            }
            for kb in page_items
        ],
        "total": total,
        "page": page,
        "size": size,
    })


@router.get("/enabled", response_model=R)
async def get_enabled_knowledge_bases():
    """获取已启用的知识库列表（供 RAG 检索使用）"""
    _ensure_seeded()
    items = [
        {
            "id": kb["id"],
            "name": kb["name"],
            "description": kb["description"],
        }
        for kb in _knowledge_bases.values()
        if kb["status"] == "enabled"
    ]
    return R(data=items)


@router.get("/{kb_id}", response_model=R)
async def get_knowledge_base(kb_id: int):
    kb = _knowledge_bases.get(kb_id)
    if not kb:
        raise HTTPException(status_code=404, detail="知识库不存在")
    return R(data={
        "id": kb["id"],
        "name": kb["name"],
        "description": kb["description"],
        "type": kb["type"],
        "status": kb["status"],
        "createdAt": kb["created_at"].isoformat(),
        "updatedAt": kb["updated_at"].isoformat(),
    })


@router.post("", response_model=R)
async def create_knowledge_base(body: dict):
    global _next_kb_id
    _ensure_seeded()
    kb_id = _next_kb_id
    _next_kb_id += 1
    now = datetime.now()
    _knowledge_bases[kb_id] = {
        "id": kb_id,
        "name": body["name"],
        "description": body.get("description", ""),
        "type": body.get("type", "public"),
        "status": "enabled",
        "created_at": now,
        "updated_at": now,
    }
    _documents[kb_id] = []
    return R()


@router.put("", response_model=R)
async def update_knowledge_base(body: dict):
    kb_id = body.get("id")
    kb = _knowledge_bases.get(kb_id)
    if not kb:
        raise HTTPException(status_code=404, detail="知识库不存在")
    if "name" in body:
        kb["name"] = body["name"]
    if "description" in body:
        kb["description"] = body["description"]
    if "status" in body:
        kb["status"] = body["status"]
    kb["updated_at"] = datetime.now()
    return R()


@router.delete("/{kb_id}", response_model=R)
async def delete_knowledge_base(kb_id: int):
    if kb_id not in _knowledge_bases:
        raise HTTPException(status_code=404, detail="知识库不存在")
    del _knowledge_bases[kb_id]
    if kb_id in _documents:
        del _documents[kb_id]
    # 删除向量集合
    try:
        get_vector_store().delete_collection(kb_id)
    except Exception:
        pass
    return R()


@router.post("/{kb_id}/import", response_model=R)
async def import_document(kb_id: int, body: dict):
    """导入文本文档到知识库并进行向量化"""
    kb = _knowledge_bases.get(kb_id)
    if not kb:
        raise HTTPException(status_code=404, detail="知识库不存在")

    title = body.get("title", "")
    content = body.get("content", "")

    if not content.strip():
        raise HTTPException(status_code=400, detail="文档内容不能为空")

    _save_and_vectorize(kb_id, title, content)
    return R(message="导入成功，文档已向量化")


@router.post("/{kb_id}/import-file", response_model=R)
async def import_file(kb_id: int, file: UploadFile = File(...)):
    """上传文件导入知识库"""
    kb = _knowledge_bases.get(kb_id)
    if not kb:
        raise HTTPException(status_code=404, detail="知识库不存在")

    # 保存临时文件并提取文本
    os.makedirs("uploads", exist_ok=True)
    ext = os.path.splitext(file.filename or ".txt")[1] or ".txt"
    temp_path = f"uploads/{uuid.uuid4().hex}{ext}"
    try:
        content = await file.read()
        with open(temp_path, "wb") as f:
            f.write(content)

        text = extract_text_from_file(temp_path)
        title = (file.filename or "未命名文件").rsplit(".", 1)[0]

        if not text.strip():
            return R(code=400, message="无法从文件中提取文本内容")

        _save_and_vectorize(kb_id, title, text)
        return R(message="导入成功，文档已向量化")
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)


def _save_and_vectorize(kb_id: int, title: str, content: str):
    """保存文档、分块、向量化"""
    global _next_doc_id

    if kb_id not in _documents:
        _documents[kb_id] = []

    # 分块
    chunks = chunk_text(content, chunk_size=512, overlap=64)

    # 保存文档索引
    doc_id = _next_doc_id
    _next_doc_id += 1
    _documents[kb_id].append({
        "id": doc_id,
        "title": title,
        "chunk_count": len(chunks),
        "created_at": datetime.now(),
    })

    # 向量化
    vs = get_vector_store()
    docs = [
        {
            "id": f"{doc_id}_{i}",
            "text": chunk,
            "metadata": {
                "doc_id": doc_id,
                "title": title,
                "chunk_index": i,
            },
        }
        for i, chunk in enumerate(chunks)
    ]
    vs.add_documents(kb_id, docs)

    _knowledge_bases[kb_id]["updated_at"] = datetime.now()
