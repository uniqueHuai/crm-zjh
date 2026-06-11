import request from '@/api/request'
import type { R, PageResult, PageParam, Opportunity, FollowUp, Contract } from '@/types'

/* ======== 销售阶段定义 ======== */

export interface OpportunityStage {
  id: number
  name: string
  sortOrder: number
  probability: number
  category: 'open' | 'win' | 'lose'
}

export function getOpportunityStages(): Promise<R<OpportunityStage[]>> {
  return request.get('/opportunity-stages')
}

export function createOpportunityStage(data: Partial<OpportunityStage>): Promise<R<null>> {
  return request.post('/opportunity-stages', data)
}

export function updateOpportunityStage(id: number, data: Partial<OpportunityStage>): Promise<R<null>> {
  return request.put(`/opportunity-stages/${id}`, data)
}

export function deleteOpportunityStage(id: number): Promise<R<null>> {
  return request.delete(`/opportunity-stages/${id}`)
}

/* ======== 商机管理 ======== */

export interface OpportunityQuery extends PageParam {
  stageId?: number
  customerId?: number
  ownerId?: number
  expectedAmountGte?: number
  expectedAmountLte?: number
  expectedCloseDateGte?: string
  expectedCloseDateLte?: string
  isExpired?: boolean
}

export function getOpportunityPage(params: OpportunityQuery): Promise<R<PageResult<Opportunity>>> {
  return request.get('/opportunities', { params })
}

export function getOpportunityById(id: number): Promise<R<Opportunity>> {
  return request.get(`/opportunities/${id}`)
}

export function createOpportunity(data: Partial<Opportunity>): Promise<R<null>> {
  return request.post('/opportunities', data)
}

export function updateOpportunity(id: number, data: Partial<Opportunity>): Promise<R<null>> {
  return request.put(`/opportunities/${id}`, data)
}

export function deleteOpportunity(id: number): Promise<R<null>> {
  return request.delete(`/opportunities/${id}`)
}

export function changeOpportunityStage(id: number, stageId: number, remark?: string): Promise<R<null>> {
  return request.put(`/opportunities/${id}/stage`, { stageId, remark })
}

export function winOpportunity(id: number, data: { finalAmount: number; contractId?: number; remark?: string }): Promise<R<null>> {
  return request.put(`/opportunities/${id}/win`, data)
}

export function loseOpportunity(id: number, data: { loseReason: string; loseReasonDetail?: string; competitor?: string }): Promise<R<null>> {
  return request.put(`/opportunities/${id}/lose`, data)
}

export function getOpportunityPipeline(): Promise<R<{ stageId: number; stageName: string; category: string; opportunities: Opportunity[]; totalAmount: number; count: number }[]>> {
  return request.get('/opportunities/pipeline')
}

export function updateOpportunityParticipants(id: number, userIds: number[]): Promise<R<null>> {
  return request.post(`/opportunities/${id}/participants`, { userIds })
}

export function removeOpportunityParticipants(id: number, userIds: number[]): Promise<R<null>> {
  return request.delete(`/opportunities/${id}/participants`, { data: { userIds } })
}

/* ======== 跟进记录 ======== */

export interface FollowUpQuery extends PageParam {
  customerId?: number
  opportunityId?: number
  type?: string
  creatorId?: number
  startDate?: string
  endDate?: string
}

export function getFollowUpPage(params: FollowUpQuery): Promise<R<PageResult<FollowUp>>> {
  return request.get('/follow-ups', { params })
}

export function getFollowUpById(id: number): Promise<R<FollowUp>> {
  return request.get(`/follow-ups/${id}`)
}

export function createFollowUp(data: Partial<FollowUp>): Promise<R<null>> {
  return request.post('/follow-ups', data)
}

export function updateFollowUp(id: number, data: Partial<FollowUp>): Promise<R<null>> {
  return request.put(`/follow-ups/${id}`, data)
}

export function deleteFollowUp(id: number): Promise<R<null>> {
  return request.delete(`/follow-ups/${id}`)
}

/* ======== 拜访日程 ======== */

export interface Appointment {
  id?: number
  customerId: number
  contactId?: number
  title: string
  description?: string
  appointmentDate: string
  startTime: string
  endTime: string
  location?: string
  type: 'visit' | 'call' | 'meeting' | 'other'
  status: 'pending' | 'completed' | 'cancelled'
  createdAt?: string
}

export function getAppointmentPage(params: PageParam & { startDate?: string; endDate?: string; status?: string; ownerId?: number }): Promise<R<PageResult<Appointment>>> {
  return request.get('/appointments', { params })
}

export function createAppointment(data: Partial<Appointment>): Promise<R<null>> {
  return request.post('/appointments', data)
}

export function updateAppointment(id: number, data: Partial<Appointment>): Promise<R<null>> {
  return request.put(`/appointments/${id}`, data)
}

export function deleteAppointment(id: number): Promise<R<null>> {
  return request.delete(`/appointments/${id}`)
}

export function checkInAppointment(id: number, data: { longitude: number; latitude: number; address: string; photoUrls?: string[] }): Promise<R<null>> {
  return request.put(`/appointments/${id}/check-in`, data)
}

export function completeAppointment(id: number, data: { summary: string; nextStep?: string }): Promise<R<null>> {
  return request.put(`/appointments/${id}/complete`, data)
}

export function getCalendarAppointments(params: { startDate: string; endDate: string; ownerId?: number }): Promise<R<{ date: string; items: any[] }[]>> {
  return request.get('/appointments/calendar', { params })
}

export function updateAppointmentStatus(id: number, status: string, cancelReason?: string): Promise<R<null>> {
  return request.put(`/appointments/${id}/status`, { status, cancelReason })
}

/* ======== 产品管理 ======== */

export interface Product {
  id?: number
  name: string
  categoryId?: number
  categoryName?: string
  unit: string
  standardPrice: number
  costPrice?: number
  description?: string
  specifications?: string
  status: number
  createdAt?: string
}

export function getProductPage(params: PageParam & { categoryId?: number; keywords?: string; status?: number }): Promise<R<PageResult<Product>>> {
  return request.get('/sales-products', { params })
}

export function createProduct(data: Partial<Product>): Promise<R<null>> {
  return request.post('/sales-products', data)
}

export function updateProduct(id: number, data: Partial<Product>): Promise<R<null>> {
  return request.put(`/sales-products/${id}`, data)
}

export function deleteProduct(id: number): Promise<R<null>> {
  return request.delete(`/sales-products/${id}`)
}

export function getProductCategories(): Promise<R<any[]>> {
  return request.get('/product-categories/tree')
}

/* ======== 报价管理 ======== */

export interface Quotation {
  id?: number
  customerId: number
  opportunityId?: number
  contactId?: number
  totalAmount: number
  discountAmount?: number
  finalAmount: number
  status: 'draft' | 'pending_approval' | 'approved' | 'rejected' | 'voided'
  validUntil: string
  items: QuotationItem[]
  createdAt?: string
}

export interface QuotationItem {
  productId: number
  productName: string
  quantity: number
  unitPrice: number
  discountRate: number
  subtotal: number
  remark?: string
}

export function getQuotationPage(params: PageParam & { customerId?: number; opportunityId?: number; status?: string; startDate?: string; endDate?: string }): Promise<R<PageResult<Quotation>>> {
  return request.get('/quotations', { params })
}

export function getQuotationById(id: number): Promise<R<Quotation>> {
  return request.get(`/quotations/${id}`)
}

export function createQuotation(data: Partial<Quotation>): Promise<R<null>> {
  return request.post('/quotations', data)
}

export function updateQuotation(id: number, data: Partial<Quotation>): Promise<R<null>> {
  return request.put(`/quotations/${id}`, data)
}

export function deleteQuotation(id: number): Promise<R<null>> {
  return request.delete(`/quotations/${id}`)
}

export function submitQuotationApproval(id: number, remark?: string): Promise<R<null>> {
  return request.post(`/quotations/${id}/submit-approval`, { remark })
}

export function approveQuotation(id: number, comment?: string): Promise<R<null>> {
  return request.put(`/quotations/${id}/approve`, { comment })
}

export function rejectQuotation(id: number, comment: string): Promise<R<null>> {
  return request.put(`/quotations/${id}/reject`, { comment })
}

export function voidQuotation(id: number, reason: string): Promise<R<null>> {
  return request.put(`/quotations/${id}/void`, { reason })
}

export function generateContractFromQuotation(id: number): Promise<R<null>> {
  return request.post(`/quotations/${id}/generate-contract`)
}

/* ======== 合同管理 ======== */

export interface ContractQuery extends PageParam {
  customerId?: number
  status?: string
  startDate?: string
  endDate?: string
}

export function getContractPage(params: ContractQuery): Promise<R<PageResult<Contract>>> {
  return request.get('/contracts', { params })
}

export function getContractById(id: number): Promise<R<Contract>> {
  return request.get(`/contracts/${id}`)
}

export function createContract(data: Partial<Contract>): Promise<R<null>> {
  return request.post('/contracts', data)
}

export function updateContract(id: number, data: Partial<Contract>): Promise<R<null>> {
  return request.put(`/contracts/${id}`, data)
}

export function deleteContract(id: number): Promise<R<null>> {
  return request.delete(`/contracts/${id}`)
}

export function signContract(id: number, data: { signType: string; platform?: string; signUrl?: string }): Promise<R<null>> {
  return request.post(`/contracts/${id}/sign`, data)
}

export function cancelContract(id: number, reason: string, attachmentIds?: number[]): Promise<R<null>> {
  return request.post(`/contracts/${id}/cancel`, { reason, attachmentIds })
}

export function renewContract(id: number, data: { newValidFrom: string; newValidUntil: string; newAmount: number; remark?: string }): Promise<R<null>> {
  return request.post(`/contracts/${id}/renewal`, data)
}

/* ======== 合同附件 ======== */

export function getContractAttachments(contractId: number): Promise<R<any[]>> {
  return request.get(`/contracts/${contractId}/attachments`)
}

export function deleteContractAttachment(fileId: number): Promise<R<null>> {
  return request.delete(`/contracts/attachments/${fileId}`)
}

export function getContractTemplates(): Promise<R<any[]>> {
  return request.get('/contract-templates')
}

export function createContractTemplate(data: any): Promise<R<null>> {
  return request.post('/contract-templates', data)
}

export function updateContractTemplate(id: number, data: any): Promise<R<null>> {
  return request.put(`/contract-templates/${id}`, data)
}

export function deleteContractTemplate(id: number): Promise<R<null>> {
  return request.delete(`/contract-templates/${id}`)
}

/* ======== 回款计划 ======== */

export function getPaymentPlans(contractId: number): Promise<R<{ records: any[] }>> {
  return request.get(`/contracts/${contractId}/payment-plans`)
}

export function createPaymentPlan(contractId: number, data: any): Promise<R<null>> {
  return request.post(`/contracts/${contractId}/payment-plans`, data)
}

export function updatePaymentPlan(id: number, data: any): Promise<R<null>> {
  return request.put(`/payment-plans/${id}`, data)
}

export function deletePaymentPlan(id: number): Promise<R<null>> {
  return request.delete(`/payment-plans/${id}`)
}

export function settlePaymentPlan(id: number, data: { actualAmount: number; paidDate: string; paymentMethod: string; voucherUrls?: string[]; remark?: string }): Promise<R<null>> {
  return request.put(`/payment-plans/${id}/settle`, data)
}

export function batchCreatePaymentPlans(contractId: number, data: { template: string; installments: number; totalAmount: number; firstDate: string; intervalDays: number }): Promise<R<null>> {
  return request.post(`/contracts/${contractId}/payment-plans/batch`, data)
}

/* ======== 发票管理 ======== */

export function getInvoicePage(params: PageParam & { customerId?: number; contractId?: number; status?: string; startDate?: string; endDate?: string }): Promise<R<PageResult<any>>> {
  return request.get('/invoices', { params })
}

export function createInvoice(data: any): Promise<R<null>> {
  return request.post('/invoices', data)
}

export function issueInvoice(id: number, data: { invoiceNo: string; invoiceFileUrl?: string; issueDate: string }): Promise<R<null>> {
  return request.put(`/invoices/${id}/issue`, data)
}

export function shipInvoice(id: number, data: { expressCompany: string; expressNo: string }): Promise<R<null>> {
  return request.put(`/invoices/${id}/ship`, data)
}

export function confirmInvoice(id: number): Promise<R<null>> {
  return request.put(`/invoices/${id}/confirm`)
}

export function cancelInvoice(id: number, reason: string): Promise<R<null>> {
  return request.put(`/invoices/${id}/cancel`, { reason })
}
