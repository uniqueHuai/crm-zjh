import request from '@/api/request'
import type { R } from '@/types'

export interface SysConfig {
  id: number
  configName: string
  configKey: string
  configValue: string
  configType: 0 | 1
  isPublic?: boolean
  remark?: string
  createdAt?: string
}

export interface PageRes<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export function getConfigPage(params: {
  page?: number
  size?: number
  keywords?: string
}): Promise<R<PageRes<SysConfig>>> {
  return request.get('/configs', { params })
}

export function createConfig(data: Partial<SysConfig>): Promise<R<null>> {
  return request.post('/configs', data)
}

export function updateConfig(configKey: string, data: Partial<SysConfig>): Promise<R<null>> {
  return request.put(`/configs/${configKey}`, data)
}

export function deleteConfig(id: number): Promise<R<null>> {
  return request.delete(`/configs/${id}`)
}
