"""销售数据查询工具 — 供 Agent 调用"""

from __future__ import annotations

from app.tools.crm_api import crm_api

GET_SALES_SUMMARY_TOOL = {
    "type": "function",
    "function": {
        "name": "get_sales_summary",
        "description": "获取销售汇总数据，包括销售额、订单数、回款等",
        "parameters": {
            "type": "object",
            "properties": {
                "period": {
                    "type": "string",
                    "enum": ["today", "week", "month", "quarter", "year"],
                    "description": "统计周期（默认月）",
                },
            },
        },
    },
}


async def get_sales_summary(period: str = "month") -> str:
    data = await crm_api.get_sales_summary(period)
    if not data:
        return "暂无销售数据"
    d = data.get("data", data)
    parts = [
        f"📊 销售汇总（{period}）",
        f"总销售额: ¥{d.get('totalAmount', 0):,.2f}",
        f"订单数: {d.get('orderCount', 0)}",
        f"成交客户数: {d.get('customerCount', 0)}",
        f"回款金额: ¥{d.get('receivedAmount', 0):,.2f}",
        f"待回款: ¥{d.get('pendingAmount', 0):,.2f}",
    ]
    return "\n".join(parts)


GET_SALES_TREND_TOOL = {
    "type": "function",
    "function": {
        "name": "get_sales_trend",
        "description": "获取销售趋势数据，按天统计",
        "parameters": {
            "type": "object",
            "properties": {
                "days": {"type": "integer", "description": "最近天数（默认30）"},
            },
        },
    },
}


async def get_sales_trend(days: int = 30) -> str:
    trend = await crm_api.get_sales_trend(days)
    if not trend:
        return "暂无趋势数据"
    total = sum(t.get("amount", 0) for t in trend)
    count = sum(t.get("count", 0) for t in trend)
    avg = total / len(trend) if trend else 0
    return (
        f"📈 最近 {days} 天销售趋势\n"
        f"总销售额: ¥{total:,.2f}\n"
        f"总订单数: {count}\n"
        f"日均销售额: ¥{avg:,.2f}\n"
        f"最近一天: ¥{trend[-1].get('amount', 0):,.2f}（{trend[-1].get('date', '')}）"
    )


GET_TOP_PRODUCTS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_top_products",
        "description": "获取热销产品排行榜",
        "parameters": {
            "type": "object",
            "properties": {
                "limit": {"type": "integer", "description": "返回条数（默认10）"},
            },
        },
    },
}


async def get_top_products(limit: int = 10) -> str:
    products = await crm_api.get_top_products(limit)
    if not products:
        return "暂无排行数据"
    lines = [f"🏆 热销产品 TOP {len(products)}"]
    for i, p in enumerate(products, 1):
        lines.append(f"  {i}. {p.get('name')} | 销售额 ¥{p.get('amount', 0):,.2f} | {p.get('count', 0)} 单")
    return "\n".join(lines)


GET_TOP_CUSTOMERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_top_customers",
        "description": "获取重要客户排行榜（按消费额）",
        "parameters": {
            "type": "object",
            "properties": {
                "limit": {"type": "integer", "description": "返回条数（默认10）"},
            },
        },
    },
}


async def get_top_customers(limit: int = 10) -> str:
    customers = await crm_api.get_top_customers(limit)
    if not customers:
        return "暂无排行数据"
    lines = [f"🏆 重要客户 TOP {len(customers)}"]
    for i, c in enumerate(customers, 1):
        lines.append(f"  {i}. {c.get('name')} | 消费 ¥{c.get('amount', 0):,.2f} | {c.get('orderCount', 0)} 单")
    return "\n".join(lines)
