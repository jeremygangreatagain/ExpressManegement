// Toast通知工具函数

// 创建一个全局的toast事件总线
let toastEventBus = null

// 初始化toast事件总线
export function initToastBus(eventBus) {
  toastEventBus = eventBus
}

// 显示信息提示
export function showInfo(message, duration = 3000) {
  if (toastEventBus) {
    toastEventBus.emit('show-toast', { message, type: 'info', duration })
  } else {
    console.warn('Toast事件总线未初始化')
  }
}

// 显示成功提示
export function showSuccess(message, duration = 3000) {
  if (toastEventBus) {
    toastEventBus.emit('show-toast', { message, type: 'success', duration })
  } else {
    console.warn('Toast事件总线未初始化')
  }
}

// 显示错误提示
export function showError(message, duration = 3000) {
  if (toastEventBus) {
    toastEventBus.emit('show-toast', { message, type: 'error', duration })
  } else {
    console.warn('Toast事件总线未初始化')
  }
}

// 导出统一的toast对象
export default {
  info: showInfo,
  success: showSuccess,
  error: showError
}