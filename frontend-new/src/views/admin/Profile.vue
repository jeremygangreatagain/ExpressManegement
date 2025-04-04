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
import { getCurrentUserInfo, updateCurrentUserInfo, updatePassword } from '@/api/profile';

const isSaving = ref(false);
const isLoading = ref(true);

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
    isLoading.value = true;
    const res = await getCurrentUserInfo();
    if (res.data) {
      profileForm.value = {
        username: res.data.username || '',
        nickname: res.data.nickname || '',
        phone: res.data.phone || '',
        email: res.data.email || ''
      };
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    toast.error('获取用户信息失败');
    // 使用本地存储的用户名作为备选
    profileForm.value.username = localStorage.getItem('username') || '';
  } finally {
    isLoading.value = false;
  }
});

// 保存个人信息
const saveProfile = async () => {
  try {
    // 验证手机号格式
    if (profileForm.value.phone && !/^1[3-9]\d{9}$/.test(profileForm.value.phone)) {
      toast.error('请输入正确的手机号码');
      return;
    }
    
    // 验证邮箱格式
    if (profileForm.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileForm.value.email)) {
      toast.error('请输入正确的邮箱地址');
      return;
    }
    
    isSaving.value = true;
    
    // 修改密码
    if (passwordForm.value.oldPassword && passwordForm.value.newPassword) {
      if (passwordMismatch.value) {
        toast.error('两次输入的密码不一致');
        return;
      }
      
      try {
        await updatePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        });
        toast.success('密码修改成功');
        
        // 清空密码表单
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
      } catch (error) {
        console.error('修改密码失败:', error);
        toast.error(error.response?.data?.message || '修改密码失败，请检查当前密码是否正确');
        return;
      }
    }
    
    // 更新个人信息
    await updateCurrentUserInfo({
      username: profileForm.value.username,
      nickname: profileForm.value.nickname,
      phone: profileForm.value.phone,
      email: profileForm.value.email
    });
    
    toast.success('个人信息保存成功！');
  } catch (error) {
    console.error('保存个人信息失败:', error);
    toast.error(error.response?.data?.message || '保存失败，请重试');
  } finally {
    isSaving.value = false;
  }
};
</script>