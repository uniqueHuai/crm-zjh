import request from '@/api/request'
import type { R, PageResult, PageParam } from '@/types'

/* ======== 核心看板 ======== */

export function getDashboardSummary(): Promise<R<{
  totalCustomers: number
  totalOppAmount: number
  totalContracts: number
  monthlyNewCustomers: number
}>> {
  return request.get('/report/dashboard/summary')
}

export function getSalesFunnel(params: { startDate?: string; endDate?: string; ownerId?: number; deptId?: number }): Promise<R<{
  stages: { stageId: number; stageName: string; count: number; amount: number }[]
  winRate: number
  avgDealCycle: number
  forecastRevenue: number
  comparison: { winRateChange: number; amountChange: number }
}>> {
  return request.get('/report/dashboard/sales-funnel', { params })
}

export function getCustomerAnalysis(params: { startDate?: string; endDate?: string }): Promise<R<{
  trend: { newCustomers: { date: string; count: number }[]; lostCustomers: { date: string; count: number }[] }
  levelDistribution: { levelName: string; count: number; percentage: number }[]
  sleepingCount: number
  sleepingRate: number
  rfm: { avgRecency: number; avgFrequency: number; avgMonetary: number }
}>> {
  return request.get('/report/dashboard/customer-analysis', { params })
}

export function getEmployeePerformance(params: { startDate?: string; endDate?: string; deptId?: number }): Promise<R<{
  rankings: { userId: number; realName: string; deptName: string; followUpCount: number; newCustomerCount: number; dealAmount: number; dealCount: number; winRate: number }[]
  avgResponseTime: number
  avgFollowUpPerDay: number
}>> {
  return request.get('/report/dashboard/employee-performance', { params })
}

export function getMallDashboard(params: { startDate?: string; endDate?: string }): Promise<R<{
  gmv: number
  avgOrderAmount: number
  repurchaseRate: number
  topProducts: { productId: number; name: string; salesVolume: number; revenue: number }[]
  conversionRate: { visitToCart: number; cartToPay: number; visitToPay: number }
}>> {
  return request.get('/report/dashboard/mall', { params })
}

export function getDashboardTemplates(): Promise<R<any[]>> {
  return request.get('/report/dashboard/templates')
}

export function saveDashboardLayout(data: { cards: { cardType: string; position: number; width: number; height: number }[] }): Promise<R<null>> {
  return request.put('/report/dashboard/layout', data)
}

/* ======== 自定义报表 ======== */

export function getCustomReports(params?: { page?: number; size?: number; keywords?: string }): Promise<R<PageResult<any>>> {
  return request.get('/reports/custom', { params })
}

export function createCustomReport(data: { name: string; dataSource: string; dimensions: string[]; metrics: string[]; filters: any[]; chartType: string; schedule?: string; recipientIds?: number[] }): Promise<R<null>> {
  return request.post('/reports/custom', data)
}

export function getCustomReportData(id: number, params: PageParam): Promise<R<PageResult<any>>> {
  return request.get(`/reports/custom/${id}/data`, { params })
}

export function exportCustomReport(id: number, data: { format: string; startDate?: string; endDate?: string }): Promise<R<null>> {
  return request.post(`/reports/custom/${id}/export`, data)
}

export function updateCustomReport(id: number, data: any): Promise<R<null>> {
  return request.put(`/reports/custom/${id}`, data)
}

export function deleteCustomReport(id: number): Promise<R<null>> {
  return request.delete(`/reports/custom/${id}`)
}
