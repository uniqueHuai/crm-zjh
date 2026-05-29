import Cookies from 'js-cookie'

const TOKEN_KEY = 'crm_access_token'
const REFRESH_TOKEN_KEY = 'crm_refresh_token'
const USER_INFO_KEY = 'crm_user_info'

export function getToken(): string | undefined {
  return Cookies.get(TOKEN_KEY)
}

export function setToken(token: string): void {
  Cookies.set(TOKEN_KEY, token, { expires: 7 })
}

export function removeToken(): void {
  Cookies.remove(TOKEN_KEY)
}

export function getRefreshToken(): string | undefined {
  return Cookies.get(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string): void {
  Cookies.set(REFRESH_TOKEN_KEY, token, { expires: 30 })
}

export function removeRefreshToken(): void {
  Cookies.remove(REFRESH_TOKEN_KEY)
}

export function getUserInfo(): Record<string, unknown> | null {
  const raw = localStorage.getItem(USER_INFO_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUserInfo(info: Record<string, unknown>): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

export function clearAuth(): void {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
}
