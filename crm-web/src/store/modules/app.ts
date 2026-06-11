import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/modules/system'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const breadcrumb = ref<{ path: string; title: string }[]>([])
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      if (res?.data) {
        unreadCount.value = res.data.total ?? 0
      }
    } catch { /* handled by interceptor */ }
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setBreadcrumb(items: { path: string; title: string }[]) {
    breadcrumb.value = items
  }

  return {
    sidebarCollapsed,
    breadcrumb,
    unreadCount,
    fetchUnreadCount,
    toggleSidebar,
    setBreadcrumb
  }
})
