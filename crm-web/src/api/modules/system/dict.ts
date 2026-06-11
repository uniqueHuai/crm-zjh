import request from '@/api/request'
import type { R } from '@/types'

export interface SysDictType {
  id: number
  typeCode: string
  typeName: string
  status: number
  remark?: string
}

export interface SysDictItem {
  id: number
  typeCode: string
  itemCode: string
  itemValue: string
  sortOrder: number
  status: number
  cssClass?: string
}

export interface PageRes<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/* ===== Dict Types ===== */

export function getDictTypePage(params: {
  page?: number
  size?: number
  keywords?: string
}): Promise<R<PageRes<SysDictType>>> {
  return request.get('/dict-types', { params })
}

export function getDictTypeDetail(id: number): Promise<R<SysDictType>> {
  return request.get(`/dict-types/${id}`)
}

export function createDictType(data: Partial<SysDictType>): Promise<R<null>> {
  return request.post('/dict-types', data)
}

export function updateDictType(id: number, data: Partial<SysDictType>): Promise<R<null>> {
  return request.put(`/dict-types/${id}`, data)
}

export function deleteDictType(id: number): Promise<R<null>> {
  return request.delete(`/dict-types/${id}`)
}

/* ===== Dict Items ===== */

export function getDictItems(typeCode: string): Promise<R<{ records: SysDictItem[] }>> {
  return request.get(`/dict-types/${typeCode}/items`)
}

export function createDictItem(typeCode: string, data: Partial<SysDictItem>): Promise<R<null>> {
  return request.post(`/dict-types/${typeCode}/items`, data)
}

export function updateDictItem(id: number, data: Partial<SysDictItem>): Promise<R<null>> {
  return request.put(`/dict-items/${id}`, data)
}

export function deleteDictItem(id: number): Promise<R<null>> {
  return request.delete(`/dict-items/${id}`)
}
