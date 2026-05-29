import request from '@/api/request'
import type { LoginRequest, LoginResponse, R, UserInfo } from '@/types'

export function loginApi(data: LoginRequest): Promise<R<LoginResponse>> {
  return request.post('/auth/login', data)
}

export function logoutApi(): Promise<R<null>> {
  return request.post('/auth/logout')
}

export function getUserInfoApi(): Promise<R<UserInfo>> {
  return request.get('/auth/user-info')
}

export function updatePasswordApi(data: { oldPassword: string; newPassword: string }): Promise<R<null>> {
  return request.put('/auth/password', data)
}
