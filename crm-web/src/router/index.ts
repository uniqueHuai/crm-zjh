import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import Cookies from 'js-cookie'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: { title: '首页' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台' }
      }
    ]
  },
  {
    path: '/customer',
    component: () => import('@/layout/index.vue'),
    meta: { title: '客户管理' },
    children: [
      {
        path: 'lead',
        name: 'Lead',
        component: () => import('@/views/customer/lead/index.vue'),
        meta: { title: '线索管理' }
      },
      {
        path: 'list',
        name: 'CustomerList',
        component: () => import('@/views/customer/list/index.vue'),
        meta: { title: '客户列表' }
      },
      {
        path: 'contact',
        name: 'CustomerContact',
        component: () => import('@/views/customer/contact/index.vue'),
        meta: { title: '联系人管理' }
      },
      {
        path: 'tag',
        name: 'CustomerTag',
        component: () => import('@/views/customer/tag/index.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'segment',
        name: 'CustomerSegment',
        component: () => import('@/views/customer/segment/index.vue'),
        meta: { title: '客户分群' }
      }
    ]
  },
  {
    path: '/sales',
    component: () => import('@/layout/index.vue'),
    meta: { title: '销售管理' },
    children: [
      {
        path: 'pipeline',
        name: 'Pipeline',
        component: () => import('@/views/sales/pipeline/index.vue'),
        meta: { title: '商机看板' }
      },
      {
        path: 'opportunity',
        name: 'Opportunity',
        component: () => import('@/views/sales/opportunity/index.vue'),
        meta: { title: '商机列表' }
      },
      {
        path: 'contract',
        name: 'Contract',
        component: () => import('@/views/sales/contract/index.vue'),
        meta: { title: '合同管理' }
      },
      {
        path: 'quotation',
        name: 'Quotation',
        component: () => import('@/views/sales/quotation/index.vue'),
        meta: { title: '报价管理' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/sales/product/index.vue'),
        meta: { title: '产品管理' }
      },
      {
        path: 'follow-up',
        name: 'FollowUp',
        component: () => import('@/views/sales/follow-up/index.vue'),
        meta: { title: '跟进记录' }
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('@/views/sales/appointment/index.vue'),
        meta: { title: '日程安排' }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    meta: { title: '系统管理' },
    children: [
      {
        path: 'user',
        name: 'SysUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'role',
        name: 'SysRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'menu',
        name: 'SysMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理' }
      },
      {
        path: 'dept',
        name: 'SysDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'dict',
        name: 'SysDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理' }
      },
      {
        path: 'config',
        name: 'SysConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '参数设置' }
      },
      {
        path: 'log',
        name: 'SysLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'message',
        name: 'SysMessage',
        component: () => import('@/views/system/message/index.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'api-key',
        name: 'SysApiKey',
        component: () => import('@/views/system/api-key/index.vue'),
        meta: { title: 'API密钥' }
      },
      {
        path: 'profile',
        name: 'SysProfile',
        component: () => import('@/views/system/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/collaboration',
    component: () => import('@/layout/index.vue'),
    meta: { title: '协同办公' },
    children: [
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/views/collaboration/approval/index.vue'),
        meta: { title: '审批管理' }
      },
      {
        path: 'ticket',
        name: 'Ticket',
        component: () => import('@/views/collaboration/ticket/index.vue'),
        meta: { title: '服务工单' }
      }
    ]
  },
  {
    path: '/report',
    component: () => import('@/layout/index.vue'),
    meta: { title: '数据报表' },
    children: [
      {
        path: 'dashboard',
        name: 'ReportDashboard',
        component: () => import('@/views/report/dashboard/index.vue'),
        meta: { title: '报表中心' }
      },
      {
        path: 'custom',
        name: 'CustomReport',
        component: () => import('@/views/report/custom/index.vue'),
        meta: { title: '自定义报表' }
      }
    ]
  },
  {
    path: '/mall',
    component: () => import('@/layout/index.vue'),
    meta: { title: '商城管理' },
    children: [
      {
        path: 'order',
        name: 'MallOrder',
        component: () => import('@/views/mall/order/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'coupon',
        name: 'MallCoupon',
        component: () => import('@/views/mall/coupon/index.vue'),
        meta: { title: '优惠券管理' }
      },
      {
        path: 'activity',
        name: 'MallActivity',
        component: () => import('@/views/mall/activity/index.vue'),
        meta: { title: '营销活动' }
      },
      {
        path: 'page',
        name: 'MallPage',
        component: () => import('@/views/mall/page/index.vue'),
        meta: { title: '小程序页面' }
      }
    ]
  },
  {
    path: '/ai',
    component: () => import('@/layout/index.vue'),
    meta: { title: '智能AI' },
    children: [
      {
        path: 'sales-assistant',
        name: 'AiSalesAssistant',
        component: () => import('@/views/ai/sales-assistant/index.vue'),
        meta: { title: '销售助手' }
      },
      {
        path: 'butler',
        name: 'AiButler',
        component: () => import('@/views/ai/butler/index.vue'),
        meta: { title: '智能管家' }
      },
      {
        path: 'knowledge-base',
        name: 'AiKnowledgeBase',
        component: () => import('@/views/ai/knowledge-base/index.vue'),
        meta: { title: '知识库管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫 — 未登录跳转登录页
router.beforeEach((to, _from, next) => {
  const token = Cookies.get('crm_access_token')
  if (to.meta.noAuth) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
