import request from '@/api/request'
import type { R, PageResult, PageParam } from '@/types'

/* ======== 订单管理 ======== */

export function getOrderPage(params: PageParam & { status?: string; startDate?: string; endDate?: string; keywords?: string }): Promise<R<PageResult<any>>> {
  return request.get('/orders', { params })
}

export function getOrderById(id: number): Promise<R<any>> {
  return request.get(`/orders/${id}`)
}

export function updateOrderStatus(id: number, status: string): Promise<R<null>> {
  return request.put(`/orders/${id}/status`, { status })
}

/* ======== 优惠券管理 ======== */

export function getCouponPage(params: PageParam & { keywords?: string; status?: number }): Promise<R<PageResult<any>>> {
  return request.get('/coupons', { params })
}

export function createCoupon(data: any): Promise<R<null>> {
  return request.post('/coupons', data)
}

export function getCouponById(id: number): Promise<R<any>> {
  return request.get(`/coupons/${id}`)
}

export function updateCoupon(id: number, data: any): Promise<R<null>> {
  return request.put(`/coupons/${id}`, data)
}

export function deleteCoupon(id: number): Promise<R<null>> {
  return request.delete(`/coupons/${id}`)
}

export function distributeCoupon(id: number, userIds: number[]): Promise<R<null>> {
  return request.post(`/coupons/${id}/distribute`, { userIds })
}

/* ======== 营销活动 ======== */

export function getActivityPage(params: PageParam & { type?: string; status?: number; keywords?: string }): Promise<R<PageResult<any>>> {
  return request.get('/activities', { params })
}

export function createActivity(data: any): Promise<R<null>> {
  return request.post('/activities', data)
}

export function getActivityById(id: number): Promise<R<any>> {
  return request.get(`/activities/${id}`)
}

export function updateActivity(id: number, data: any): Promise<R<null>> {
  return request.put(`/activities/${id}`, data)
}

export function deleteActivity(id: number): Promise<R<null>> {
  return request.delete(`/activities/${id}`)
}

export function updateActivityStatus(id: number, status: number): Promise<R<null>> {
  return request.put(`/activities/${id}/status`, { status })
}

/* ======== 小程序页面管理 ======== */

export function getPageTemplatePage(params: PageParam & { keywords?: string; status?: number }): Promise<R<PageResult<any>>> {
  return request.get('/page-templates', { params })
}

export function createPageTemplate(data: any): Promise<R<null>> {
  return request.post('/page-templates', data)
}

export function getPageTemplateById(id: number): Promise<R<any>> {
  return request.get(`/page-templates/${id}`)
}

export function updatePageTemplate(id: number, data: any): Promise<R<null>> {
  return request.put(`/page-templates/${id}`, data)
}

export function deletePageTemplate(id: number): Promise<R<null>> {
  return request.delete(`/page-templates/${id}`)
}

export function publishPageTemplate(id: number): Promise<R<null>> {
  return request.put(`/page-templates/${id}/publish`)
}
