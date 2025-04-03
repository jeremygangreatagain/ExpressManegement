// 用户状态管理
import { reactive, readonly } from 'vue'

// 创建一个简单的状态存储
const state = reactive({
  userInfo: null,
  isLoggedIn: false
})

// 创建actions来修改状态
const actions = {
  // 设置用户信息
  setUserInfo(userInfo) {
    state.userInfo = userInfo
    state.isLoggedIn = !!userInfo
  },

  // 清除用户信息（登出）
  clearUserInfo() {
    state.userInfo = null
    state.isLoggedIn = false
  },

  // 从localStorage初始化用户信息
  initUserFromStorage() {
    try {
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        const userInfo = JSON.parse(storedUser)
        this.setUserInfo(userInfo)
      }
    } catch (error) {
      console.error('Failed to initialize user from storage:', error)
    }
  }
}

// 创建并导出store
export function useUserStore() {
  // 初始化用户信息
  if (!state.userInfo) {
    actions.initUserFromStorage()
  }

  return {
    // 只读状态
    ...readonly(state),
    // 可调用的actions
    ...actions
  }
}