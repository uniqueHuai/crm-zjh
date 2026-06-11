import request from '@/api/request'
import type { R } from '@/types'

export interface SysDeptNode {
  id: number
  parentId: number
  name: string
  leaderName?: string
  phone?: string
  email?: string
  sortOrder: number
  status: number
  createdAt?: string
  children?: SysDeptNode[]
}

export function getDeptTree(): Promise<R<{ records: SysDeptNode[] }>> {
  return request.get('/depts/tree')
}

export function createDept(data: Partial<SysDeptNode>): Promise<R<null>> {
  return request.post('/depts', data)
}

export function updateDept(id: number, data: Partial<SysDeptNode>): Promise<R<null>> {
  return request.put(`/depts/${id}`, data)
}

export function deleteDept(id: number): Promise<R<null>> {
  return request.delete(`/depts/${id}`)
}

export function getDeptOptions(): Promise<R<SysDeptNode[]>> {
  return request.get('/depts/options')
}
