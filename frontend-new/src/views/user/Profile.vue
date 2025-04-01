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
        
        <!-- 常用地址 -->
        <div class="border-b pb-4">
          <h2 class="text-lg font-semibold mb-4 text-gray-700">常用地址</h2>
          <div class="space-y-4">
            <div v-for="(address, index) in profileForm.addresses" :key="index" class="border p-4 rounded-md relative">
              <button 
                v-if="profileForm.addresses.length > 1" 
                @click="removeAddress(index)" 
                type="button"
                class="absolute top-2 right-2 text-gray-400 hover:text-red-500"
              >
                <span class="material-icons">delete</span>
              </button>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="form-group">
                  <label :for="`name-${index}`" class="block text-sm font-medium text-gray-700 mb-1">联系人</label>
                  <input 
                    type="text" 
                    :id="`name-${index}`" 
                    v-model="address.name" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div class="form-group">
                  <label :for="`phone-${index}`" class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
                  <input 
                    type="tel" 
                    :id="`phone-${index}`" 
                    v-model="address.phone" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div class="form-group md:col-span-2">
                  <label :for="`address-${index}`" class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
                  <input 
                    type="text" 
                    :id="`address-${index}`" 
                    v-model="address.fullAddress" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div class="form-group">
                  <div class="flex items-center mt-2">
                    <input 
                      type="checkbox" 
                      :id="`default-${index}`" 
                      v-model="address.isDefault" 
                      class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
                      @change="setDefaultAddress(index)"
                    >
                    <label :for="`default-${index}`" class="ml-2 block text-sm text-gray-700">设为默认地址</label>
                  </div>
                </div>
              </div>
            </div>
            
            <button 
              type="button" 
              @click="addAddress" 
              class="flex items-center text-orange-500 hover:text-orange-700"
            >
              <span class="material-icons mr-1">add_circle</span>
              <span>添加新地址</span>
            </button>
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
import { getCurrentUserInfo, updateCurrentUserInfo, updatePassword } from '../../api/profile';

const isSaving = ref(false);

// 个人信息表单数据
const profileForm = ref({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  addresses: [
    {
      name: '',
      phone: '',
      fullAddress: '',
      isDefault: true
    }
  ]
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
    // 调用API获取用户信息
    const response = await getCurrentUserInfo();
    if (response.code === 200 && response.data) {
      const userData = response.data;
      // 初始化地址数据
      let addresses = [
        {
          name: '',
          phone: '',
          fullAddress: userData.address || '',
          isDefault: true
        }
      ];
      
      // 如果有地址数据，则使用该数据
      if (userData.address) {
        addresses = [
          {
            name: userData.nickname || '',
            phone: userData.phone || '',
            fullAddress: userData.address,
            isDefault: true
          }
        ];
      }
      
      profileForm.value = {
        username: userData.username || '',
        nickname: userData.nickname || '',
        phone: userData.phone || '',
        email: userData.email || '',
        addresses: addresses
      };
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    toast.error('获取用户信息失败，请刷新页面重试');
  }
});

// 添加新地址
const addAddress = () => {
  profileForm.value.addresses.push({
    name: '',
    phone: '',
    fullAddress: '',
    isDefault: false
  });
};

// 移除地址
const removeAddress = (index) => {
  profileForm.value.addresses.splice(index, 1);
};

// 设置默认地址
const setDefaultAddress = (index) => {
  if (profileForm.value.addresses[index].isDefault) {
    // 将其他地址设为非默认
    profileForm.value.addresses.forEach((address, i) => {
      if (i !== index) {
        address.isDefault = false;
      }
    });
  }
};

// 保存个人信息
const saveProfile = async () => {
  try {
    isSaving.value = true;
    
    // 获取默认地址
    const defaultAddress = profileForm.value.addresses.find(addr => addr.isDefault);
    
    // 保存个人信息
    const userInfo = {
      nickname: profileForm.value.nickname,
      phone: profileForm.value.phone,
      email: profileForm.value.email,
      address: defaultAddress ? defaultAddress.fullAddress : ''
    };
    
    const response = await updateCurrentUserInfo(userInfo);
    if (response.code !== 200) {
      throw new Error(response.message || '保存个人信息失败');
    }
    
    // 处理密码修改
    if (passwordForm.value.oldPassword && passwordForm.value.newPassword) {
      if (passwordMismatch.value) {
        toast.error('两次输入的密码不一致');
        return;
      }
      
      const passwordData = {
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      };
      
      const passwordResponse = await updatePassword(passwordData);
      if (passwordResponse.code !== 200) {
        throw new Error(passwordResponse.message || '修改密码失败');
      }
      
      // 清空密码表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
    }
    
    toast.success('个人信息保存成功！');
  } catch (error) {
    console.error('保存个人信息失败:', error);
    toast.error(error.message || '保存失败，请重试');
  } finally {
    isSaving.value = false;
  }
};
</script>