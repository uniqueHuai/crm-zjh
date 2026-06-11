import request from '@/api/request'
import type { R, PageResult, PageParam, MenuTree, UserInfo } from '@/types'

/* ======== 角色管理 ======== */

export interface Role {
  id?: number
  name: string
  roleCode: string
  dataScope: number
  status: number
  remark?: string
  menuIds?: number[]
  permissionCodes?: string[]
  createTime?: string
}

export function getRolePage(params: PageParam & { keywords?: string; status?: number }): Promise<R<PageResult<Role>>> {
  return request.get('/roles', { params })
}

export function getRoleById(id: number): Promise<R<Role>> {
  return request.get(`/roles/${id}`)
}

export function createRole(data: Role): Promise<R<null>> {
  return request.post('/roles', data)
}

export function updateRole(id: number, data: Role): Promise<R<null>> {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id: number): Promise<R<null>> {
  return request.delete(`/roles/${id}`)
}

export function assignRoleMenus(id: number, menuIds: number[]): Promise<R<null>> {
  return request.put(`/roles/${id}/menus`, { menuIds })
}

export function assignRoleDataScope(id: number, dataScope: number, deptIds?: number[]): Promise<R<null>> {
  return request.put(`/roles/${id}/data-scope`, { dataScope, deptIds })
}

export function getRoleOptions(): Promise<R<{ value: number; label: string }[]>> {
  return request.get('/roles/options')
}

/* ======== 菜单管理 ======== */

export interface MenuForm {
  parentId: number
  name: string
  menuType: 'M' | 'C' | 'F'
  icon?: string
  routePath?: string
  component?: string
  permissionCode?: string
  sortOrder: number
  isVisible?: boolean
  status: number
}

export function getMenuTree(): Promise<R<{ records: MenuTree[] }>> {
  return request.get('/menus/tree')
}

export function createMenu(data: MenuForm): Promise<R<null>> {
  return request.post('/menus', data)
}

export function updateMenu(id: number, data: Partial<MenuForm>): Promise<R<null>> {
  return request.put(`/menus/${id}`, data)
}

export function deleteMenu(id: number): Promise<R<null>> {
  return request.delete(`/menus/${id}`)
}

export function getMenuRoutes(): Promise<R<MenuTree[]>> {
  return request.get('/menus/routes')
}

/* ======== 部门管理 ======== */

export interface Dept {
  id?: number
  parentId: number
  name: string
  leaderName?: string
  phone?: string
  sortOrder: number
  status: number
  children?: Dept[]
}

export function getDeptTree(): Promise<R<{ records: Dept[] }>> {
  return request.get('/depts/tree')
}

export function createDept(data: Partial<Dept>): Promise<R<null>> {
  return request.post('/depts', data)
}

export function updateDept(id: number, data: Partial<Dept>): Promise<R<null>> {
  return request.put(`/depts/${id}`, data)
}

export function deleteDept(id: number): Promise<R<null>> {
  return request.delete(`/depts/${id}`)
}

export function getDeptOptions(): Promise<R<{ value: number; label: string }[]>> {
  return request.get('/depts/options')
}

/* ======== 数据字典 ======== */

export interface DictType {
  id?: number
  typeCode: string
  typeName: string
  remark?: string
  status: number
}

export interface DictItem {
  id?: number
  itemCode: string
  itemValue: string
  sortOrder: number
  status: number
}

export function getDictTypePage(params: PageParam & { keywords?: string }): Promise<R<PageResult<DictType>>> {
  return request.get('/dict-types', { params })
}

export function createDictType(data: DictType): Promise<R<null>> {
  return request.post('/dict-types', data)
}

export function getDictTypeById(id: number): Promise<R<DictType>> {
  return request.get(`/dict-types/${id}`)
}

export function updateDictType(id: number, data: Partial<DictType>): Promise<R<null>> {
  return request.put(`/dict-types/${id}`, data)
}

export function deleteDictType(id: number): Promise<R<null>> {
  return request.delete(`/dict-types/${id}`)
}

export function getDictItems(typeCode: string): Promise<R<{ records: DictItem[] }>> {
  return request.get(`/dict-types/${typeCode}/items`)
}

export function createDictItem(typeCode: string, data: DictItem): Promise<R<null>> {
  return request.post(`/dict-types/${typeCode}/items`, data)
}

export function updateDictItem(id: number, data: Partial<DictItem>): Promise<R<null>> {
  return request.put(`/dict-items/${id}`, data)
}

export function deleteDictItem(id: number): Promise<R<null>> {
  return request.delete(`/dict-items/${id}`)
}

export function batchGetDicts(codes: string): Promise<R<Record<string, DictItem[]>>> {
  return request.get('/dicts', { params: { codes } })
}

/* ======== 系统配置 ======== */

export function getConfigPage(params: PageParam & { keywords?: string }): Promise<R<PageResult<any>>> {
  return request.get('/configs', { params })
}

export function getConfig(key: string): Promise<R<any>> {
  return request.get(`/configs/${key}`)
}

export function updateConfig(configKey: string, configValue: string, remark?: string): Promise<R<null>> {
  return request.put(`/configs/${configKey}`, { configValue, remark })
}

export function getPublicConfigs(): Promise<R<any[]>> {
  return request.get('/configs/public')
}

export function refreshConfigCache(): Promise<R<null>> {
  return request.post('/configs/refresh')
}

/* ======== 操作日志 ======== */

export function getOperationLogPage(params: PageParam & {
  module?: string
  action?: string
  operatorId?: number
  targetType?: string
  targetId?: number
  startDate?: string
  endDate?: string
}): Promise<R<PageResult<any>>> {
  return request.get('/operation-logs', { params })
}

export function getOperationLogById(id: number): Promise<R<any>> {
  return request.get(`/operation-logs/${id}`)
}

export function cleanOperationLogs(beforeDays: number): Promise<R<null>> {
  return request.delete('/operation-logs/clean', { data: { beforeDays } })
}

/* ======== 消息中心 ======== */

export function getMessagePage(params: PageParam & { isRead?: boolean; keywords?: string; bizType?: string; dateFrom?: string; dateTo?: string }): Promise<R<PageResult<any>>> {
  return request.get('/messages', { params })
}

export function markMessageRead(id: number): Promise<R<null>> {
  return request.put(`/messages/${id}/read`)
}

export function markAllMessagesRead(): Promise<R<null>> {
  return request.put('/messages/read-all')
}

export function sendMessage(data: { receiverType: string; receiverIds: number[]; channel: string[]; title: string; content: string; bizType?: string; bizId?: number; priority?: string }): Promise<R<null>> {
  return request.post('/messages/send', data)
}

export function getUnreadCount(): Promise<R<{ total: number; byType: Record<string, number> }>> {
  return request.get('/messages/unread-count')
}

/* ======== 文件管理 ======== */

export function uploadFile(file: File, bizType?: string, isPublic?: boolean): Promise<R<{ fileId: string; fileName: string; fileSize: number; fileUrl: string; thumbnailUrl?: string }>> {
  const formData = new FormData()
  formData.append('file', file)
  if (bizType) formData.append('bizType', bizType)
  if (isPublic !== undefined) formData.append('isPublic', String(isPublic))
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getFileById(id: string): Promise<R<any>> {
  return request.get(`/files/${id}`)
}

export function deleteFile(id: string): Promise<R<null>> {
  return request.delete(`/files/${id}`)
}

export function getFileDownloadUrl(id: string, expiresIn?: number): Promise<R<string>> {
  return request.get(`/files/${id}/download-url`, { params: { expiresIn } })
}

export function getFilesByBiz(bizType: string, bizId?: number): Promise<R<any[]>> {
  return request.get('/files', { params: { bizType, bizId } })
}

/* ======== API密钥管理 ======== */

export function createApiKey(data: { appName: string; permissions: string[]; expireDays: number; ipWhitelist?: string[] }): Promise<R<{ apiKey: string; appSecret: string }>> {
  return request.post('/api-keys', data)
}

export function getApiKeyList(): Promise<R<any[]>> {
  return request.get('/api-keys')
}

export function updateApiKeyStatus(id: number, status: number): Promise<R<null>> {
  return request.put(`/api-keys/${id}/status`, { status })
}

export function deleteApiKey(id: number): Promise<R<null>> {
  return request.delete(`/api-keys/${id}`)
}

export function regenerateApiKey(id: number): Promise<R<{ apiKey: string; appSecret: string }>> {
  return request.post(`/api-keys/${id}/regenerate`)
}
