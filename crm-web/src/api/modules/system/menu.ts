import request from '@/api/request'
import type { R } from '@/types'

export interface SysMenuNode {
  id: number
  parentId: number
  name: string
  menuType: 'M' | 'C' | 'F'
  icon?: string
  routePath?: string
  permissionCode?: string
  sortOrder: number
  status: number
  children?: SysMenuNode[]
}

export function getMenuTree(): Promise<R<{ records: SysMenuNode[] }>> {
  return request.get('/menus/tree')
}

export function createMenu(data: Partial<SysMenuNode>): Promise<R<null>> {
  return request.post('/menus', data)
}

export function updateMenu(id: number, data: Partial<SysMenuNode>): Promise<R<null>> {
  return request.put(`/menus/${id}`, data)
}

export function deleteMenu(id: number): Promise<R<null>> {
  return request.delete(`/menus/${id}`)
}
