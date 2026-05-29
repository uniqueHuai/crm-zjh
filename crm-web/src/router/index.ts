import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
