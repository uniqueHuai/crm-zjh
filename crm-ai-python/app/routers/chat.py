"""SSE 聊天路由 — 处理三个助手的流式对话"""

from __future__ import annotations

import json
from datetime import datetime
from typing import AsyncGenerator, Optional

from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse

from app.agents.base import AIAgent
from app.agents.sales_assistant import create_sales_assistant
from app.agents.butler import create_butler
from app.agents.customer_service import create_customer_service, HANDOFF_MARKER
from app.llm_client import LlmClient
from app.schemas import ChatRequest, ConversationResponse, MessageResponse, R
from app.tools.registry import register_tools

router = APIRouter(prefix="/ai", tags=["AI 聊天"])

# 全局状态（生产环境应使用数据库）
_llm: Optional[LlmClient] = None
_agents: dict[str, AIAgent] = {}
_conversations: dict[int, dict] = {}  # id → conversation
_messages: dict[int, list[dict]] = {}  # conversation_id → messages
_next_conv_id = 1
_next_msg_id = 1


def init_chat_router(llm: LlmClient):
    global _llm, _agents
    _llm = llm
    register_tools()
    _agents = {
        "sales_assistant": create_sales_assistant(llm),
        "butler": create_butler(llm),
        "customer_service": create_customer_service(llm),
    }
    # 预置一个示例对话
    _seed_demo_conversation()


def _seed_demo_conversation():
    """创建示例历史对话用于演示"""
    global _next_conv_id, _next_msg_id
    for agent_type, title, msgs in [
        ("sales_assistant", "客户张总分析", [
            ("user", "帮我查一下张总的客户信息，我想了解他的消费情况"),
            ("assistant", "好的，我来查询张总的客户信息。\n\n**客户信息**：\n- 名称：张明\n- 等级：VIP客户\n- 累计消费：¥128,500.00\n- 历史订单：12单\n\n张总是我们的重要VIP客户，消费能力较强，建议可以重点推荐我们的高端产品线。"),
        ]),
        ("butler", "本月销售概况", [
            ("user", "这个月的销售情况怎么样？"),
            ("assistant", "📊 **本月销售概况**\n\n本月总销售额 ¥386,200，环比增长 15.3%，完成月度目标的 92%。\n\n核心数据：\n- 订单数：247 单\n- 成交客户：89 人\n- 回款率：78.6%\n\n热销产品前三名：「商务套餐」「企业定制方案」「会员年卡」。\n\n建议：月底冲刺阶段可对目标客户进行定向跟进，有望超额完成目标。"),
        ]),
    ]:
        conv_id = _next_conv_id
        _next_conv_id += 1
        _conversations[conv_id] = {
            "id": conv_id,
            "agent_type": agent_type,
            "title": title,
            "status": "active",
            "created_at": datetime.now(),
            "updated_at": datetime.now(),
        }
        conv_msgs = []
        for role, content in msgs:
            msg_id = _next_msg_id
            _next_msg_id += 1
            conv_msgs.append({
                "id": msg_id,
                "conversation_id": conv_id,
                "role": role,
                "content": content,
                "created_at": datetime.now(),
            })
        _messages[conv_id] = conv_msgs


def _get_agent(agent_type: str) -> AIAgent:
    agent = _agents.get(agent_type)
    if not agent:
        raise HTTPException(status_code=400, detail=f"未知的 Agent 类型: {agent_type}")
    return agent


@router.post("/{agent_type}/chat")
async def chat_stream(agent_type: str, req: ChatRequest):
    """
    SSE 流式聊天接口。
    前端用 fetch + ReadableStream 消费，兼容原有 SseClient。
    """
    agent = _get_agent(agent_type)

    async def event_stream():
        nonlocal req, agent

        # 获取历史消息
        history = []
        conv_id = req.conversation_id
        if conv_id and conv_id in _messages:
            for m in _messages[conv_id]:
                history.append({"role": m["role"], "content": m["content"]})

        async for event in agent.chat_stream(history, req.message):
            if isinstance(event, str):
                data = json.dumps({"content": event}, ensure_ascii=False)
                yield f"data: {data}\n\n"
            elif isinstance(event, dict):
                if event.get("type") == "done":
                    # 对话结束后保存
                    _save_conversation(agent_type, req, history)
                    # 检查是否需要转人工
                    if _check_handoff(history):
                        handoff_data = json.dumps({"handoff": True, "message": "正在为您转接人工客服，请稍候..."}, ensure_ascii=False)
                        yield f"data: {handoff_data}\n\n"
                    yield "data: [DONE]\n\n"
                elif event.get("type") == "tool_result":
                    # tool 结果不直接推送给前端
                    pass

    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )


def _save_conversation(agent_type: str, req: ChatRequest, history: list[dict]):
    """保存对话消息到内存（生产环境应写入数据库）"""
    global _next_conv_id, _next_msg_id

    conv_id = req.conversation_id
    is_new = conv_id is None or conv_id not in _conversations

    if is_new:
        conv_id = _next_conv_id
        _next_conv_id += 1
        title = req.message[:30] + ("…" if len(req.message) > 30 else "")
        _conversations[conv_id] = {
            "id": conv_id,
            "agent_type": agent_type,
            "title": title,
            "status": "active",
            "created_at": datetime.now(),
            "updated_at": datetime.now(),
        }
        _messages[conv_id] = []

    # 添加用户消息
    user_msg = {
        "id": _next_msg_id,
        "next_msg_id": _next_msg_id + 1,
        "conversation_id": conv_id,
        "role": "user",
        "content": req.message,
        "created_at": datetime.now(),
    }
    _messages[conv_id].append(user_msg)
    _next_msg_id += 1

    # 添加助手回复（从 history 中获取最后一条 assistant 消息）
    for m in reversed(history):
        if m["role"] == "assistant":
            assistant_msg = {
                "id": _next_msg_id,
                "next_msg_id": _next_msg_id + 1,
                "conversation_id": conv_id,
                "role": "assistant",
                "content": m["content"],
                "created_at": datetime.now(),
            }
            _messages[conv_id].append(assistant_msg)
            _next_msg_id += 1
            break

    _conversations[conv_id]["updated_at"] = datetime.now()


def _check_handoff(history: list[dict]) -> bool:
    """检查最后一条 assistant 回复是否包含转人工标记"""
    for m in reversed(history):
        if m["role"] == "assistant" and HANDOFF_MARKER in m.get("content", ""):
            # 移除标记
            m["content"] = m["content"].replace(HANDOFF_MARKER, "").strip()
            return True
    return False


# ── 对话管理 REST 接口 ──

@router.get("/{agent_type}/conversations", response_model=R)
async def list_conversations(agent_type: str):
    """获取对话列表"""
    convs = [
        {
            "id": c["id"],
            "agentType": c["agent_type"],
            "title": c["title"],
            "status": c["status"],
            "messageCount": len(_messages.get(c["id"], [])),
            "createdAt": c["created_at"].isoformat(),
            "updatedAt": c["updated_at"].isoformat(),
        }
        for c in _conversations.values()
        if c["agent_type"] == agent_type
    ]
    convs.sort(key=lambda x: x["updatedAt"], reverse=True)
    return R(data=convs)


@router.get("/{agent_type}/conversations/{conv_id}/messages", response_model=R)
async def get_conversation_messages(agent_type: str, conv_id: int):
    """获取对话消息列表"""
    msgs = _messages.get(conv_id, [])
    return R(data=[
        {
            "id": m["id"],
            "conversationId": m["conversation_id"],
            "role": m["role"],
            "content": m["content"],
            "createdAt": m["created_at"].isoformat(),
        }
        for m in msgs
    ])


@router.post("/{agent_type}/conversations/{conv_id}/close", response_model=R)
async def close_conversation(agent_type: str, conv_id: int):
    """关闭对话"""
    if conv_id in _conversations:
        _conversations[conv_id]["status"] = "closed"
    return R()


@router.post("/customer_service/conversations/{conv_id}/reply", response_model=R)
async def staff_reply(conv_id: int, body: dict):
    """人工客服回复消息"""
    global _next_msg_id
    if conv_id not in _conversations:
        raise HTTPException(status_code=404, detail="对话不存在")

    content = body.get("content", "").strip()
    if not content:
        raise HTTPException(status_code=400, detail="回复内容不能为空")

    msg = {
        "id": _next_msg_id,
        "conversation_id": conv_id,
        "role": "assistant",  # staff 回复以 assistant role 存储
        "content": content,
        "created_at": datetime.now(),
        "staff_reply": True,  # 标记为人工回复
    }
    _next_msg_id += 1
    _messages[conv_id].append(msg)
    _conversations[conv_id]["updated_at"] = datetime.now()
    return R()


@router.get("/customer_service/conversations/active", response_model=R)
async def list_active_conversations():
    """获取所有需要人工处理的活跃对话"""
    convs = [
        {
            "id": c["id"],
            "agentType": c["agent_type"],
            "title": c["title"],
            "status": c["status"],
            "messageCount": len(_messages.get(c["id"], [])),
            "handoff": any(
                m.get("content", "").find("转接人工") >= 0 or m.get("staff_reply", False)
                for m in _messages.get(c["id"], [])
            ),
            "createdAt": c["created_at"].isoformat(),
            "updatedAt": c["updated_at"].isoformat(),
            "lastMessage": _messages.get(c["id"], [{}])[-1].get("content", "")[:100] if _messages.get(c["id"]) else "",
        }
        for c in _conversations.values()
        if c["agent_type"] == "customer_service" and c["status"] == "active"
    ]
    convs.sort(key=lambda x: x["updatedAt"], reverse=True)
    return R(data=convs)
