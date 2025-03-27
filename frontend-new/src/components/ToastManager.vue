<template>
  <div class="toast-container">
    <Toast
      v-for="(toast, index) in toasts"
      :key="index"
      :message="toast.message"
      :type="toast.type"
      :duration="toast.duration"
      @close="removeToast(index)"
    />
  </div>
</template>

<script>
import { ref, provide, onMounted, onBeforeUnmount } from 'vue'
import Toast from './Toast.vue'
import eventBus from '../utils/eventBus'

export default {
  name: 'ToastManager',
  components: {
    Toast
  },
  setup() {
    const toasts = ref([])

    // 添加新的toast
    const addToast = (message, type = 'info', duration = 3000) => {
      const toast = {
        message,
        type,
        duration
      }
      toasts.value.push(toast)
      return toasts.value.length - 1
    }

    // 移除指定的toast
    const removeToast = (index) => {
      toasts.value.splice(index, 1)
    }

    // 提供toast服务给其他组件使用
    provide('toast', {
      info: (message, duration) => addToast(message, 'info', duration),
      success: (message, duration) => addToast(message, 'success', duration),
      error: (message, duration) => addToast(message, 'error', duration)
    })
    
    // 监听事件总线上的toast事件
    const handleShowToast = (data) => {
      addToast(data.message, data.type, data.duration)
    }
    
    onMounted(() => {
      // 注册事件监听
      eventBus.on('show-toast', handleShowToast)
    })
    
    onBeforeUnmount(() => {
      // 移除事件监听
      eventBus.off('show-toast', handleShowToast)
    })

    return {
      toasts,
      addToast,
      removeToast
    }
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
  width: 100%;
  max-width: 500px;
  align-items: center;
}
</style>