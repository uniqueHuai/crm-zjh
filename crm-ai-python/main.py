"""CRM AI 服务 — FastAPI 入口"""

from __future__ import annotations

from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.embedder import Embedder
from app.llm_client import LlmClient
from app.rag.vector_store import init_vector_store


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动：初始化 LLM、嵌入、向量库、注册工具
    llm = LlmClient()
    embedder = Embedder()
    init_vector_store(embedder)

    # 初始化聊天路由（注册工具 + 预置示例）
    from app.routers.chat import init_chat_router
    init_chat_router(llm)

    # 确保 knowledge base 预置数据
    from app.routers.knowledge import _ensure_seeded
    _ensure_seeded()

    app.state.llm = llm
    app.state.embedder = embedder

    print(f"🤖 CRM AI Service started on port {settings.ai_service_port}")
    print(f"   Model: {settings.siliconflow_model}")
    print(f"   Embedding: {settings.embedding_model}")
    print(f"   CRM API: {settings.crm_api_base_url}")

    yield

    # 关闭
    from app.tools.crm_api import crm_api
    await crm_api.close()


app = FastAPI(
    title="CRM AI Service",
    description="CRM 智能AI服务 — 销售助手 / 智能管家 / 智能客服",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS — 允许 Java 后端和前端跨域访问
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
from app.routers import chat, knowledge
app.include_router(chat.router, prefix="/api/v1")
app.include_router(knowledge.router, prefix="/api/v1")


@app.get("/health")
async def health():
    return {"status": "ok", "service": "crm-ai"}


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=settings.ai_service_port,
        reload=True,
    )
