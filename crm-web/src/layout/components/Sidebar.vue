<template>
  <div class="sidebar" :class="{ collapsed }">
    <div class="sidebar-logo">
      <span class="logo-text">{{ collapsed ? 'CRM' : 'CRM 管理系统' }}</span>
    </div>
    <el-menu
      :default-active="route.path"
      :collapse="collapsed"
      :router="true"
      background-color="#001529"
      text-color="#ffffffaa"
      active-text-color="#fff"
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>工作台</template>
      </el-menu-item>

      <el-sub-menu index="/customer">
        <template #title>
          <el-icon><User /></el-icon>
          <span>客户管理</span>
        </template>
        <el-menu-item index="/customer/lead">线索管理</el-menu-item>
        <el-menu-item index="/customer/list">客户列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="/sales">
        <template #title>
          <el-icon><TrendCharts /></el-icon>
          <span>销售管理</span>
        </template>
        <el-menu-item index="/sales/pipeline">商机看板</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="/system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/user">用户管理</el-menu-item>
        <el-menu-item index="/system/role">角色管理</el-menu-item>
        <el-menu-item index="/system/menu">菜单管理</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import {
  Odometer, User, TrendCharts, Setting
} from '@element-plus/icons-vue'

defineProps<{ collapsed: boolean }>()
const route = useRoute()
</script>

<style scoped lang="scss">
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 220px;
  background: #001529;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.3s;
  z-index: 100;
  &.collapsed {
    width: 64px;
  }
  &-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid rgba(255,255,255,0.1);
    .logo-text {
      white-space: nowrap;
      overflow: hidden;
    }
  }
}
.el-menu {
  border-right: none;
}
</style>
