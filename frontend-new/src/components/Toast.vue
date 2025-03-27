<template>
  <transition name="toast-fade">
    <div v-if="visible" class="toast" :class="type">
      <div class="toast-content">
        <span class="toast-icon" v-if="type === 'error'">❌</span>
        <span class="toast-icon" v-else-if="type === 'success'">✓</span>
        <span class="toast-icon" v-else-if="type === 'info'">ℹ️</span>
        <span class="toast-message">{{ message }}</span>
      </div>
    </div>
  </transition>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'

export default {
  name: 'Toast',
  props: {
    message: {
      type: String,
      required: true
    },
    duration: {
      type: Number,
      default: 3000
    },
    type: {
      type: String,
      default: 'info',
      validator: (value) => ['info', 'success', 'error'].includes(value)
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const visible = ref(false)
    let timer = null

    const show = () => {
      visible.value = true
      if (props.duration > 0) {
        timer = setTimeout(() => {
          hide()
        }, props.duration)
      }
    }

    const hide = () => {
      visible.value = false
      emit('close')
    }

    onMounted(() => {
      show()
    })

    onBeforeUnmount(() => {
      if (timer) {
        clearTimeout(timer)
      }
    })

    return {
      visible
    }
  }
}
</script>

<style scoped>
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  z-index: 9999;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  min-width: 250px;
  max-width: 80%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.toast-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.toast-icon {
  margin-right: 10px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toast-message {
  font-weight: 500;
  line-height: 1.4;
}

.info {
  background-color: #3b82f6;
}

.success {
  background-color: #10b981;
}

.error {
  background-color: #ef4444;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -30px);
}
</style>