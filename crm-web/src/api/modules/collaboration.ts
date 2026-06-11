import request from '@/api/request'
import type { R, PageResult, PageParam } from '@/types'

/* ======== 审批流引擎 ======== */

export function getApprovalDefines(): Promise<R<any[]>> {
  return request.get('/approval-defines')
}

export function createApprovalDefine(data: any): Promise<R<null>> {
  return request.post('/approval-defines', data)
}

export function getApprovalDefineById(id: number): Promise<R<any>> {
  return request.get(`/approval-defines/${id}`)
}

export function updateApprovalDefine(id: number, data: any): Promise<R<null>> {
  return request.put(`/approval-defines/${id}`, data)
}

export function deleteApprovalDefine(id: number): Promise<R<null>> {
  return request.delete(`/approval-defines/${id}`)
}

export function updateApprovalDefineStatus(id: number, status: number): Promise<R<null>> {
  return request.put(`/approval-defines/${id}/status`, { status })
}

export function submitApprovalInstance(data: { defineId: number; bizId: number; bizType: string; formData: Record<string, any> }): Promise<R<null>> {
  return request.post('/approval-instances', data)
}

export function getApprovalInstancePage(params: PageParam & { status?: string; bizType?: string; applicantId?: number; keywords?: string; dateFrom?: string; dateTo?: string }): Promise<R<PageResult<any>>> {
  return request.get('/approval-instances', { params })
}

export function getPendingApprovals(params: PageParam): Promise<R<PageResult<any>>> {
  return request.get('/approval-instances/pending', { params })
}

export function approveApprovalInstance(id: number, comment?: string): Promise<R<null>> {
  return request.put(`/approval-instances/${id}/approve`, { comment })
}

export function rejectApprovalInstance(id: number, comment: string): Promise<R<null>> {
  return request.put(`/approval-instances/${id}/reject`, { comment })
}

export function getApprovalInstanceDetail(id: number): Promise<R<any>> {
  return request.get(`/approval-instances/${id}`)
}

export function recallApprovalInstance(id: number): Promise<R<null>> {
  return request.put(`/approval-instances/${id}/recall`)
}

/* ======== 企业微信集成 ======== */

export function getWecomJsConfig(url: string): Promise<R<{ corpId: string; agentId: string; timestamp: number; nonceStr: string; signature: string }>> {
  return request.get('/wecom/js-config', { params: { url } })
}

export function bindWecomUser(userId: number, wecomUserId: string): Promise<R<null>> {
  return request.post('/wecom/bind', { userId, wecomUserId })
}

export function getWecomSidebarCustomer(customerId: number): Promise<R<any>> {
  return request.get('/wecom/sidebar/customer', { params: { customerId } })
}

export function sendWecomMessage(data: { wecomUserIds: string[]; msgType: string; title: string; description: string; url?: string }): Promise<R<null>> {
  return request.post('/wecom/messages/send', data)
}

/* ======== 服务工单 ======== */

export function getTicketPage(params: PageParam & { status?: string; type?: string; priority?: string }): Promise<R<PageResult<any>>> {
  return request.get('/tickets', { params })
}

export function createTicket(data: { customerId: number; type: string; title: string; description: string; priority: string; source: string; attachments?: number[] }): Promise<R<null>> {
  return request.post('/tickets', data)
}

export function getTicketById(id: number): Promise<R<any>> {
  return request.get(`/tickets/${id}`)
}

export function updateTicket(id: number, data: any): Promise<R<null>> {
  return request.put(`/tickets/${id}`, data)
}

export function deleteTicket(id: number): Promise<R<null>> {
  return request.delete(`/tickets/${id}`)
}

export function assignTicket(id: number, assigneeId: number): Promise<R<null>> {
  return request.put(`/tickets/${id}/assign`, { assigneeId })
}

export function acceptTicket(id: number): Promise<R<null>> {
  return request.put(`/tickets/${id}/accept`)
}

export function startTicket(id: number): Promise<R<null>> {
  return request.put(`/tickets/${id}/start`)
}

export function completeTicket(id: number): Promise<R<null>> {
  return request.put(`/tickets/${id}/complete`)
}

export function rateTicket(id: number, rating: number, comment?: string): Promise<R<null>> {
  return request.put(`/tickets/${id}/rate`, { rating, comment })
}
