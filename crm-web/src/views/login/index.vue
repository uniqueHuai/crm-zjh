<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-bg-grid" />
      <div class="login-bg-shapes">
        <div class="shape shape-1" />
        <div class="shape shape-2" />
        <div class="shape shape-3" />
        <div class="shape shape-4" />
      </div>
    </div>

    <div class="login-container">
      <div class="login-brand">
        <div class="brand-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="currentColor" fill-opacity="0.15"/>
            <path d="M14 28V20L24 14L34 20V28L24 34L14 28Z" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linejoin="round"/>
            <circle cx="24" cy="24" r="4" fill="currentColor"/>
            <line x1="24" y1="14" x2="24" y2="10" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
            <line x1="24" y1="34" x2="24" y2="38" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="brand-title">CRM 管理系统</h1>
        <p class="brand-desc">全渠道客户关系管理平台</p>
      </div>

      <div class="login-card">
        <div class="login-card-header">
          <h2 class="login-title">账号登录</h2>
          <p class="login-subtitle">欢迎回来，请登录您的账号</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="UserFilled"
              class="login-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              class="login-input"
            />
          </el-form-item>

          <el-form-item prop="captchaCode" v-if="captchaEnabled">
            <div class="captcha-row">
              <el-input
                v-model="form.captchaCode"
                placeholder="验证码"
                class="captcha-input"
                maxlength="6"
              />
              <img
                class="captcha-img"
                :src="captchaImage || '/captcha-placeholder.svg'"
                @click="refreshCaptcha"
                alt="验证码"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-button link type="primary" size="small">忘记密码？</el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="footer-text">Copyright &copy; {{ year }} CRM System. All rights reserved.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const year = new Date().getFullYear()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaEnabled = ref(false)
const captchaImage = ref('')
const rememberMe = ref(true)

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaId: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名至少2位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码至少4位', trigger: 'blur' },
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
}

function refreshCaptcha() {
  // TODO: call captcha API
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password,
      captchaId: form.captchaId || undefined,
      captchaCode: form.captchaCode || undefined,
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // handled by interceptor
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--crm-bg);
  position: relative;
  overflow: hidden;
}

/* ---- Background ---- */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;

  &-grid {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(26, 115, 232, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(26, 115, 232, 0.03) 1px, transparent 1px);
    background-size: 60px 60px;
  }

  .shape {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.12;
  }
  .shape-1 {
    width: 500px; height: 500px;
    background: var(--crm-primary);
    top: -150px; left: -150px;
    animation: float 12s ease-in-out infinite;
  }
  .shape-2 {
    width: 350px; height: 350px;
    background: var(--crm-success);
    bottom: -100px; right: -80px;
    animation: float 15s ease-in-out infinite reverse;
  }
  .shape-3 {
    width: 250px; height: 250px;
    background: var(--crm-warning);
    top: 40%; left: 55%;
    animation: float 10s ease-in-out infinite;
  }
  .shape-4 {
    width: 200px; height: 200px;
    background: var(--crm-danger);
    top: 20%; left: 20%;
    animation: float 14s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-40px) scale(1.05); }
}

/* ---- Layout ---- */
.login-container {
  display: flex;
  align-items: center;
  gap: 80px;
  position: relative;
  z-index: 1;
}

.login-brand {
  text-align: right;
  .brand-icon {
    display: inline-flex;
    color: var(--crm-primary);
    margin-bottom: 20px;
  }
  .brand-title {
    font-size: 36px;
    font-weight: 700;
    color: var(--crm-text-primary);
    margin: 0 0 8px;
    letter-spacing: 1px;
  }
  .brand-desc {
    font-size: var(--crm-font-size-base);
    color: var(--crm-text-secondary);
    margin: 0;
  }
}

/* ---- Login Card ---- */
.login-card {
  width: 420px;
  padding: 40px;
  background: var(--crm-bg-white);
  border-radius: var(--crm-radius-xl);
  box-shadow: var(--crm-shadow-lg);
  border: 1px solid var(--crm-border);
}

.login-card-header {
  margin-bottom: 28px;
  .login-title {
    margin: 0 0 6px;
    font-size: 22px;
    font-weight: 700;
    color: var(--crm-text-primary);
  }
  .login-subtitle {
    margin: 0;
    font-size: var(--crm-font-size-sm);
    color: var(--crm-text-secondary);
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;
  }
}

.login-input {
  :deep(.el-input__wrapper) {
    padding: 4px 16px;
    border-radius: var(--crm-radius-md);
    box-shadow: 0 0 0 1px var(--crm-border) inset;
    transition: box-shadow var(--crm-transition-fast);
    &:hover {
      box-shadow: 0 0 0 1px var(--crm-primary-light) inset;
    }
    &.is-focus {
      box-shadow: 0 0 0 2px var(--crm-primary) inset;
    }
  }
  :deep(.el-input__prefix) {
    margin-right: 8px;
    .el-icon { color: var(--crm-text-secondary); }
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
  .captcha-input {
    flex: 1;
  }
  .captcha-img {
    width: 120px;
    height: 40px;
    border-radius: var(--crm-radius-sm);
    cursor: pointer;
    border: 1px solid var(--crm-border);
    object-fit: cover;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  :deep(.el-checkbox__label) {
    font-size: var(--crm-font-size-sm);
    color: var(--crm-text-regular);
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: var(--crm-font-size-base);
  border-radius: var(--crm-radius-md);
  font-weight: 500;
  letter-spacing: 2px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  .footer-text {
    font-size: var(--crm-font-size-xs);
    color: var(--crm-text-placeholder);
  }
}
</style>
