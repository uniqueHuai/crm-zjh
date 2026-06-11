<template>
  <div class="sidebar" :class="{ collapsed }">
    <div class="sidebar-logo">
      <el-icon v-if="collapsed" :size="28" class="logo-icon"><Coin /></el-icon>
      <span v-else class="logo-text">CRM 管理系统</span>
    </div>
    <el-scrollbar class="sidebar-menu">
      <el-menu
        :default-active="route.path"
        :default-openeds="openedMenus"
        :collapse="collapsed"
        :router="true"
        :collapse-transition="false"
        background-color="transparent"
        text-color="var(--crm-sidebar-text)"
        active-text-color="var(--crm-sidebar-text-active)"
        @open="handleMenuOpen"
        @close="handleMenuClose"
      >
        <template v-for="menu in menuItems" :key="menu.index">
          <el-menu-item v-if="!menu.children" :index="menu.index">
            <el-icon><component :is="menu.icon" /></el-icon>
            <template #title>{{ menu.title }}</template>
          </el-menu-item>
          <el-sub-menu v-else :index="menu.index">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.index"
              :index="child.index"
            >
              <el-icon><component :is="child.icon || 'Menu'" /></el-icon>
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  Coin, Odometer, User, TrendCharts, Setting,
  Tickets, Files, ShoppingCart, ChatDotSquare, DataAnalysis, Menu as MenuIcon,
  MagicStick, Promotion,
} from '@element-plus/icons-vue'

defineProps<{ collapsed: boolean }>()

const route = useRoute()

const STORAGE_KEY = 'sidebar-opened-menus'
const openedMenus = ref<string[]>(
  JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
)

function handleMenuOpen(index: string) {
  if (!openedMenus.value.includes(index)) {
    openedMenus.value.push(index)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(openedMenus.value))
  }
}

function handleMenuClose(index: string) {
  openedMenus.value = openedMenus.value.filter(i => i !== index)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(openedMenus.value))
}

interface MenuItem {
  index: string
  title: string
  icon: any
  children?: { index: string; title: string; icon?: any }[]
}

const menuItems: MenuItem[] = [
  { index: '/dashboard', title: '工作台', icon: Odometer },
  {
    index: '/customer', title: '客户管理', icon: User,
    children: [
      { index: '/customer/lead', title: '线索管理', icon: MenuIcon },
      { index: '/customer/list', title: '客户列表', icon: MenuIcon },
      { index: '/customer/contact', title: '联系人管理', icon: MenuIcon },
      { index: '/customer/tag', title: '标签管理', icon: MenuIcon },
      { index: '/customer/segment', title: '客户分群', icon: MenuIcon },
    ],
  },
  {
    index: '/sales', title: '销售管理', icon: TrendCharts,
    children: [
      { index: '/sales/pipeline', title: '商机看板', icon: MenuIcon },
      { index: '/sales/opportunity', title: '商机列表', icon: MenuIcon },
      { index: '/sales/contract', title: '合同管理', icon: Files },
      { index: '/sales/quotation', title: '报价管理', icon: Tickets },
      { index: '/sales/product', title: '产品管理', icon: MenuIcon },
    ],
  },
  {
    index: '/mall', title: '商城管理', icon: ShoppingCart,
    children: [
      { index: '/mall/order', title: '订单管理', icon: MenuIcon },
      { index: '/mall/coupon', title: '优惠券管理', icon: MenuIcon },
      { index: '/mall/activity', title: '营销活动', icon: MenuIcon },
      { index: '/mall/page', title: '小程序页面', icon: MenuIcon },
    ],
  },
  {
    index: '/collaboration', title: '协同办公', icon: ChatDotSquare,
    children: [
      { index: '/collaboration/approval', title: '审批管理', icon: MenuIcon },
      { index: '/collaboration/ticket', title: '服务工单', icon: MenuIcon },
    ],
  },
  {
    index: '/report', title: '数据报表', icon: DataAnalysis,
    children: [
      { index: '/report/dashboard', title: '报表中心', icon: MenuIcon },
      { index: '/report/custom', title: '自定义报表', icon: MenuIcon },
    ],
  },
  {
    index: '/ai', title: '智能AI', icon: MagicStick,
    children: [
      { index: '/ai/sales-assistant', title: '销售助手', icon: MenuIcon },
      { index: '/ai/butler', title: '智能管家', icon: Promotion },
      { index: '/ai/knowledge-base', title: '知识库管理', icon: MenuIcon },
    ],
  },
  {
    index: '/system', title: '系统管理', icon: Setting,
    children: [
      { index: '/system/user', title: '用户管理', icon: MenuIcon },
      { index: '/system/role', title: '角色管理', icon: MenuIcon },
      { index: '/system/menu', title: '菜单管理', icon: MenuIcon },
      { index: '/system/dept', title: '部门管理', icon: MenuIcon },
      { index: '/system/dict', title: '字典管理', icon: MenuIcon },
      { index: '/system/config', title: '参数设置', icon: MenuIcon },
    ],
  },
]
</script>

<style scoped lang="scss">
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--crm-sidebar-width);
  background: var(--crm-sidebar-bg);
  transition: width var(--crm-transition-base);
  z-index: 100;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.collapsed {
    width: var(--crm-sidebar-collapsed-width);
  }

  &-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--crm-text-white);
    font-size: 18px;
    font-weight: 700;
    flex-shrink: 0;
    letter-spacing: 0.5px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    .logo-icon {
      color: var(--crm-primary-light);
    }
    .logo-text {
      white-space: nowrap;
    }
  }

  &-menu {
    flex: 1;
    padding: var(--crm-space-sm) 0;
    :deep(.el-menu) {
      border-right: none;
      background: transparent !important;
    }
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 44px;
      line-height: 44px;
      margin: 2px var(--crm-space-sm);
      border-radius: var(--crm-radius-md);
      transition: all var(--crm-transition-fast);
      &:hover {
        background: var(--crm-sidebar-bg-hover) !important;
      }
      &.is-active {
        background: var(--crm-primary) !important;
        color: var(--crm-text-white) !important;
      }
    }
    :deep(.el-sub-menu) {
      .el-menu {
        padding-left: 0;
        .el-menu-item {
          padding-left: 52px !important;
          height: 40px;
          line-height: 40px;
          &.is-active {
            background: var(--crm-primary) !important;
          }
        }
      }
    }
  }
}
</style>
