import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const breadcrumb = ref<{ path: string; title: string }[]>([])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setBreadcrumb(items: { path: string; title: string }[]) {
    breadcrumb.value = items
  }

  return {
    sidebarCollapsed,
    breadcrumb,
    toggleSidebar,
    setBreadcrumb
  }
})
