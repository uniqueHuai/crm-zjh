import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo, LoginRequest } from '@/types'
import { loginApi, getUserInfoApi, logoutApi } from '@/api/modules/auth'
import { setToken, setRefreshToken, getToken, clearAuth } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string | undefined>(getToken())
  const permissions = ref<string[]>([])

  /** 登录 */
  async function login(loginData: LoginRequest) {
    const res = await loginApi(loginData)
    const data = res.data
    token.value = data.accessToken
    setToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    userInfo.value = data.userInfo
    permissions.value = data.userInfo.permissions || []
  }

  /** 获取用户信息 */
  async function getUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    permissions.value = res.data.permissions || []
    return res.data
  }

  /** 退出登录 */
  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 忽略退出接口异常
    }
    clearAuth()
    userInfo.value = null
    token.value = undefined
    permissions.value = []
    router.push('/login')
  }

  /** 是否有权限 */
  function hasPermission(perm: string): boolean {
    if (permissions.value.includes('*:*:*')) return true
    return permissions.value.includes(perm)
  }

  return {
    userInfo,
    token,
    permissions,
    login,
    getUserInfo,
    logout,
    hasPermission
  }
})
