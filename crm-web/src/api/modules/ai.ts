import request from '@/api/request'
import type { R, PageResult, Conversation, KnowledgeBase } from '@/types'

/** 销售助手聊天（SSE） */
export function salesAssistantChat(data: {
  conversationId?: number
  message: string
}): Promise<Response> {
  return fetch(`/api/v1/ai/sales-assistant/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${localStorage.getItem('crm_access_token')}` },
    body: JSON.stringify(data),
  })
}

/** 智能管家聊天（SSE） */
export function butlerChat(data: {
  conversationId?: number
  message: string
}): Promise<Response> {
  return fetch(`/api/v1/ai/butler/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${localStorage.getItem('crm_access_token')}` },
    body: JSON.stringify(data),
  })
}

/** 获取对话列表 */
export function getConversationList(agentType: string): Promise<R<Conversation[]>> {
  const key = agentType === 'sales_assistant' ? 'sales-assistant' : 'butler'
  return request.get(`/ai/${key}/conversations`)
}

/** 获取对话消息 */
export function getConversationMessages(agentType: string, id: number): Promise<R<any[]>> {
  const key = agentType === 'sales_assistant' ? 'sales-assistant' : 'butler'
  return request.get(`/ai/${key}/conversations/${id}/messages`)
}

/** 关闭对话 */
export function closeConversation(agentType: string, id: number): Promise<R<void>> {
  const key = agentType === 'sales_assistant' ? 'sales-assistant' : 'butler'
  return request.post(`/ai/${key}/conversations/${id}/close`)
}

// ========== 知识库管理 ==========

/** 知识库分页列表 */
export function getKnowledgeBasePage(params: {
  page: number
  size: number
  keywords?: string
}): Promise<R<PageResult<KnowledgeBase>>> {
  return request.get('/ai/knowledge-base', { params })
}

/** 获取知识库详情 */
export function getKnowledgeBaseById(id: number): Promise<R<KnowledgeBase>> {
  return request.get(`/ai/knowledge-base/${id}`)
}

/** 新增知识库 */
export function createKnowledgeBase(data: Partial<KnowledgeBase>): Promise<R<void>> {
  return request.post('/ai/knowledge-base', data)
}

/** 编辑知识库 */
export function updateKnowledgeBase(data: Partial<KnowledgeBase>): Promise<R<void>> {
  return request.put('/ai/knowledge-base', data)
}

/** 删除知识库 */
export function deleteKnowledgeBase(id: number): Promise<R<void>> {
  return request.delete(`/ai/knowledge-base/${id}`)
}

/** 导入文档（文本） */
export function importDocument(kbId: number, title: string, content: string): Promise<R<void>> {
  return request.post(`/ai/knowledge-base/${kbId}/import`, { title, content })
}

/** 导入文档文件（.txt/.md/.doc/.docx/.pdf） */
export function importFile(kbId: number, formData: FormData): Promise<R<void>> {
  return request.post(`/ai/knowledge-base/${kbId}/import-file`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 获取启用知识库列表 */
export function getEnabledKnowledgeBases(): Promise<R<KnowledgeBase[]>> {
  return request.get('/ai/knowledge-base/enabled')
}
