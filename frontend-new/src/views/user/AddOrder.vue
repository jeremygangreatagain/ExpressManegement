<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">添加新订单</h1>

    <div class="bg-white rounded-lg shadow-md p-6 max-w-4xl mx-auto">
      <form @submit.prevent="submitOrder" class="space-y-6">
        
        <!-- 寄件人信息 -->
        <div class="border-b pb-4">
          <h2 class="text-lg font-semibold mb-4 text-gray-700">寄件人信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="senderName" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
              <input type="text" id="senderName" v-model="orderForm.senderName" required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
            <div class="form-group">
              <label for="senderPhone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
              <input type="tel" id="senderPhone" v-model="orderForm.senderPhone" required pattern="^1[3-9]\d{9}$" title="请输入有效的11位手机号码" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
            <div class="form-group md:col-span-2">
              <label for="senderAddress" class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
              <input type="text" id="senderAddress" v-model="orderForm.senderAddress" required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
          </div>
        </div>

        <!-- 收件人信息 -->
        <div class="border-b pb-4">
          <h2 class="text-lg font-semibold mb-4 text-gray-700">收件人信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="receiverName" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
              <input type="text" id="receiverName" v-model="orderForm.receiverName" required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
            <div class="form-group">
              <label for="receiverPhone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
              <input type="tel" id="receiverPhone" v-model="orderForm.receiverPhone" required pattern="^1[3-9]\d{9}$" title="请输入有效的11位手机号码" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
            <div class="form-group md:col-span-2">
              <label for="receiverAddress" class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
              <input type="text" id="receiverAddress" v-model="orderForm.receiverAddress" required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
          </div>
        </div>

        <!-- 包裹信息 -->
        <div>
          <h2 class="text-lg font-semibold mb-4 text-gray-700">包裹信息</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group">
              <label for="itemName" class="block text-sm font-medium text-gray-700 mb-1">物品名称</label>
              <input type="text" id="itemName" v-model="orderForm.itemName" required class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
            <div class="form-group">
              <label for="weight" class="block text-sm font-medium text-gray-700 mb-1">重量 (kg)</label>
              <input type="number" id="weight" v-model.number="orderForm.weight" required min="0.1" step="0.1" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500">
            </div>
             <div class="form-group md:col-span-2">
              <label for="remark" class="block text-sm font-medium text-gray-700 mb-1">备注 (可选)</label>
              <textarea id="remark" v-model="orderForm.remark" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"></textarea>
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
import toast from '@/utils/toast';
import { createUserOrder } from '@/api/orders'; // 导入用户创建订单的API函数

const router = useRouter();
const isSubmitting = ref(false);

const orderForm = ref({
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  itemName: '',
  weight: null,
  remark: ''
});

const submitOrder = async () => {
  isSubmitting.value = true;
  try {
    // 调用API创建订单
    console.log('Submitting order:', orderForm.value);
    const response = await createUserOrder(orderForm.value);
    console.log('Order created:', response.data); // 后端应返回创建的订单信息

    toast.success('订单提交成功！订单号：' + (response.data?.orderNumber || '未知')); // 显示订单号

    // 清空表单
    orderForm.value = {
      senderName: '',
      senderPhone: '',
      senderAddress: '',
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      itemName: '',
      weight: null,
      remark: ''
    };

    // 可选：跳转到订单查询页面 (如果已存在)
    // router.push('/user/orders');

  } catch (error) {
    console.error('提交订单失败:', error);
    toast.error(error.response?.data?.message || '提交订单失败，请重试');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
/* Add any specific styles if needed */
</style>
