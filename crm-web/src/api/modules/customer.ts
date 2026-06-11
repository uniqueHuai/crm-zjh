import request from '@/api/request'
import type { R, PageResult, PageParam, Lead, Customer, Contact } from '@/types'

/* ======== 线索管理 ======== */

export interface LeadQuery extends PageParam {
  status?: string
  sourceChannel?: string
  ownerId?: number
  assignType?: string
  province?: string
  city?: string
  industry?: string
  startDate?: string
  endDate?: string
  poolReturnAt?: boolean
}

export function getLeadPage(params: LeadQuery): Promise<R<PageResult<Lead>>> {
  return request.get('/leads', { params })
}

export function getLeadById(id: number): Promise<R<Lead>> {
  return request.get(`/leads/${id}`)
}

export function createLead(data: Partial<Lead>): Promise<R<null>> {
  return request.post('/leads', data)
}

export function updateLead(id: number, data: Partial<Lead>): Promise<R<null>> {
  return request.put(`/leads/${id}`, data)
}

export function deleteLead(id: number): Promise<R<null>> {
  return request.delete(`/leads/${id}`)
}

export function convertLead(id: number, data: { customerId?: number; createOpportunity?: boolean; opportunityName?: string; expectedAmount?: number }): Promise<R<null>> {
  return request.post(`/leads/${id}/convert`, data)
}

export function distributeLeads(data: { leadIds: number[]; ownerId: number; assignType: string }): Promise<R<null>> {
  return request.post('/leads/distribute', data)
}

export function detectDuplicate(data: { phone?: string; wechatUnionId?: string }): Promise<R<{ isDuplicate: boolean; matchedLeads: Lead[]; matchedCustomers: Customer[] }>> {
  return request.post('/leads/dedup-check', data)
}

export function mergeLeads(data: { mainLeadId: number; mergeLeadIds: number[]; fieldStrategy: Record<string, string> }): Promise<R<null>> {
  return request.post('/leads/merge', data)
}

export function returnLeadToPool(id: number): Promise<R<null>> {
  return request.put(`/leads/${id}/pool-return`)
}

export function batchOperateLeads(data: { leadIds: number[]; action: string; payload?: Record<string, unknown> }): Promise<R<null>> {
  return request.post('/leads/batch', data)
}

export function importLeads(file: File): Promise<R<{ totalCount: number; successCount: number; failCount: number; errors: string[] }>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/leads/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/* ======== 客户管理 ======== */

export interface CustomerQuery extends PageParam {
  levelId?: number
  tagIds?: string
  ownerId?: number
  sourceChannel?: string
  province?: string
  city?: string
  isSleeping?: boolean
  startDate?: string
  endDate?: string
}

export function getCustomerPage(params: CustomerQuery): Promise<R<PageResult<Customer>>> {
  return request.get('/customers', { params })
}

export function getCustomerDetail(id: number): Promise<R<{
  customer: Customer
  recentFollowUps: any[]
  opportunities: any[]
  orders: any[]
  contacts: Contact[]
  pendingTodos: any[]
  attachmentCount: number
  recentActivityLogs: any[]
}>> {
  return request.get(`/customers/${id}/detail`)
}

export function createCustomer(data: Partial<Customer>): Promise<R<null>> {
  return request.post('/customers', data)
}

export function updateCustomer(id: number, data: Partial<Customer>): Promise<R<null>> {
  return request.put(`/customers/${id}`, data)
}

export function deleteCustomer(id: number): Promise<R<null>> {
  return request.delete(`/customers/${id}`)
}

export function batchTagCustomers(data: { customerIds: number[]; tagIds: number[]; mode: 'append' | 'overwrite' }): Promise<R<null>> {
  return request.post('/customers/batch-tag', data)
}

export function removeBatchTags(data: { customerIds: number[]; tagIds: number[] }): Promise<R<null>> {
  return request.delete('/customers/batch-tag', { data })
}

export function transferCustomer(id: number, newOwnerId: number, transferFollowUps?: boolean): Promise<R<null>> {
  return request.put(`/customers/${id}/transfer`, { newOwnerId, transferFollowUps })
}

export function getCustomerActivityLogs(id: number, params: PageParam): Promise<R<PageResult<any>>> {
  return request.get(`/customers/${id}/activity-logs`, { params })
}

export function getCustomerTimeline(id: number, params: PageParam & { types?: string }): Promise<R<PageResult<any>>> {
  return request.get(`/customers/${id}/timeline`, { params })
}

/* ======== 标签管理 ======== */

export interface Tag {
  id?: number
  name: string
  color: string
  type: 'manual' | 'auto'
  remark?: string
  createdAt?: string
}

export function getTagPage(params: PageParam & { type?: string; keywords?: string }): Promise<R<PageResult<Tag>>> {
  return request.get('/tags', { params })
}

export function getAllTags(): Promise<R<Tag[]>> {
  return request.get('/tags/all')
}

export function createTag(data: Tag): Promise<R<null>> {
  return request.post('/tags', data)
}

export function updateTag(id: number, data: Tag): Promise<R<null>> {
  return request.put(`/tags/${id}`, data)
}

export function deleteTag(id: number): Promise<R<null>> {
  return request.delete(`/tags/${id}`)
}

/* ======== 自动标签规则 ======== */

export function getAutoRulesPage(params: PageParam): Promise<R<PageResult<any>>> {
  return request.get('/tags/auto-rules', { params })
}

export function createAutoRule(data: any): Promise<R<null>> {
  return request.post('/tags/auto-rules', data)
}

export function updateAutoRule(id: number, data: any): Promise<R<null>> {
  return request.put(`/tags/auto-rules/${id}`, data)
}

export function deleteAutoRule(id: number): Promise<R<null>> {
  return request.delete(`/tags/auto-rules/${id}`)
}

export function executeAutoRule(id: number): Promise<R<null>> {
  return request.post(`/tags/auto-rules/${id}/execute`)
}

/* ======== 客户等级 ======== */

export function getCustomerLevels(): Promise<R<any[]>> {
  return request.get('/customer-levels')
}

export function createCustomerLevel(data: any): Promise<R<null>> {
  return request.post('/customer-levels', data)
}

export function updateCustomerLevel(id: number, data: any): Promise<R<null>> {
  return request.put(`/customer-levels/${id}`, data)
}

export function deleteCustomerLevel(id: number): Promise<R<null>> {
  return request.delete(`/customer-levels/${id}`)
}

export function setLevelRules(id: number, data: any): Promise<R<null>> {
  return request.put(`/customer-levels/${id}/rules`, data)
}

export function evaluateLevels(): Promise<R<null>> {
  return request.post('/customer-levels/evaluate')
}

export function batchSetLevel(data: { customerIds: number[]; levelId: number; reason: string }): Promise<R<null>> {
  return request.put('/customers/batch-level', data)
}

/* ======== 客户分群 ======== */

export function getSegmentPage(params: PageParam): Promise<R<PageResult<any>>> {
  return request.get('/segments', { params })
}

export function createSegment(data: any): Promise<R<null>> {
  return request.post('/segments', data)
}

export function updateSegment(id: number, data: any): Promise<R<null>> {
  return request.put(`/segments/${id}`, data)
}

export function deleteSegment(id: number): Promise<R<null>> {
  return request.delete(`/segments/${id}`)
}

export function refreshSegment(id: number): Promise<R<null>> {
  return request.post(`/segments/${id}/refresh`)
}

export function getSegmentMembers(id: number, params: PageParam): Promise<R<PageResult<Customer>>> {
  return request.get(`/segments/${id}/members`, { params })
}

export function updateSegmentMembers(id: number, data: { customerIds: number[]; action: 'add' | 'remove' }): Promise<R<null>> {
  return request.post(`/segments/${id}/members`, data)
}

/* ======== 联系人管理 ======== */

export interface ContactQuery extends PageParam {
  keywords?: string
  customerId?: number
  isDecisionMaker?: boolean
}

export function getContactPage(params: ContactQuery): Promise<R<PageResult<Contact>>> {
  return request.get('/contacts', { params })
}

export function getContactsByCustomer(customerId: number): Promise<R<{ records: Contact[] }>> {
  return request.get(`/customers/${customerId}/contacts`)
}

export function createContact(data: Partial<Contact>): Promise<R<null>> {
  return request.post('/contacts', data)
}

export function updateContact(id: number, data: Partial<Contact>): Promise<R<null>> {
  return request.put(`/contacts/${id}`, data)
}

export function deleteContact(id: number): Promise<R<null>> {
  return request.delete(`/contacts/${id}`)
}

export function setPrimaryContact(id: number): Promise<R<null>> {
  return request.put(`/contacts/${id}/set-primary`)
}

export function getUpcomingBirthdays(days: number): Promise<R<Contact[]>> {
  return request.get('/contacts/upcoming-birthdays', { params: { days } })
}

/* ======== 分配规则 ======== */

export function getDistributionRules(): Promise<R<any[]>> {
  return request.get('/lead-distribution-rules')
}

export function createDistributionRule(data: any): Promise<R<null>> {
  return request.post('/lead-distribution-rules', data)
}

export function updateDistributionRule(id: number, data: any): Promise<R<null>> {
  return request.put(`/lead-distribution-rules/${id}`, data)
}

export function deleteDistributionRule(id: number): Promise<R<null>> {
  return request.delete(`/lead-distribution-rules/${id}`)
}

export function executeDistributionRule(id: number): Promise<R<null>> {
  return request.post(`/lead-distribution-rules/${id}/execute`)
}

export function getDistributionLogs(id: number, params: PageParam): Promise<R<PageResult<any>>> {
  return request.get(`/lead-distribution-rules/${id}/logs`, { params })
}
