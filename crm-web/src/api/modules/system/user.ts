import request from '@/api/request'
import type { R } from '@/types'

export interface SysUser {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  deptId: number
  deptName: string
  status: number
  avatar?: string
  gender?: number
  post?: string
  lastLoginAt?: string
  createdAt?: string
  roleIds?: number[]
}

export interface SysUserQuery {
  page: number
  size: number
  keywords?: string
  status?: number
  deptId?: number
}

export function getUserPage(query: SysUserQuery): Promise<R<{ records: SysUser[]; total: number }>> {
  return request.get('/users', { params: query })
}

export function getUserDetail(id: number): Promise<R<SysUser>> {
  return request.get(`/users/${id}`)
}

export function createUser(data: Partial<SysUser>): Promise<R<null>> {
  return request.post('/users', data)
}

export function updateUser(id: number, data: Partial<SysUser>): Promise<R<null>> {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: number): Promise<R<null>> {
  return request.delete(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number): Promise<R<null>> {
  return request.put(`/users/${id}/status`, { status })
}

export function resetUserPassword(id: number, password: string): Promise<R<null>> {
  return request.put(`/users/${id}/reset-password`, { password })
}
