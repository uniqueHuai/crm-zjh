import router from './index'
import { useUserStore } from '@/store/modules/user'
import { getToken } from '@/utils/auth'

const whiteList = ['/login']

router.beforeEach(async (to, _from, next) => {
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      const userStore = useUserStore()
      if (!userStore.userInfo) {
        try {
          await userStore.getUserInfo()
          next()
        } catch {
          userStore.logout()
          next({ path: '/login' })
        }
      } else {
        next()
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next({ path: '/login' })
    }
  }
})
