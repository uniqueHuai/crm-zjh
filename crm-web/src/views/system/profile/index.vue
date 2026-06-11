<template>
  <div class="profile-page" v-loading="loading">
    <!-- 个人信息卡片 -->
    <div class="card profile-header-card">
      <div class="profile-avatar-section">
        <el-avatar :size="80" :icon="UserFilled" :src="userInfo.avatar" class="profile-avatar" />
        <div class="profile-name-section">
          <h2 class="profile-name">{{ userInfo.realName || userInfo.username }}</h2>
          <span class="profile-role">{{ userInfo.roles?.join('、') || '普通用户' }}</span>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="14">
        <div class="card">
          <div class="card-header">
            <h3>基本信息</h3>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属部门">{{ userInfo.deptName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="r in userInfo.roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
              <span v-if="!userInfo.roles?.length">-</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="10">
        <div class="card">
          <div class="card-header">
            <h3>快捷操作</h3>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="openPasswordDialog">
              <el-icon :size="22"><Lock /></el-icon>
              <span>修改密码</span>
            </div>
          </div>
        </div>

        <div class="card" style="margin-top:16px">
          <div class="card-header">
            <h3>登录信息</h3>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="最后登录">{{ userInfo.lastLoginAt || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>

    <ChangePasswordDialog v-model="passwordVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { UserFilled, Lock } from '@element-plus/icons-vue'
import { getUserInfoApi } from '@/api/modules/auth'
import ChangePasswordDialog from './components/ChangePasswordDialog.vue'

const loading = ref(false)
const passwordVisible = ref(false)

const userInfo = reactive({
  username: '',
  realName: '',
  avatar: '',
  deptName: '',
  email: '',
  phone: '',
  roles: [] as string[],
  lastLoginAt: '',
})

function openPasswordDialog() {
  passwordVisible.value = true
}

async function fetchProfile() {
  loading.value = true
  try {
    const res = await getUserInfoApi()
    if (res?.data) {
      Object.assign(userInfo, res.data)
    }
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}

onMounted(fetchProfile)
</script>

<style scoped lang="scss">
.profile-page { max-width: 1100px; }

.profile-header-card {
  margin-bottom: 20px;
  .profile-avatar-section {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  .profile-avatar {
    flex-shrink: 0;
    border: 3px solid var(--el-color-primary-light-5);
  }
  .profile-name-section {
    h2 { margin: 0 0 4px; font-size: 22px; color: var(--crm-text-primary); }
    .profile-role { font-size: 13px; color: var(--crm-text-secondary); }
  }
}

.card {
  background: var(--crm-bg-white);
  border-radius: var(--crm-radius-lg);
  border: 1px solid var(--crm-border);
  padding: 20px;
}
.card-header {
  margin-bottom: 16px;
  h3 { margin: 0; font-size: 16px; color: var(--crm-text-primary); }
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  .action-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 16px;
    border-radius: var(--crm-radius-md);
    cursor: pointer;
    color: var(--crm-text-primary);
    transition: background 0.2s;
    &:hover { background: var(--crm-bg-gray); }
    span { font-size: 14px; }
  }
}
</style>
