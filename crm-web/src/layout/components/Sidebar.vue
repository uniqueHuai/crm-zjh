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
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <template v-for="menu in menuTree" :key="menu.id">
          <el-sub-menu v-if="menu.children?.length" :index="menu.routePath!">
            <template #title>
              <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.routePath!">
              <el-icon><component :is="child.icon || 'Menu'" /></el-icon>
              <template #title>{{ child.name }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Coin, Odometer, Menu as MenuIcon } from '@element-plus/icons-vue'
import { getMenuTree } from '@/api/modules/system/menu'
import type { SysMenuNode } from '@/api/modules/system/menu'

defineProps<{ collapsed: boolean }>()

const route = useRoute()

const STORAGE_KEY = 'sidebar-opened-menus'
const openedMenus = ref<string[]>(
  JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
)
const menuTree = ref<SysMenuNode[]>([])

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

onMounted(async () => {
  try {
    const res = await getMenuTree()
    const all = res.data?.records || []
    const roots = all.filter((n: SysMenuNode) => n.menuType === 'M' && n.status === 1)
    menuTree.value = roots.map((root: SysMenuNode) => ({
      ...root,
      children: (root.children || []).filter(
        (c: SysMenuNode) => c.menuType === 'C' && c.status === 1
      )
    }))
  } catch {
    // API 不可用时静默降级
  }
})
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
