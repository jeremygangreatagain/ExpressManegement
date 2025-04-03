<template>
  <div class="login-container">
    <!-- 左侧表单区域 -->
    <div class="form-container" :class="{ 'slide-right': !isLogin }">
      <div class="form-box">
        <!-- 登录表单 -->
        <div v-if="isLogin" class="form-content">
          <h2 class="form-title">登录</h2>
          <form @submit.prevent="handleLogin">
            <!-- 用户名 -->
            <div class="form-group">
              <label for="loginUsername">用户名</label>
              <input
                id="loginUsername"
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                required
              />
            </div>
            
            <!-- 密码 -->
            <div class="form-group">
              <label for="loginPassword">密码</label>
              <input
                id="loginPassword"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
                required
              />
            </div>
            
            <!-- 验证码 -->
            <div class="form-group">
              <label for="loginCaptcha">验证码</label>
              <div class="captcha-container">
                <input
                  id="loginCaptcha"
                  v-model="loginForm.captcha"
                  type="text"
                  placeholder="请输入验证码"
                  required
                />
                <div class="captcha-img" @click="refreshCaptcha">
                  <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
                  <span v-else>加载中...</span>
                </div>
              </div>
            </div>
            
            <!-- 记住我 -->
            <div class="form-checkbox">
              <input
                id="rememberMe"
                v-model="loginForm.rememberMe"
                type="checkbox"
              />
              <label for="rememberMe">记住我</label>
            </div>
            
            <!-- 登录按钮 -->
            <button
              type="submit"
              class="submit-btn"
              :disabled="isLoading"
            >
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
            
            <!-- 切换到注册 -->
            <div class="form-switch">
              <p>还没有账号？<a @click="toggleForm">立即注册</a></p>
            </div>
          </form>
        </div>
        
        <!-- 注册表单 -->
        <div v-else class="form-content">
          <h2 class="form-title">注册</h2>
          <form @submit.prevent="handleRegister">
            <!-- 用户名 -->
            <div class="form-group">
              <label for="registerUsername">用户名</label>
              <div class="input-with-status">
                <input
                  id="registerUsername"
                  v-model="registerForm.username"
                  type="text"
                  placeholder="请输入用户名"
                  @blur="checkUsernameAvailability"
                  required
                />
                <span v-if="usernameChecked" class="status-icon">
                  <i v-if="usernameAvailable" class="icon-success">✓</i>
                  <i v-else class="icon-error">✗</i>
                </span>
              </div>
              <p v-if="usernameChecked && !usernameAvailable" class="error-message">用户名已被占用</p>
            </div>
            
            <!-- 密码 -->
            <div class="form-group">
              <label for="registerPassword">密码</label>
              <input
                id="registerPassword"
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                autocomplete="new-password"
                required
              />
            </div>
            
            <!-- 确认密码 -->
            <div class="form-group">
              <label for="confirmPassword">确认密码</label>
              <input
                id="confirmPassword"
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                autocomplete="new-password"
                required
              />
              <p v-if="passwordMismatch" class="error-message">两次输入的密码不一致</p>
            </div>
            
            <!-- 验证码 -->
            <div class="form-group">
              <label for="registerCaptcha">验证码</label>
              <div class="captcha-container">
                <input
                  id="registerCaptcha"
                  v-model="registerForm.captcha"
                  type="text"
                  placeholder="请输入验证码"
                  required
                />
                <div class="captcha-img" @click="refreshCaptcha">
                  <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
                  <span v-else>加载中...</span>
                </div>
              </div>
            </div>
            
            <!-- 注册按钮 -->
            <button
              type="submit"
              class="submit-btn"
              :disabled="isLoading || !usernameAvailable || passwordMismatch"
            >
              {{ isLoading ? '注册中...' : '注册' }}
            </button>
            
            <!-- 切换到登录 -->
            <div class="form-switch">
              <p>已有账号？<a @click="toggleForm">立即登录</a></p>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 右侧背景区域 -->
    <div class="bg-container" :class="{ 'slide-left': !isLogin }">
      <div class="bg-content">
        <h2>{{ isLogin ? '还没有账号？' : '已经有账号了？' }}</h2>
        <p>{{ isLogin ? '立即注册，开始使用快递管理系统' : '立即登录，畅享快递管理系统' }}</p>
        <button @click="toggleForm" class="switch-btn">
          {{ isLogin ? '立即注册' : '立即登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue' // Removed inject as it wasn't used
import { useRouter } from 'vue-router'
import { login, register, checkUsername, getCaptcha } from '../api/auth'
import toast from '../utils/toast'
import { useUserStore } from '../stores/user' // Import useUserStore

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const userStore = useUserStore() // Get user store instance
    const isLogin = ref(true)
    const isLoading = ref(false)
    const captchaUrl = ref('')
    
    // 登录表单数据
    const loginForm = ref({
      username: '',
      password: '',
      captcha: '',
      rememberMe: false
    })
    
    // 注册表单数据
    const registerForm = ref({
      username: '',
      password: '',
      confirmPassword: '',
      captcha: ''
    })
    
    // 用户名检查状态
    const usernameChecked = ref(false)
    const usernameAvailable = ref(true)
    
    // 密码匹配检查
    const passwordMismatch = computed(() => {
      return registerForm.value.confirmPassword && 
             registerForm.value.password !== registerForm.value.confirmPassword
    })
    
    // 切换登录/注册表单
    const toggleForm = () => {
      isLogin.value = !isLogin.value
      refreshCaptcha()
    }
    
    // 刷新验证码
    const refreshCaptcha = async () => {
      console.log('开始刷新验证码')
      // 设置加载状态
      const isInitialLoad = !captchaUrl.value;
      captchaUrl.value = ''; // 清空验证码URL，显示加载中状态
      
      try {
        console.log('发送验证码请求')
        const res = await getCaptcha()
        console.log('验证码响应数据:', res) // 添加日志输出
        
        if (!res) {
          console.error('验证码响应为空')
          throw new Error('验证码请求响应为空')
        }
        
        if (!res.data) {
          console.error('验证码数据为空')
          throw new Error('验证码数据为空')
        }
        
        console.log('验证码数据类型:', typeof res.data)
        
        // 处理不同类型的验证码数据
        if (res.data.captchaId && res.data.captchaImage) {
          // 后端返回的标准格式：包含captchaId和captchaImage
          console.log('使用标准格式验证码数据')
          localStorage.setItem('captchaId', res.data.captchaId);
          captchaUrl.value = res.data.captchaImage;
          console.log('已保存captchaId:', res.data.captchaId)
        } else if (typeof res.data === 'string') {
          console.log('处理字符串格式验证码数据')
          if (res.data.startsWith('/9j/') || res.data.startsWith('iVBOR') || res.data.startsWith('PHN2')) {
            captchaUrl.value = `data:image/png;base64,${res.data}`
            console.log('设置为base64图片')
          } else if (!res.data.startsWith('data:') && !res.data.startsWith('http')) {
            // 确保路径正确
            captchaUrl.value = `${window.location.origin}/api${res.data.startsWith('/') ? '' : '/'}${res.data}`
            console.log('设置为相对路径图片:', captchaUrl.value)
          } else {
            captchaUrl.value = res.data
            console.log('设置为原始URL图片')
          }
        } else if (res.data.image) {
          console.log('使用image字段验证码数据')
          captchaUrl.value = res.data.image
        } else {
          console.log('使用原始验证码数据:', res.data)
          captchaUrl.value = res.data
        }
        
        if (!captchaUrl.value) {
          console.error('验证码URL设置失败')
          throw new Error('验证码URL设置失败')
        }
        
        console.log('验证码刷新成功，URL:', captchaUrl.value)
      } catch (error) {
        console.error('获取验证码失败:', error)
        console.error('错误详情:', error.response || error.message || error)
        captchaUrl.value = ''; // 确保清空验证码URL
        // 只有在非初始加载时才显示错误提示
        if (!isInitialLoad) {
          toast.error(`获取验证码失败: ${error.message}`);
        }
      }
    }
    
    // 检查用户名是否可用
    const checkUsernameAvailability = async () => {
      if (!registerForm.value.username) return
      
      try {
        const res = await checkUsername(registerForm.value.username)
        usernameAvailable.value = res.data
        usernameChecked.value = true
      } catch (error) {
        console.error('检查用户名失败:', error)
      }
    }
    
    // 处理登录
    const handleLogin = async () => {
      console.log('登录按钮被点击')
      toast.info('正在登录...')
      isLoading.value = true
      
      try {
        // 检查表单数据
        console.log('登录表单数据:', {
          username: loginForm.value.username,
          password: '******', // 不输出实际密码
          captchaId: localStorage.getItem('captchaId'),
          captchaCode: loginForm.value.captcha
        })
        
        const res = await login({
          username: loginForm.value.username,
          password: loginForm.value.password,
          captchaId: localStorage.getItem('captchaId'),
          captchaCode: loginForm.value.captcha
        })
        
        console.log('登录成功，响应数据:', res)
        
        // 保存token和用户角色
        localStorage.setItem('token', res.data.token)
        
        // 标准化角色格式并保存
        let roleValue = res.data.role
        if (roleValue === 1 || roleValue === '1') {
          roleValue = 'ADMIN'
        } else if (roleValue === 2 || roleValue === '2') {
          roleValue = 'STAFF'
          
          // 如果是员工角色，保存员工信息到localStorage并更新store
          if (res.data.userInfo) {
            const userInfo = res.data.userInfo;
            console.log('[Login] Received userInfo from backend:', JSON.stringify(userInfo)); // Log received userInfo
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
            userStore.setUserInfo(userInfo); // Explicitly update the store
            console.log('[Login] Staff userInfo saved to localStorage and Pinia store:', userStore.userInfo); // Log store state AFTER setting
          } else {
             console.warn('[Login] Backend login response did not contain userInfo field for STAFF role.');
             // 如果后端没返回userInfo，尝试从顶层获取基本信息更新store (确保至少有基本信息)
             const basicUserInfo = { 
                id: res.data.id, // Assuming id might be at top level
                username: loginForm.value.username, 
                role: roleValue, 
                // storeId might be missing here if not in userInfo
             };
             userStore.setUserInfo(basicUserInfo); 
             console.warn('[Login] Staff userInfo not found in response, updated store with basic info.');
          }
        } else if (roleValue === 3 || roleValue === '3') {
          roleValue = 'USER'
          // 更新普通用户信息到store (如果需要的话)
          const basicUserInfo = { 
             id: res.data.id, 
             username: loginForm.value.username, 
             role: roleValue 
          };
          userStore.setUserInfo(basicUserInfo);
          console.log('[Login] User userInfo updated in Pinia store:', basicUserInfo);
        } else if (typeof roleValue === 'string') {
          // 如果是字符串格式的ROLE_XXX，转换为XXX格式
          roleValue = roleValue.replace('ROLE_', '')
          
          // 如果是员工角色，保存员工信息到localStorage并更新store
          if (roleValue === 'STAFF' && res.data.userInfo) {
            const userInfo = res.data.userInfo;
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
            userStore.setUserInfo(userInfo); // Explicitly update the store
            console.log('[Login] Staff userInfo (ROLE_STAFF) saved to localStorage and Pinia store:', userInfo);
          } else if (roleValue === 'STAFF') {
             // 如果后端没返回userInfo，尝试从顶层获取基本信息更新store
             const basicUserInfo = { 
                id: res.data.id, 
                username: loginForm.value.username, 
                role: roleValue, 
             };
             userStore.setUserInfo(basicUserInfo); 
             console.warn('[Login] Staff userInfo (ROLE_STAFF) not found in response, updated store with basic info.');
          } else if (roleValue === 'ADMIN' || roleValue === 'USER') {
             // 更新管理员或普通用户信息到store
             const basicUserInfo = { 
                id: res.data.id, 
                username: loginForm.value.username, 
                role: roleValue 
             };
             userStore.setUserInfo(basicUserInfo);
             console.log(`[Login] ${roleValue} userInfo updated in Pinia store:`, basicUserInfo);
          }
        }
        
        localStorage.setItem('userRole', roleValue) // Keep saving role separately if needed elsewhere
        
        // 如果选择了记住我，保存用户名
        if (loginForm.value.rememberMe) {
          localStorage.setItem('rememberedUsername', loginForm.value.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }
        
        toast.success('登录成功，正在跳转...')
        
        // 根据用户角色跳转到相应的页面
        console.log('用户角色:', res.data.role)
        const role = res.data.role
        
        // 使用标准化后的角色值进行路由跳转
        let targetRoute = '/user' // 默认为用户页面
        
        if (role === 'ROLE_ADMIN' || role === 'ADMIN' || role === 1 || role === '1') {
          console.log('跳转到管理员页面')
          targetRoute = '/admin'
        } else if (role === 'ROLE_STAFF' || role === 'STAFF' || role === 2 || role === '2') {
          console.log('跳转到员工页面')
          targetRoute = '/staff'
        } else {
          // 普通用户角色或其他情况
          console.log('跳转到用户页面')
          targetRoute = '/user'
        }
        
        // 使用await确保路由跳转完成
        await router.push(targetRoute)
      } catch (error) {
        console.error('登录失败:', error)
        console.error('错误详情:', error.response || error.message || error)
        toast.error('登录失败: ' + (error.response?.data?.message || '用户名或密码错误'))
        refreshCaptcha()
      } finally {
        isLoading.value = false
      }
    }
    
    // 处理注册
    const handleRegister = async () => {
      // 检查密码是否匹配
      if (passwordMismatch.value) {
        return
      }
      
      isLoading.value = true
      
      try {
        await register({
          username: registerForm.value.username,
          password: registerForm.value.password,
          captchaId: localStorage.getItem('captchaId'),
          captchaCode: registerForm.value.captcha
        })
        
        // 注册成功后切换到登录表单
        isLogin.value = true
        
        // 将注册的用户名填充到登录表单
        loginForm.value.username = registerForm.value.username
        loginForm.value.password = ''
        
        // 清空注册表单
        registerForm.value = {
          username: '',
          password: '',
          confirmPassword: '',
          captcha: ''
        }
        
        // 显示成功消息
        toast.success('注册成功，请登录')
        
        // 刷新验证码
        refreshCaptcha()
      } catch (error) {
        console.error('注册失败:', error)
        toast.error('注册失败: ' + (error.response?.data?.message || '未知错误'))
        refreshCaptcha()
      } finally {
        isLoading.value = false
      }
    }
    
    // 页面加载时初始化
    onMounted(() => {
      // 获取验证码
      refreshCaptcha()
      
      // 检查是否有记住的用户名
      const rememberedUsername = localStorage.getItem('rememberedUsername')
      if (rememberedUsername) {
        loginForm.value.username = rememberedUsername
        loginForm.value.rememberMe = true
      }
    })
    
    return {
      isLogin,
      isLoading,
      captchaUrl,
      loginForm,
      registerForm,
      usernameChecked,
      usernameAvailable,
      passwordMismatch,
      toggleForm,
      refreshCaptcha,
      checkUsernameAvailability,
      handleLogin,
      handleRegister
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
  margin: 0;
  padding: 0;
}

.form-container {
  width: 50%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  position: absolute;
  left: 0;
  transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  z-index: 10;
}

.form-container.slide-right {
  transform: translateX(100%);
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.1);
}

.bg-container {
  width: 50%;
  height: 100%;
  position: absolute;
  right: 0;
  background: linear-gradient(135deg, #3b82f6, #1e40af);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  z-index: 5;
  overflow: hidden;
}

.bg-container.slide-left {
  transform: translateX(-100%);
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.1);
}

.form-box {
  width: 100%;
  max-width: 450px;
  padding: 2rem;
}

.form-content {
  width: 100%;
}

.form-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
  color: #3b82f6;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.captcha-container {
  display: flex;
  gap: 0.5rem;
}

.captcha-container input {
  flex: 1;
}

.captcha-img {
  width: 120px;
  height: 42px;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-checkbox {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.form-checkbox input {
  margin-right: 0.5rem;
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 0.375rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-btn:hover {
  background-color: #2563eb;
}

.submit-btn:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}

.form-switch {
  margin-top: 1.5rem;
  text-align: center;
}

.form-switch a {
  color: #3b82f6;
  cursor: pointer;
  text-decoration: underline;
}

.bg-content {
  max-width: 80%;
  text-align: center;
  transition: opacity 0.4s ease-in-out, transform 0.6s ease;
}

.bg-content h2 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.bg-content p {
  font-size: 1.125rem;
  margin-bottom: 2rem;
}

.switch-btn {
  padding: 0.75rem 1.5rem;
  background-color: transparent;
  color: white;
  border: 2px solid white;
  border-radius: 0.375rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.switch-btn:hover {
  background-color: white;
  color: #3b82f6;
}

.input-with-status {
  position: relative;
}

.status-icon {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
}

.icon-success {
  color: #10b981;
}

.icon-error {
  color: #ef4444;
}

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.form-container.slide-right .form-content,
.form-container:not(.slide-right) .form-content {
  animation: fadeIn 0.6s ease-in-out forwards;
}

.bg-container .bg-content {
  animation: slideUpFade 0.6s ease-in-out forwards;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUpFade {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 添加背景元素动画效果 */
.bg-container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
  top: -50%;
  left: -50%;
  animation: rotate 15s linear infinite;
  z-index: -1;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    height: 100vh;
    overflow-y: auto;
  }
  
  .form-container, .bg-container {
    width: 100%;
    min-height: 50vh;
    position: relative;
    left: 0;
    right: 0;
    transform: none !important;
  }

  .form-container.slide-right,
  .bg-container.slide-left {
    transform: none !important;
  }

  .form-box {
    padding: 1.5rem;
  }

  .captcha-container {
    flex-direction: column;
    gap: 0.75rem;
  }

  .captcha-img {
    width: 100%;
    height: 50px;
  }
  
  .form-container {
    order: 2;
  }
  
  .bg-container {
    order: 1;
  }
  
  .form-container.slide-right {
    transform: translateY(0);
  }
  
  .bg-container.slide-left {
    transform: translateY(0);
  }
}
</style>
