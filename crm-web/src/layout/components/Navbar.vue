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
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="28" :icon="UserFilled" />
          <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Fold, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { useAppStore } from '@/store/modules/app'

defineEmits<{ 'toggle-sidebar': [] }>()

const userStore = useUserStore()
const appStore = useAppStore()
const route = useRoute()

const breadcrumb = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title)
  return matched.map(r => ({ path: r.path, title: r.meta.title as string }))
})

async function handleCommand(command: string) {
  if (command === 'logout') {
    await userStore.logout()
  }
}
</script>

<style scoped lang="scss">
.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  z-index: 10;
  &-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  &-right {
    display: flex;
    align-items: center;
  }
  .toggle-btn {
    cursor: pointer;
    &:hover { color: #409eff; }
  }
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    .username {
      font-size: 14px;
    }
  }
}
</style>
