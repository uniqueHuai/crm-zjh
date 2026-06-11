<template>
  <div class="navbar">
    <div class="navbar-left">
      <el-icon class="toggle-btn" @click="$emit('toggle-sidebar')" :size="20">
        <Fold />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumb" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="navbar-right">
      <el-tooltip content="消息通知">
        <span class="navbar-badge-wrap">
          <el-button :text="true" @click="$router.push('/system/message')">
            <el-icon :size="18"><Bell /></el-icon>
          </el-button>
          <sup v-if="unreadCount > 0" class="navbar-badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </sup>
        </span>
      </el-tooltip>
      <el-divider direction="vertical" />
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="30" :icon="UserFilled" />
          <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
  <ChangePasswordDialog v-model="passwordVisible" />
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, UserFilled, ArrowDown, Bell, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { useAppStore } from '@/store/modules/app'
import ChangePasswordDialog from '@/views/system/profile/components/ChangePasswordDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const route = useRoute()

const breadcrumb = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({ path: r.path, title: r.meta.title as string }))
})

const unreadCount = computed(() => appStore.unreadCount)
const passwordVisible = ref(false)

let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  appStore.fetchUnreadCount()
  pollTimer = setInterval(() => appStore.fetchUnreadCount(), 30_000)
  document.addEventListener('visibilitychange', onVisibilityChange)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  document.removeEventListener('visibilitychange', onVisibilityChange)
})

function onVisibilityChange() {
  if (document.visibilityState === 'visible') {
    appStore.fetchUnreadCount()
  }
}

async function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/system/profile')
  } else if (command === 'password') {
    passwordVisible.value = true
  } else if (command === 'logout') {
    await userStore.logout()
  }
}
</script>

<style scoped lang="scss">
.navbar {
  height: var(--crm-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--crm-space-lg);
  background: var(--crm-header-bg);
  border-bottom: 1px solid var(--crm-border);
  flex-shrink: 0;

  &-left {
    display: flex;
    align-items: center;
    gap: var(--crm-space-base);
  }
  &-right {
    display: flex;
    align-items: center;
    gap: var(--crm-space-sm);
  }
  .toggle-btn {
    cursor: pointer;
    color: var(--crm-text-regular);
    transition: color var(--crm-transition-fast);
    &:hover { color: var(--crm-primary); }
  }
  .navbar-badge-wrap {
    position: relative;
    display: inline-flex;
    align-items: center;
  }
  .navbar-badge {
    position: absolute;
    top: 4px;
    right: 2px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: 9px;
    background: var(--el-color-danger);
    color: #fff;
    font-size: 12px;
    line-height: 18px;
    white-space: nowrap;
    pointer-events: none;
    z-index: 1;
  }
  .user-info {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: var(--crm-radius-md);
    transition: background var(--crm-transition-fast);
    &:hover {
      background: var(--crm-bg-gray);
    }
    .username {
      font-size: var(--crm-font-size-sm);
      color: var(--crm-text-primary);
      max-width: 100px;
      @extend %ellipsis;
    }
  }
}
%ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
