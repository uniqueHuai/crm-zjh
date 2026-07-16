"""订单查询工具 — 供客服 Agent 调用"""

from __future__ import annotations

from app.tools.crm_api import crm_api

GET_ORDER_TOOL = {
    "type": "function",
    "function": {
        "name": "get_order_info",
        "description": "查询订单详情，包括订单状态、金额、商品列表等",
        "parameters": {
            "type": "object",
            "properties": {
                "order_id": {"type": "integer", "description": "订单ID"},
            },
            "required": ["order_id"],
        },
    },
}


async def get_order_info(order_id: int) -> str:
    order = await crm_api.get_order(order_id)
    if not order:
        return f"未找到订单 (ID: {order_id})"
    data = order.get("data", order)
    status_map = {
        "pending_payment": "待支付",
        "paid": "已支付",
        "shipped": "已发货",
        "delivered": "已签收",
        "completed": "已完成",
        "cancelled": "已取消",
        "refunding": "退款中",
        "refunded": "已退款",
    }
    status = status_map.get(data.get("status", ""), data.get("status", "未知"))
    parts = [
        f"订单号: {data.get('orderNo', data.get('id', ''))}",
        f"状态: {status}",
        f"金额: ¥{data.get('totalAmount', 0):,.2f}",
        f"收货人: {data.get('receiverName', '')}",
        f"联系电话: {data.get('receiverPhone', '')}",
        f"收货地址: {data.get('receiverAddress', '')}",
        f"下单时间: {data.get('createdAt', '')}",
    ]
    return "\n".join(parts)


GET_CUSTOMER_ORDERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_customer_orders",
        "description": "查询客户的最近订单列表",
        "parameters": {
            "type": "object",
            "properties": {
                "customer_id": {"type": "integer", "description": "客户ID"},
                "limit": {"type": "integer", "description": "返回条数（默认5）"},
            },
            "required": ["customer_id"],
        },
    },
}


async def get_customer_orders(customer_id: int, limit: int = 5) -> str:
    orders = await crm_api.get_customer_orders(customer_id, limit)
    if not orders:
        return "该客户暂无订单"
    status_map = {
        "pending_payment": "待支付",
        "paid": "已支付",
        "shipped": "已发货",
        "delivered": "已签收",
        "completed": "已完成",
        "cancelled": "已取消",
    }
    lines = [f"该客户最近 {len(orders)} 笔订单："]
    for o in orders:
        status = status_map.get(o.get("status", ""), o.get("status", ""))
        lines.append(f"  - #{o.get('orderNo', o.get('id'))} | ¥{o.get('totalAmount', 0):,.2f} | {status} | {o.get('createdAt', '')}")
    return "\n".join(lines)
