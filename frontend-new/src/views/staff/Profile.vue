<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">个人信息</h1>
    
    <div class="bg-white rounded-lg shadow-md p-6 max-w-3xl mx-auto">
      <!-- 个人信息表单 -->
      <form @submit.prevent="saveProfile" class="space-y-6">
        <!-- 基本信息 -->
        <div class="border-b pb-4">
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
              <label for="nickname" class="block text-sm font-medium text-gray-700 mb-1">昵称</label>
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
  try {
    // 调用API获取员工信息
    const response = await getCurrentStaffInfo();
    if (response.code === 200 && response.data) {
      // 更新表单数据
      profileForm.value = {
        username: response.data.username || '',
        nickname: response.data.name || '',
        phone: response.data.phone || '',
        email: response.data.email || ''
      };
      
      // 保存完整的员工信息到localStorage，确保包含storeId
      const staffInfo = {
        ...response.data,
        storeId: response.data.storeId // 确保门店ID被保存
      };
      localStorage.setItem('userInfo', JSON.stringify(staffInfo));
      
      // 更新用户存储
      const userStore = useUserStore();
      userStore.setUserInfo(staffInfo);
    } else {
      toast.error('获取员工信息失败');
    }
  } catch (error) {
    console.error('获取员工信息失败:', error);
    toast.error('获取员工信息失败，请重试');
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
        const passwordResponse = await updatePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
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
    
    // 获取当前存储的员工信息
    const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    
    // 准备更新的员工信息，确保包含storeId
    const updatedUserInfo = {
      ...currentUserInfo,
      username: profileForm.value.username,
      name: profileForm.value.nickname,
      phone: profileForm.value.phone,
      email: profileForm.value.email,
      // 确保保留storeId
      storeId: currentUserInfo.storeId
    };
    
    // 调用API保存员工信息
    const { updateStaffInfo } = await import('../../api/profile');
    const response = await updateStaffInfo(updatedUserInfo);
    
    if (response.code === 200) {
      // 更新localStorage中的员工信息，确保保留门店ID
      localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo));
      
      // 更新用户存储
      const userStore = useUserStore();
      userStore.setUserInfo(updatedUserInfo);
      
      toast.success('个人信息保存成功！');
      
      // 清空密码表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
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