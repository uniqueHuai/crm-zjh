"""LLM 客户端 — 通过 OpenAI 兼容 SDK 调用 SiliconFlow"""

from __future__ import annotations

import json
from typing import AsyncGenerator, Optional

from openai import AsyncOpenAI

from app.config import settings


class LlmClient:
    """大模型调用封装，支持流式 SSE 输出"""

    def __init__(self):
        self.client = AsyncOpenAI(
            api_key=settings.siliconflow_api_key,
            base_url=settings.siliconflow_base_url,
        )
        self.model = settings.siliconflow_model
        self.temperature = settings.siliconflow_temperature
        self.max_tokens = settings.siliconflow_max_tokens

    async def chat_stream(
        self,
        messages: list[dict],
        tools: Optional[list[dict]] = None,
    ) -> AsyncGenerator[str, None]:
        """
        流式调用 LLM，逐块 yield 文本内容。
        支持 function calling，当检测到 tool_calls 时直接返回序列化后的 tool_call。
        """
        kwargs = dict(
            model=self.model,
            messages=messages,
            temperature=self.temperature,
            max_tokens=self.max_tokens,
            stream=True,
            stream_options={"include_usage": False},
        )
        if tools:
            kwargs["tools"] = tools
            kwargs["tool_choice"] = "auto"

        stream = await self.client.chat.completions.create(**kwargs)

        # 收集 tool_calls
        pending_tool_calls: dict[int, dict] = {}

        async for chunk in stream:
            delta = chunk.choices[0].delta if chunk.choices else None
            if delta is None:
                continue

            # 文本 token
            if delta.content:
                yield delta.content

            # tool_calls 累积
            if delta.tool_calls:
                for tc in delta.tool_calls:
                    idx = tc.index
                    if idx not in pending_tool_calls:
                        pending_tool_calls[idx] = {
                            "id": tc.id or "",
                            "function": {"name": "", "arguments": ""},
                        }
                    if tc.id:
                        pending_tool_calls[idx]["id"] = tc.id
                    if tc.function:
                        if tc.function.name:
                            pending_tool_calls[idx]["function"]["name"] += tc.function.name
                        if tc.function.arguments:
                            pending_tool_calls[idx]["function"]["arguments"] += tc.function.arguments

        # 有 tool_calls → 以特殊格式返回，让 agent 层处理
        if pending_tool_calls:
            tool_calls_list = [
                {"id": v["id"], "type": "function", "function": v["function"]}
                for v in sorted(pending_tool_calls.values(), key=lambda x: list(pending_tool_calls.keys())[0])
            ]
            yield f"__TOOL_CALLS__:{json.dumps(tool_calls_list)}"

    async def chat(
        self,
        messages: list[dict],
        tools: Optional[list[dict]] = None,
    ) -> tuple[str, Optional[list[dict]]]:
        """
        非流式调用，返回 (content, tool_calls)。
        """
        kwargs = dict(
            model=self.model,
            messages=messages,
            temperature=self.temperature,
            max_tokens=self.max_tokens,
        )
        if tools:
            kwargs["tools"] = tools
            kwargs["tool_choice"] = "auto"

        resp = await self.client.chat.completions.create(**kwargs)
        choice = resp.choices[0]
        msg = choice.message

        if msg.tool_calls:
            tool_calls = [
                {
                    "id": tc.id,
                    "type": "function",
                    "function": {"name": tc.function.name, "arguments": tc.function.arguments},
                }
                for tc in msg.tool_calls
            ]
            return msg.content or "", tool_calls

        return msg.content or "", None
