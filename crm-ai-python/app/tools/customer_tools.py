"""客户查询工具 — 供 Agent 调用"""

from __future__ import annotations

from app.tools.crm_api import crm_api

TOOL_DEFINITION = {
    "type": "function",
    "function": {
        "name": "get_customer_info",
        "description": "查询客户详细信息，包括客户名称、联系方式、等级、消费总额等",
        "parameters": {
            "type": "object",
            "properties": {
                "customer_id": {"type": "integer", "description": "客户ID"},
            },
            "required": ["customer_id"],
        },
    },
}


async def get_customer_info(customer_id: int) -> str:
    """获取客户详情"""
    customer = await crm_api.get_customer(customer_id)
    if not customer:
        return f"未找到客户 (ID: {customer_id})"
    data = customer.get("data", customer)
    parts = [
        f"客户名称: {data.get('name', '未知')}",
        f"手机号: {data.get('phone', '未填写')}",
        f"等级: {data.get('levelName') or data.get('levelId', '未分级')}",
        f"累计消费: ¥{data.get('totalConsumption', 0):,.2f}",
        f"订单数: {data.get('orderCount', 0)}",
    ]
    return "\n".join(parts)


# ── 搜索客户 ──

SEARCH_CUSTOMER_TOOL = {
    "type": "function",
    "function": {
        "name": "search_customers",
        "description": "按关键词搜索客户",
        "parameters": {
            "type": "object",
            "properties": {
                "keyword": {"type": "string", "description": "客户名称/手机号关键词"},
                "limit": {"type": "integer", "description": "返回条数（默认10）"},
            },
            "required": ["keyword"],
        },
    },
}


async def search_customers(keyword: str, limit: int = 10) -> str:
    customers = await crm_api.search_customers(keyword, limit)
    if not customers:
        return f"未找到匹配「{keyword}」的客户"
    lines = [f"找到 {len(customers)} 个客户："]
    for c in customers:
        lines.append(f"  - {c.get('name')} | {c.get('phone', '')} | 消费 ¥{c.get('totalConsumption', 0):,.2f}")
    return "\n".join(lines)
