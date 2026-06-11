import request from '@/api/request'
import type { R } from '@/types'

export interface SysRole {
  id: number
  name: string
  roleCode: string
  dataScope: number
  status: number
  sortOrder?: number
  remark?: string
  menuIds?: number[]
  deptIds?: number[]
}

export interface SysRoleQuery {
  page: number
  size: number
  keywords?: string
  status?: number
}

export function getRolePage(query: SysRoleQuery): Promise<R<{ records: SysRole[]; total: number }>> {
  return request.get('/roles', { params: query })
}

export function getRoleDetail(id: number): Promise<R<SysRole>> {
  return request.get(`/roles/${id}`)
}

export function createRole(data: Partial<SysRole>): Promise<R<null>> {
  return request.post('/roles', data)
}

export function updateRole(id: number, data: Partial<SysRole>): Promise<R<null>> {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id: number): Promise<R<null>> {
  return request.delete(`/roles/${id}`)
}

export function assignRoleMenus(id: number, menuIds: number[]): Promise<R<null>> {
  return request.put(`/roles/${id}/menus`, { menuIds })
}

export function assignRoleDataScope(id: number, dataScope: number, deptIds: number[]): Promise<R<null>> {
  return request.put(`/roles/${id}/data-scope`, { dataScope, deptIds })
}
