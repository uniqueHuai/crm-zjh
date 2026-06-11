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
  phone?: string
  email?: string
  deptId: number
  deptName: string
  lastLoginAt?: string
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
  company?: string
  position?: string
  province?: string
  city?: string
  sourceChannel?: string
  sourceChannelName?: string
  levelId?: number
  levelName?: string
  ownerId?: number
  ownerName?: string
  tags?: { id: number; name: string }[]
  lastContactAt?: string
  remark?: string
  status?: number
  createdAt: string
}

export interface Lead {
  id: number
  name: string
  phone: string
  wechatUnionid?: string
  company?: string
  position?: string
  province?: string
  city?: string
  industry?: string
  sourceChannel: string
  sourceChannelName?: string
  status: string
  statusName?: string
  ownerId?: number
  ownerName?: string
  assignType?: string
  remark?: string
  createdAt: string
}

export interface Contact {
  id: number
  customerId: number
  customerName?: string
  name: string
  phone: string
  position: string
  email?: string
  isDecisionMaker: boolean
  birthday?: string
  remark?: string
  createdAt: string
}

/* ======== 销售 ======== */

export interface Opportunity {
  id: number
  name: string
  customerId?: number
  customerName: string
  stageId: number
  stageName: string
  expectedAmount: number
  expectedCloseDate: string
  ownerId?: number
  ownerName: string
  probability?: number
  remark?: string
  createdAt: string
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
  customerId?: number
  customerName: string
  totalAmount: number
  status: string
  ownerName?: string
  signedAt?: string
  validFrom?: string
  validUntil?: string
  createdAt: string
}

/* ======== AI 智能体 ======== */

export interface Conversation {
  id: number
  agentType: string
  title: string
  userId?: number
  customerId?: number
  status: string
  messageCount: number
  createdAt: string
  updatedAt: string
}

export interface AiMessage {
  id: number
  conversationId: number
  role: 'user' | 'assistant'
  content: string
  toolCalls?: string
  createdAt: string
}

export interface KnowledgeBase {
  id: number
  name: string
  description: string
  type: string
  status: string
  createdAt: string
  updatedAt: string
}
