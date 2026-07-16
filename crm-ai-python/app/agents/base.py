"""Agent 基类 — 公共的 LLM 对话循环和工具调用逻辑"""

from __future__ import annotations

import json
from typing import AsyncGenerator, Optional

from app.llm_client import LlmClient
from app.tools.registry import registry


class AIAgent:
    """AI 智能体基类，封装对话循环 + 工具调用"""

    def __init__(
        self,
        llm: LlmClient,
        system_prompt: str,
        agent_type: str,
        use_tools: bool = True,
    ):
        self.llm = llm
        self.system_prompt = system_prompt
        self.agent_type = agent_type
        self.use_tools = use_tools

    def _build_messages(self, history: list[dict], query: str) -> list[dict]:
        """构建完整的 messages 列表（system + history + query）"""
        messages = [{"role": "system", "content": self.system_prompt}]
        for msg in history:
            messages.append({"role": msg["role"], "content": msg["content"]})
        messages.append({"role": "user", "content": query})
        return messages

    async def chat_stream(
        self,
        history: list[dict],
        query: str,
    ) -> AsyncGenerator[str, Optional[dict]]:
        """
        流式对话。yield 两种类型：
        - str: 文本片段
        - dict: 特殊事件（如 {"type": "tool_result", "content": "..."}）
        最后 yield {"type": "done"}。
        """
        messages = self._build_messages(history, query)
        tools = registry.get_definitions() if self.use_tools else None

        max_rounds = 5  # 防止工具调用无限循环
        for _ in range(max_rounds):
            full_content = ""
            tool_calls_json = None

            async for chunk in self.llm.chat_stream(messages, tools):
                if chunk.startswith("__TOOL_CALLS__:"):
                    tool_calls_json = chunk[len("__TOOL_CALLS__:"):]
                else:
                    full_content += chunk
                    yield chunk

            # 如果有工具调用
            if tool_calls_json:
                try:
                    tool_calls = json.loads(tool_calls_json)
                    # 把 LLM 的响应加入消息历史
                    assistant_msg = {"role": "assistant", "content": full_content or None}
                    if tool_calls:
                        assistant_msg["tool_calls"] = [
                            {
                                "id": tc["id"],
                                "type": "function",
                                "function": {"name": tc["function"]["name"], "arguments": tc["function"]["arguments"]},
                            }
                            for tc in tool_calls
                        ]
                    messages.append(assistant_msg)

                    # 执行工具调用
                    for tc in tool_calls:
                        name = tc["function"]["name"]
                        args = json.loads(tc["function"]["arguments"])
                        result = await registry.execute(name, args)
                        messages.append({
                            "role": "tool",
                            "tool_call_id": tc["id"],
                            "content": result,
                        })
                        # yield 工具结果摘要（用于前端展示）
                        yield {"type": "tool_result", "tool": name, "content": result}

                    # 继续循环，让 LLM 基于工具结果生成最终回复
                    continue
                except (json.JSONDecodeError, KeyError) as e:
                    yield f"\n\n[工具调用解析失败: {e}]"
                    break

            # 没有工具调用 → 回复完成
            break

        yield {"type": "done"}
