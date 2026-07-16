"""CRM Java 后端 API 客户端 — 通过 HTTP 查询业务数据"""

from __future__ import annotations

from typing import Any, Optional

import httpx

from app.config import settings


class CrmApiClient:
    """与 Java 后端通信，获取客户/产品/销售等业务数据"""

    def __init__(self):
        self.base_url = settings.crm_api_base_url
        self._client = httpx.AsyncClient(timeout=15.0)

    async def _get(self, path: str, params: Optional[dict] = None) -> dict:
        url = f"{self.base_url}{path}"
        resp = await self._client.get(url, params=params)
        resp.raise_for_status()
        return resp.json()

    async def _post(self, path: str, data: Optional[dict] = None) -> dict:
        url = f"{self.base_url}{path}"
        resp = await self._client.post(url, json=data)
        resp.raise_for_status()
        return resp.json()

    # ── 客户 ──

    async def get_customer(self, customer_id: int) -> Optional[dict]:
        """获取客户详情"""
        try:
            return await self._get(f"/customer/{customer_id}")
        except Exception:
            return None

    async def search_customers(self, keyword: str, limit: int = 10) -> list[dict]:
        """搜索客户"""
        try:
            result = await self._get("/customer/list", {"keywords": keyword, "page": 1, "size": limit})
            return (result.get("data") or {}).get("records") or []
        except Exception:
            return []

    async def get_customer_contacts(self, customer_id: int) -> list[dict]:
        """获取客户联系人"""
        try:
            result = await self._get(f"/customer/{customer_id}/contacts")
            return result.get("data") or []
        except Exception:
            return []

    async def get_customer_deals(self, customer_id: int) -> list[dict]:
        """获取客户成交记录"""
        try:
            result = await self._get(f"/customer/{customer_id}/deals")
            return result.get("data") or []
        except Exception:
            return []

    # ── 产品 ──

    async def get_product(self, product_id: int) -> Optional[dict]:
        """获取产品详情"""
        try:
            return await self._get(f"/sales/product/{product_id}")
        except Exception:
            return None

    async def search_products(self, keyword: str, limit: int = 10) -> list[dict]:
        """搜索产品"""
        try:
            result = await self._get("/sales/product/list", {"keywords": keyword, "page": 1, "size": limit})
            return (result.get("data") or {}).get("records") or []
        except Exception:
            return []

    # ── 销售 ──

    async def get_sales_summary(self, period: str = "month") -> dict:
        """获取销售汇总"""
        try:
            return await self._get("/report/dashboard/summary", {"period": period})
        except Exception:
            return {}

    async def get_sales_trend(self, days: int = 30) -> list[dict]:
        """获取销售趋势"""
        try:
            result = await self._get("/report/dashboard/trend", {"days": days})
            return result.get("data") or []
        except Exception:
            return []

    async def get_top_products(self, limit: int = 10) -> list[dict]:
        """热销产品排行"""
        try:
            result = await self._get("/report/dashboard/top-products", {"limit": limit})
            return result.get("data") or []
        except Exception:
            return []

    async def get_top_customers(self, limit: int = 10) -> list[dict]:
        """重要客户排行"""
        try:
            result = await self._get("/report/dashboard/top-customers", {"limit": limit})
            return result.get("data") or []
        except Exception:
            return []

    # ── 订单（商城） ──

    async def get_order(self, order_id: int) -> Optional[dict]:
        """获取订单详情"""
        try:
            return await self._get(f"/mall/order/{order_id}")
        except Exception:
            return None

    async def get_customer_orders(self, customer_id: int, limit: int = 10) -> list[dict]:
        """获取客户订单列表"""
        try:
            result = await self._get("/mall/order/list", {"customerId": customer_id, "page": 1, "size": limit})
            return (result.get("data") or {}).get("records") or []
        except Exception:
            return []

    async def close(self):
        await self._client.aclose()


# 全局单例
crm_api = CrmApiClient()
