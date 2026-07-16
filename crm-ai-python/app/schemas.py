"""Pydantic 数据模型"""

from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


# ── 请求体 ──

class ChatRequest(BaseModel):
    """聊天请求"""
    conversation_id: Optional[int] = Field(None, description="会话 ID，新对话传 null")
    message: str = Field(..., min_length=1, description="用户消息")
    customer_id: Optional[int] = Field(None, description="小程序客户 ID（客服）")


class KnowledgeBaseCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    description: str = ""
    type: str = "public"


class KnowledgeBaseUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    status: Optional[str] = None


class DocumentImport(BaseModel):
    title: str = Field(..., min_length=1)
    content: str = Field(..., min_length=1)


# ── 响应体 ──

class MessageResponse(BaseModel):
    id: int
    conversation_id: int
    role: str
    content: str
    created_at: datetime


class ConversationResponse(BaseModel):
    id: int
    agent_type: str
    title: str
    status: str
    message_count: int
    created_at: datetime
    updated_at: datetime


class KnowledgeBaseResponse(BaseModel):
    id: int
    name: str
    description: str
    type: str
    status: str
    doc_count: int
    created_at: datetime
    updated_at: datetime


class R(BaseModel):
    """通用响应包装，与 Java 后端 R<T> 对齐"""
    code: int = 200
    message: str = "ok"
    data: Optional[object] = None


# ── SSE 事件 ──

class SseEvent(BaseModel):
    """SSE 流式事件块"""
    content: str = ""


class SseDone(BaseModel):
    """SSE 结束标记"""


class SseConvId(BaseModel):
    """SSE 新会话 ID"""
    conv_id: int


class SseHandoff(BaseModel):
    """SSE 转人工标记"""
    handoff: bool = True
    message: str = "正在为您转接人工客服，请稍候..."
