import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API as string || '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 — 添加 Token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 — 统一处理
request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === undefined) {
      // 非标准响应（如文件流）
      return response
    }

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // Token 过期跳转登录
      if (res.code === 401) {
        clearAuth()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }

    return res
  },
  (error: AxiosError) => {
    const status = error.response?.status
    const data = error.response?.data as { message?: string } | undefined
    const message = data?.message || error.message

    switch (status) {
      case 401:
        clearAuth()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
        break
      case 403:
        ElMessage.error('无权限访问')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器异常')
        break
      default:
        ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
