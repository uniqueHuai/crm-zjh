/* ======== 通用类型 ======== */

export interface R<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
  timestamp?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface PageParam {
  page?: number
  size?: number
  sort?: string
  keywords?: string
  [key: string]: unknown
}

/* ======== 用户认证 ======== */

export interface LoginRequest {
  username: string
  password: string
  captchaId?: string
  captchaCode?: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
  userInfo: UserInfo
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  avatar: string
  deptId: number
  deptName: string
  roles: string[]
  permissions: string[]
}

/* ======== 菜单 ======== */

export interface MenuTree {
  id: number
  parentId: number
  name: string
  menuType: string
  icon: string
  routePath: string
  component: string
  permissionCode: string
  sortOrder: number
  isVisible: boolean
  status: number
  children: MenuTree[]
}

/* ======== 客户 ======== */

export interface Customer {
  id: number
  name: string
  phone: string
  company: string
  levelName: string
  ownerName: string
  tags: { id: number; name: string }[]
  lastContactAt: string
  sourceChannel: string
  createdAt: string
}

export interface Lead {
  id: number
  name: string
  phone: string
  sourceChannel: string
  status: string
  ownerName: string
  createdAt: string
}

export interface Contact {
  id: number
  customerId: number
  name: string
  phone: string
  position: string
  isDecisionMaker: boolean
}

/* ======== 销售 ======== */

export interface Opportunity {
  id: number
  name: string
  customerName: string
  stageId: number
  stageName: string
  expectedAmount: number
  expectedCloseDate: string
  ownerName: string
  winProbability: number
}

export interface FollowUp {
  id: number
  customerId: number
  type: string
  content: string
  creatorName: string
  createdAt: string
}

export interface Contract {
  id: number
  contractNo: string
  title: string
  customerName: string
  totalAmount: number
  signStatus: string
  createdAt: string
}
