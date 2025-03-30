import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import eventBus from './utils/eventBus'
import { initToastBus } from './utils/toast'

// 导入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 初始化Toast事件总线
initToastBus(eventBus)

// 创建Vue应用实例
const app = createApp(App)

// 使用路由
app.use(router)
// 使用Element Plus
app.use(ElementPlus)

// 将事件总线添加到全局属性
app.config.globalProperties.$eventBus = eventBus

// 挂载应用
app.mount('#app')
