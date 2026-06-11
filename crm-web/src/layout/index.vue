<template>
  <div class="app-layout">
    <Sidebar :collapsed="sidebarCollapsed" />
    <div class="app-main" :class="{ collapsed: sidebarCollapsed }">
      <Navbar @toggle-sidebar="toggleSidebar" />
      <div class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'

const sidebarCollapsed = ref(false)
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--crm-bg);
}
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: var(--crm-sidebar-width);
  transition: margin-left var(--crm-transition-base);
  min-width: 0;
  &.collapsed {
    margin-left: var(--crm-sidebar-collapsed-width);
  }
}
.app-content {
  flex: 1;
  padding: var(--crm-space-lg);
  overflow-y: auto;
  background: var(--crm-bg);
}
</style>
