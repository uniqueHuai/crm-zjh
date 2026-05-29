/** 校验手机号 */
export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/** 校验邮箱 */
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

/** 校验 URL */
export function isValidUrl(url: string): boolean {
  return /^https?:\/\/.+/.test(url)
}
