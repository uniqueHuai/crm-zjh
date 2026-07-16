"""产品查询工具 — 供 Agent 调用"""

from __future__ import annotations

from app.tools.crm_api import crm_api

GET_PRODUCT_TOOL = {
    "type": "function",
    "function": {
        "name": "get_product_info",
        "description": "查询产品详情，包括名称、价格、库存、描述等",
        "parameters": {
            "type": "object",
            "properties": {
                "product_id": {"type": "integer", "description": "产品ID"},
            },
            "required": ["product_id"],
        },
    },
}


async def get_product_info(product_id: int) -> str:
    product = await crm_api.get_product(product_id)
    if not product:
        return f"未找到产品 (ID: {product_id})"
    data = product.get("data", product)
    parts = [
        f"产品名称: {data.get('name', '未知')}",
        f"价格: ¥{data.get('price', 0):,.2f}",
        f"库存: {data.get('stock', 0)}",
        f"分类: {data.get('categoryName', '未分类')}",
        f"描述: {data.get('description', '暂无描述')}",
    ]
    return "\n".join(parts)


SEARCH_PRODUCT_TOOL = {
    "type": "function",
    "function": {
        "name": "search_products",
        "description": "按关键词搜索产品",
        "parameters": {
            "type": "object",
            "properties": {
                "keyword": {"type": "string", "description": "产品名称关键词"},
                "limit": {"type": "integer", "description": "返回条数（默认10）"},
            },
            "required": ["keyword"],
        },
    },
}


async def search_products(keyword: str, limit: int = 10) -> str:
    products = await crm_api.search_products(keyword, limit)
    if not products:
        return f"未找到匹配「{keyword}」的产品"
    lines = [f"找到 {len(products)} 个产品："]
    for p in products:
        lines.append(f"  - {p.get('name')} | ¥{p.get('price', 0):,.2f} | 库存 {p.get('stock', 0)}")
    return "\n".join(lines)
