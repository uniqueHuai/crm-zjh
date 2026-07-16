"""工具注册表 — 集中管理所有 Agent 可用的工具"""

from __future__ import annotations

from typing import Any, Callable, Coroutine

# 工具注册结构
ToolFunc = Callable[..., Coroutine[Any, Any, str]]


class ToolRegistry:
    """工具注册表，维护 name → (definition, handler) 映射"""

    def __init__(self):
        self._tools: dict[str, tuple[dict, ToolFunc]] = {}

    def register(self, definition: dict, handler: ToolFunc):
        name = definition["function"]["name"]
        self._tools[name] = (definition, handler)

    def get_definitions(self) -> list[dict]:
        return [defn for defn, _ in self._tools.values()]

    def get_handler(self, name: str) -> ToolFunc | None:
        pair = self._tools.get(name)
        return pair[1] if pair else None

    async def execute(self, name: str, arguments: dict) -> str:
        handler = self.get_handler(name)
        if not handler:
            return f"错误：未知工具 {name}"
        try:
            return await handler(**arguments)
        except Exception as e:
            return f"工具调用失败 ({name}): {e!s}"


# 全局工具注册表
registry = ToolRegistry()


def register_tools():
    """注册所有工具到全局注册表"""
    from app.tools.customer_tools import (
        TOOL_DEFINITION as GET_CUSTOMER_INFO,
        SEARCH_CUSTOMER_TOOL,
        get_customer_info,
        search_customers,
    )
    from app.tools.product_tools import (
        GET_PRODUCT_TOOL,
        SEARCH_PRODUCT_TOOL,
        get_product_info,
        search_products,
    )
    from app.tools.sales_tools import (
        GET_SALES_SUMMARY_TOOL,
        GET_SALES_TREND_TOOL,
        GET_TOP_PRODUCTS_TOOL,
        GET_TOP_CUSTOMERS_TOOL,
        get_sales_summary,
        get_sales_trend,
        get_top_products,
        get_top_customers,
    )
    from app.tools.order_tools import (
        GET_ORDER_TOOL,
        GET_CUSTOMER_ORDERS_TOOL,
        get_order_info,
        get_customer_orders,
    )

    # 员工助手工具
    registry.register(GET_CUSTOMER_INFO, get_customer_info)
    registry.register(SEARCH_CUSTOMER_TOOL, search_customers)
    registry.register(GET_PRODUCT_TOOL, get_product_info)
    registry.register(SEARCH_PRODUCT_TOOL, search_products)

    # 领导助手工具
    registry.register(GET_SALES_SUMMARY_TOOL, get_sales_summary)
    registry.register(GET_SALES_TREND_TOOL, get_sales_trend)
    registry.register(GET_TOP_PRODUCTS_TOOL, get_top_products)
    registry.register(GET_TOP_CUSTOMERS_TOOL, get_top_customers)

    # 客服工具
    registry.register(GET_ORDER_TOOL, get_order_info)
    registry.register(GET_CUSTOMER_ORDERS_TOOL, get_customer_orders)
