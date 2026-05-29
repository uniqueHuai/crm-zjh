<template>
  <div class="app-layout">
    <Sidebar :collapsed="sidebarCollapsed" />
    <div class="app-main" :class="{ collapsed: sidebarCollapsed }">
      <Navbar @toggle-sidebar="toggleSidebar" />
      <div class="app-content">
        <router-view />
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
}
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 220px;
  transition: margin-left 0.3s;
  &.collapsed {
    margin-left: 64px;
  }
}
.app-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
