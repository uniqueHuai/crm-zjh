import request from '@/api/request'
import type { R, PageResult, PageParam, UserInfo } from '@/types'

export interface SysUser {
  id?: number
  username: string
  password?: string
  realName: string
  phone?: string
  email?: string
  deptId?: number
  status?: number
  roleIds?: number[]
  createdAt?: string
}

export function getUserPage(params: PageParam): Promise<R<PageResult<SysUser>>> {
  return request.get('/users', { params })
}

export function getUserById(id: number): Promise<R<SysUser>> {
  return request.get(`/users/${id}`)
}

export function createUser(data: SysUser): Promise<R<null>> {
  return request.post('/users', data)
}

export function updateUser(id: number, data: SysUser): Promise<R<null>> {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: number): Promise<R<null>> {
  return request.delete(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number): Promise<R<null>> {
  return request.put(`/users/${id}/status`, { status })
}

export function resetPassword(id: number, password: string): Promise<R<null>> {
  return request.put(`/users/${id}/reset-password`, { password })
}
