"""向量存储 — ChromaDB 封装，用于知识库 RAG 检索"""

from __future__ import annotations

from typing import Optional

import chromadb
from chromadb.config import Settings as ChromaSettings

from app.config import settings
from app.embedder import Embedder


class VectorStore:
    """向量数据库，管理知识库文档的嵌入和检索"""

    def __init__(self, embedder: Embedder):
        self.embedder = embedder
        self._client = chromadb.PersistentClient(
            path=settings.chroma_persist_dir,
            settings=ChromaSettings(anonymized_telemetry=False),
        )

    def _collection_name(self, kb_id: int) -> str:
        return f"kb_{kb_id}"

    def _get_collection(self, kb_id: int):
        return self._client.get_or_create_collection(
            name=self._collection_name(kb_id),
            metadata={"hnsw:space": "cosine"},
        )

    def add_documents(self, kb_id: int, docs: list[dict]):
        """
        添加文档块到知识库向量集合。
        docs: [{id, text, metadata}]
        """
        if not docs:
            return

        texts = [d["text"] for d in docs]
        ids = [str(d["id"]) for d in docs]
        metadatas = [d.get("metadata", {}) for d in docs]

        # 生成嵌入向量
        import asyncio
        embeddings = asyncio.run(self.embedder.embed_batch(texts))

        collection = self._get_collection(kb_id)
        collection.add(
            ids=ids,
            embeddings=embeddings,
            documents=texts,
            metadatas=metadatas,
        )

    def search(self, kb_id: int, query: str, top_k: int = 5) -> list[dict]:
        """
        语义检索知识库，返回最相关的文档块。
        """
        import asyncio
        query_embedding = asyncio.run(self.embedder.embed(query))

        collection = self._get_collection(kb_id)
        results = collection.query(
            query_embeddings=[query_embedding],
            n_results=min(top_k, 20),
        )

        hits = []
        if results["documents"] and results["documents"][0]:
            for i in range(len(results["documents"][0])):
                hits.append({
                    "text": results["documents"][0][i],
                    "metadata": (results["metadatas"][0][i] if results["metadatas"] else {}),
                    "score": (1 - results["distances"][0][i]) if results["distances"] else 0,
                })
        return hits

    def search_all_knowledge_bases(self, kb_ids: list[int], query: str, top_k: int = 3) -> list[dict]:
        """跨多个知识库检索"""
        all_hits = []
        for kb_id in kb_ids:
            hits = self.search(kb_id, query, top_k)
            all_hits.extend(hits)
        # 按 score 降序排列
        all_hits.sort(key=lambda x: x["score"], reverse=True)
        return all_hits[:top_k]

    def delete_documents(self, kb_id: int, doc_ids: list[str]):
        """删除知识库中的文档块"""
        collection = self._get_collection(kb_id)
        collection.delete(ids=doc_ids)

    def delete_collection(self, kb_id: int):
        """删除整个知识库向量集合"""
        try:
            self._client.delete_collection(self._collection_name(kb_id))
        except ValueError:
            pass


# 全局单例（需先初始化 embedder）
vector_store: Optional[VectorStore] = None


def init_vector_store(embedder: Embedder):
    global vector_store
    vector_store = VectorStore(embedder)
    return vector_store


def get_vector_store() -> VectorStore:
    assert vector_store is not None, "VectorStore 未初始化"
    return vector_store
