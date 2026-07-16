"""嵌入服务 — 通过 SiliconFlow Embeddings API 生成文本向量"""

from __future__ import annotations

from openai import AsyncOpenAI

from app.config import settings


class Embedder:
    """文本嵌入服务，支持单条和批量"""

    def __init__(self):
        self.client = AsyncOpenAI(
            api_key=settings.siliconflow_api_key,
            base_url=settings.siliconflow_base_url,
        )
        self.model = settings.embedding_model
        self.dim = settings.embedding_dim

    async def embed(self, text: str) -> list[float]:
        """单条文本嵌入"""
        resp = await self.client.embeddings.create(
            model=self.model,
            input=text,
        )
        return resp.data[0].embedding

    async def embed_batch(self, texts: list[str]) -> list[list[float]]:
        """批量文本嵌入"""
        resp = await self.client.embeddings.create(
            model=self.model,
            input=texts,
        )
        # 按输入顺序排列
        indexed = [(r.index, r.embedding) for r in resp.data]
        indexed.sort(key=lambda x: x[0])
        return [e for _, e in indexed]
