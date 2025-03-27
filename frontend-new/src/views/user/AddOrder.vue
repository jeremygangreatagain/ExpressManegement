<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">添加订单</h1>
    
    <div class="bg-white rounded-lg shadow-md p-6 max-w-3xl mx-auto">
      <form @submit.prevent="submitOrder" class="space-y-6">
        <!-- 寄件人信息 -->
        <div class="border-b pb-4">
          <h2 class="text-lg font-semibold mb-4 text-gray-700">寄件人信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="senderName" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
              <input 
                type="text" 
                id="senderName" 
                v-model="orderForm.senderName" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
            <div class="form-group">
              <label for="senderPhone" class="block text-sm font-medium text-gray-700 mb-1">电话</label>
              <input 
                type="tel" 
                id="senderPhone" 
                v-model="orderForm.senderPhone" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
            <div class="form-group md:col-span-2">
              <label for="senderAddress" class="block text-sm font-medium text-gray-700 mb-1">地址</label>
              <input 
                type="text" 
                id="senderAddress" 
                v-model="orderForm.senderAddress" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
          </div>
        </div>
        
        <!-- 收件人信息 -->
        <div class="border-b pb-4">
          <h2 class="text-lg font-semibold mb-4 text-gray-700">收件人信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="receiverName" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
              <input 
                type="text" 
                id="receiverName" 
                v-model="orderForm.receiverName" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
            <div class="form-group">
              <label for="receiverPhone" class="block text-sm font-medium text-gray-700 mb-1">电话</label>
              <input 
                type="tel" 
                id="receiverPhone" 
                v-model="orderForm.receiverPhone" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
            <div class="form-group md:col-span-2">
              <label for="receiverAddress" class="block text-sm font-medium text-gray-700 mb-1">地址</label>
              <input 
                type="text" 
                id="receiverAddress" 
                v-model="orderForm.receiverAddress" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
          </div>
        </div>
        
        <!-- 包裹信息 -->
        <div>
          <h2 class="text-lg font-semibold mb-4 text-gray-700">包裹信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="weight" class="block text-sm font-medium text-gray-700 mb-1">重量 (kg)</label>
              <input 
                type="number" 
                id="weight" 
                v-model="orderForm.weight" 
                step="0.1"
                min="0.1"
                @input="calculatePrice"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
            </div>
            <div class="form-group">
              <label for="price" class="block text-sm font-medium text-gray-700 mb-1">运费 (元)</label>
              <input 
                type="number" 
                id="price" 
                v-model="orderForm.price" 
                step="0.1"
                min="0"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
                readonly
              >
            </div>
            <div class="form-group">
              <label for="itemType" class="block text-sm font-medium text-gray-700 mb-1">物品种类</label>
              <select 
                id="itemType" 
                v-model="orderForm.itemType" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
                <option value="">请选择物品种类</option>
                <option value="电器">电器</option>
                <option value="数码产品">数码产品</option>
                <option value="日常用品">日常用品</option>
                <option value="文件类">文件类</option>
                <option value="服装">服装</option>
                <option value="食品">食品</option>
                <option value="化妆品">化妆品</option>
                <option value="玩具">玩具</option>
                <option value="书籍">书籍</option>
              </select>
            </div>
            <div class="form-group md:col-span-2">
              <label for="description" class="block text-sm font-medium text-gray-700 mb-1">物品描述</label>
              <textarea 
                id="description" 
                v-model="orderForm.description" 
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              ></textarea>
            </div>
          </div>
        </div>
        
        <!-- 提交按钮 -->
        <div class="flex justify-center pt-4">
          <button 
            type="submit" 
            class="px-6 py-3 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import toast from '../../utils/toast';
import request from '../../utils/request';

const router = useRouter();
const isSubmitting = ref(false);

// 订单表单数据
const orderForm = ref({
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  weight: '',
  price: 0,
  itemType: '',
  description: '',
  storeId: 1 // 默认快递点ID，实际应用中应该从下拉列表选择
});

// 计算运费
const calculatePrice = () => {
  if (orderForm.value.weight && orderForm.value.weight > 0) {
    // 简单的运费计算逻辑：10元起步价，每增加1kg增加5元
    const basePrice = 10;
    const additionalPrice = Math.max(0, orderForm.value.weight - 1) * 5;
    orderForm.value.price = basePrice + additionalPrice;
  } else {
    orderForm.value.price = 0;
  }
};

// 提交订单
const submitOrder = async () => {
  try {
    isSubmitting.value = true;
    
    // 调用后端API创建订单
    const response = await request({
      url: '/user/order',
      method: 'post',
      data: orderForm.value
    });
    
    if (response.code === 200) {
      toast.success('订单提交成功！');
      router.push('/user');
    } else {
      toast.error(response.message || '订单提交失败');
    }
  } catch (error) {
    console.error('提交订单失败:', error);
    toast.error('订单提交失败，请重试');
  } finally {
    isSubmitting.value = false;
  }
};
</script>