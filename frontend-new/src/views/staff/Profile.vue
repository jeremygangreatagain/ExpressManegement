<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">员工个人信息</h1>
    
    <!-- 加载状态 -->
    <div v-if="isLoading" class="bg-white rounded-lg shadow-md p-6 max-w-3xl mx-auto flex justify-center items-center py-12">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mb-2"></div>
        <p class="text-gray-600">正在加载员工信息...</p>
      </div>
    </div>
    
    <div v-else class="bg-white rounded-lg shadow-md p-6 max-w-3xl mx-auto">
      <!-- 个人信息表单 -->
      <form @submit.prevent="saveProfile" class="space-y-6">
        <!-- 员工信息卡片 -->
        <div class="border-b pb-6 mb-6">
          <h2 class="text-lg font-semibold mb-4 text-gray-700">员工信息</h2>
          <div class="bg-orange-50 rounded-lg p-4 mb-4 border border-orange-100">
            <div class="flex items-center mb-3">
              <span class="material-icons text-orange-500 mr-2">badge</span>
              <span class="text-lg font-medium text-gray-800">{{ profileForm.nickname || profileForm.username }}</span>
              <span class="ml-2 px-2 py-1 bg-orange-100 text-orange-700 text-xs font-medium rounded-full">员工</span>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="flex items-center">
                <span class="text-gray-500 mr-2">员工ID:</span>
                <span class="text-gray-800 font-medium">{{ staffInfo.id || '未知' }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-gray-500 mr-2">所属门店:</span>
                <span class="text-gray-800 font-medium">{{ staffInfo.storeName || '未知' }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-gray-500 mr-2">门店ID:</span>
                <span class="text-gray-800 font-medium">{{ staffInfo.storeId || '未知' }}</span>
              </div>
              <div class="flex items-center">
                <span class="text-gray-500 mr-2">账号状态:</span>
                <span class="px-2 py-1 bg-green-100 text-green-700 text-xs font-medium rounded-full">正常</span>
              </div>
            </div>
          </div>
          
          <!-- 基本信息表单 -->
          <h2 class="text-lg font-semibold mb-4 text-gray-700">基本信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="username" class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
              <input 
                type="text" 
                id="username" 
                v-model="profileForm.username" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500 bg-gray-100"
                disabled
              >
            </div>
            <div class="form-group">
              <label for="nickname" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
              <input 
                type="text" 
                id="nickname" 
                v-model="profileForm.nickname" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
            <div class="form-group">
              <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
              <input 
                type="tel" 
                id="phone" 
                v-model="profileForm.phone" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
            <div class="form-group">
              <label for="email" class="block text-sm font-medium text-gray-700 mb-1">电子邮箱</label>
              <input 
                type="email" 
                id="email" 
                v-model="profileForm.email" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
          </div>
        </div>
        
        <!-- 修改密码 -->
        <div>
          <h2 class="text-lg font-semibold mb-4 text-gray-700">修改密码</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="oldPassword" class="block text-sm font-medium text-gray-700 mb-1">当前密码</label>
              <input 
                type="password" 
                id="oldPassword" 
                v-model="passwordForm.oldPassword" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
            <div class="form-group md:col-span-2"></div>
            <div class="form-group">
              <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-1">新密码</label>
              <input 
                type="password" 
                id="newPassword" 
                v-model="passwordForm.newPassword" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
            <div class="form-group">
              <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">确认新密码</label>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="passwordForm.confirmPassword" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
              <p v-if="passwordMismatch" class="text-red-500 text-sm mt-1">两次输入的密码不一致</p>
            </div>
          </div>
        </div>
        
        <!-- 提交按钮 -->
        <div class="flex justify-center pt-4">
          <button 
            type="submit" 
            class="px-6 py-3 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
            :disabled="isSaving || passwordMismatch"
          >
            {{ isSaving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import toast from '../../utils/toast';
import { getCurrentStaffInfo } from '../../api/profile';
import { useUserStore } from '../../stores/user';

const isSaving = ref(false);
const isLoading = ref(true);

// 员工完整信息
const staffInfo = ref({
  id: '',
  username: '',
  name: '',
  phone: '',
  email: '',
  storeId: '',
  storeName: '',
  createTime: '',
  updateTime: ''
});

// 个人信息表单数据
const profileForm = ref({
  username: '',
  nickname: '',
  phone: '',
  email: ''
});

// 密码表单数据
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 判断两次输入的密码是否一致
const passwordMismatch = computed(() => {
  if (!passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    return false;
  }
  return passwordForm.value.newPassword !== passwordForm.value.confirmPassword;
});

// 在组件挂载时获取用户信息
onMounted(async () => {
  isLoading.value = true;
  try {
    // 首先尝试从Pinia store获取信息
    const userStore = useUserStore();
    const storeUserInfo = userStore.userInfo;
    
    // 调用API获取最新的员工信息
    console.log('正在获取员工信息...');
    const response = await getCurrentStaffInfo();
    console.log('获取员工信息响应:', response);
    
    if (response.code === 200 && response.data) {
      // 更新员工完整信息
      staffInfo.value = {
        id: response.data.id || '',
        username: response.data.username || '',
        name: response.data.name || '',
        phone: response.data.phone || '',
        email: response.data.email || '',
        storeId: response.data.storeId || '',
        storeName: response.data.storeName || '',
        createTime: response.data.createTime || '',
        updateTime: response.data.updateTime || ''
      };
      
      // 更新表单数据
      profileForm.value = {
        username: response.data.username || '',
        nickname: response.data.name || '',
        phone: response.data.phone || '',
        email: response.data.email || ''
      };
      
      console.log('员工信息已更新:', staffInfo.value);
      
      // 保存完整的员工信息到localStorage
      localStorage.setItem('userInfo', JSON.stringify(staffInfo.value));
      
      // 更新用户存储
      userStore.setUserInfo(staffInfo.value);
    } else if (storeUserInfo) {
      // 如果API调用失败但Pinia store中有数据，使用store中的数据
      console.log('API调用失败，使用Pinia store中的数据');
      staffInfo.value = { ...storeUserInfo };
      profileForm.value = {
        username: storeUserInfo.username || '',
        nickname: storeUserInfo.name || '',
        phone: storeUserInfo.phone || '',
        email: storeUserInfo.email || ''
      };
    } else {
      // 尝试从localStorage获取
      const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      if (localUserInfo && localUserInfo.id) {
        console.log('使用localStorage中的数据');
        staffInfo.value = { ...localUserInfo };
        profileForm.value = {
          username: localUserInfo.username || '',
          nickname: localUserInfo.name || '',
          phone: localUserInfo.phone || '',
          email: localUserInfo.email || ''
        };
      } else {
        toast.error('获取员工信息失败');
      }
    }
  } catch (error) {
    console.error('获取员工信息失败:', error);
    toast.error('获取员工信息失败，请重试');
    
    // 尝试从localStorage获取备用数据
    try {
      const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      if (localUserInfo && localUserInfo.id) {
        staffInfo.value = { ...localUserInfo };
        profileForm.value = {
          username: localUserInfo.username || '',
          nickname: localUserInfo.name || '',
          phone: localUserInfo.phone || '',
          email: localUserInfo.email || ''
        };
      }
    } catch (e) {
      console.error('从localStorage获取数据失败:', e);
    }
  } finally {
    isLoading.value = false;
  }
});

// 保存个人信息
const saveProfile = async () => {
  try {
    // 验证密码修改
    if (passwordForm.value.oldPassword && passwordForm.value.newPassword) {
      if (passwordMismatch.value) {
        toast.error('两次输入的密码不一致');
        return;
      }
      
      // 调用修改密码的API
      try {
        const { updatePassword } = await import('../../api/profile');
        // Trim passwords before sending
        const oldPasswordTrimmed = passwordForm.value.oldPassword.trim();
        const newPasswordTrimmed = passwordForm.value.newPassword.trim();

        if (!oldPasswordTrimmed || !newPasswordTrimmed) {
          toast.error('旧密码和新密码不能为空');
          return;
        }

        const passwordResponse = await updatePassword({
          oldPassword: oldPasswordTrimmed,
          newPassword: newPasswordTrimmed
        });

        if (passwordResponse.code !== 200) {
          toast.error(passwordResponse.message || '密码修改失败');
          return;
        }
        
        toast.success('密码修改成功');
      } catch (passwordError) {
        console.error('密码修改失败:', passwordError);
        toast.error('密码修改失败，请重试');
        return;
      }
    }
    
    isSaving.value = true;
    
    // 准备更新的员工信息，只包含Staff实体类支持的字段
    const updatedUserInfo = {
      id: staffInfo.value.id,
      username: profileForm.value.username,
      name: profileForm.value.nickname,
      phone: profileForm.value.phone,
      email: profileForm.value.email,
      storeId: staffInfo.value.storeId
    };
    
    console.log('准备更新员工信息:', updatedUserInfo);
    
    // 调用API保存员工信息
    const { updateStaffInfo } = await import('../../api/profile');
    const response = await updateStaffInfo(updatedUserInfo);
    
    if (response.code === 200) {
      // 更新本地员工信息
      staffInfo.value = {
        ...staffInfo.value,
        username: profileForm.value.username,
        name: profileForm.value.nickname,
        phone: profileForm.value.phone,
        email: profileForm.value.email,
        updateTime: new Date().toISOString()
      };
      
      // 更新localStorage中的员工信息
      localStorage.setItem('userInfo', JSON.stringify(staffInfo.value));
      
      // 更新用户存储
      const userStore = useUserStore();
      userStore.setUserInfo(staffInfo.value);
      
      toast.success('个人信息保存成功！');
      
      // 清空密码表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      
      console.log('员工信息已更新:', staffInfo.value);
    } else {
      toast.error(response.message || '保存失败');
    }
  } catch (error) {
    console.error('保存个人信息失败:', error);
    toast.error('保存失败，请重试');
  } finally {
    isSaving.value = false;
  }
};
</script>
