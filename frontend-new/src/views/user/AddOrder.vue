<template>
  <div class="p-2 bg-gray-100 min-h-screen">
    <h1 class="text-xl font-bold text-orange-500 mb-3">添加新订单</h1>

    <div class="max-w-full mx-auto px-2">
      <form @submit.prevent="submitOrder" class="space-y-4">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <!-- 第一列：寄件人信息 -->
          <div class="bg-white rounded-lg shadow-md p-4 min-h-[800px] flex flex-col">
            <div class="flex justify-between items-center mb-3">
              <h2 class="text-base font-semibold text-gray-700">寄件人信息</h2>
              <span class="text-xs text-gray-500">(默认填充，可修改)</span>
            </div>
            <div class="space-y-3 flex-grow">
              <div class="form-group">
                <label for="senderName" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                <input type="text" id="senderName" v-model="orderForm.senderName" required class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
              <div class="form-group">
                <label for="senderPhone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
                <input type="tel" id="senderPhone" v-model="orderForm.senderPhone" required pattern="^1[3-9]\d{9}$" title="请输入有效的11位手机号码" class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
              <div class="form-group">
                <label for="senderAddress" class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
                <input type="text" id="senderAddress" v-model="orderForm.senderAddress" required class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
            </div>
          </div>

          <!-- 第二列：收件人信息 -->
          <div class="bg-white rounded-lg shadow-md p-4 min-h-[800px] flex flex-col">
            <h2 class="text-base font-semibold mb-3 text-gray-700">收件人信息</h2>
            <div class="space-y-3 flex-grow">
              <div class="form-group">
                <label for="receiverName" class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                <input type="text" id="receiverName" v-model="orderForm.receiverName" required class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
              <div class="form-group">
                <label for="receiverPhone" class="block text-sm font-medium text-gray-700 mb-1">手机号码</label>
                <input type="tel" id="receiverPhone" v-model="orderForm.receiverPhone" required pattern="^1[3-9]\d{9}$" title="请输入有效的11位手机号码" class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
              <div class="form-group">
                <label for="receiverAddress" class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
                <input type="text" id="receiverAddress" v-model="orderForm.receiverAddress" required class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
              <!-- 备注字段移动到这里 -->
              <div class="form-group mt-4">
                <label for="remark" class="block text-sm font-medium text-gray-700 mb-1">备注 (可选)</label>
                <textarea id="remark" v-model="orderForm.remark" rows="3" class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500"></textarea>
              </div>
            </div>
          </div>

          <!-- 第三列：包裹信息和提交按钮 -->
          <div class="bg-white rounded-lg shadow-md p-4 min-h-[600px] flex flex-col">
            <h2 class="text-base font-semibold mb-3 text-gray-700">包裹信息</h2>
            <div class="space-y-3 flex-grow">
              <div class="form-group">
                <label for="itemType" class="block text-sm font-medium text-gray-700 mb-1">物品种类</label>
                <select id="itemType" v-model="orderForm.itemType" required class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
                  <option value="" disabled>请选择物品种类</option>
                  <option value="电器">电器</option>
                  <option value="数码产品">数码产品</option>
                  <option value="日常用品">日常用品</option>
                  <option value="文件类">文件类</option>
                  <option value="服装">服装</option>
                  <option value="食品">食品</option>
                  <option value="化妆品">化妆品</option>
                  <option value="玩具">玩具</option>
                </select>
              </div>
              <div class="form-group">
                <label for="weight" class="block text-sm font-medium text-gray-700 mb-1">重量 (kg)</label>
                <input type="number" id="weight" v-model.number="orderForm.weight" required min="0.1" step="0.1" class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
              </div>
              <div class="form-group">
                <label for="storeId" class="block text-sm font-medium text-gray-700 mb-1">选择快递门店</label>
                <select id="storeId" v-model="orderForm.storeId" required class="w-full px-2 py-1.5 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-orange-500">
                  <option value="" disabled>请选择门店</option>
                  <option v-for="store in storeList" :key="store.id" :value="store.id">{{ store.name }} ({{ store.address }})</option>
                </select>
              </div>
              <!-- 提交按钮 -->
              <div class="flex justify-center pt-4 mt-4">
                <button 
                  type="submit" 
                  class="px-6 py-2 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors w-full"
                  :disabled="isSubmitting"
                >
                  {{ isSubmitting ? '提交中...' : '提交订单' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import toast from '@/utils/toast';
import { createUserOrder } from '@/api/orders'; // 导入用户创建订单的API函数
import { getCurrentUserInfo } from '@/api/profile'; // 导入获取当前用户信息的API函数
import { getAllStores } from '@/api/stores'; // 导入获取所有门店的API函数

const router = useRouter();
const isSubmitting = ref(false);
const storeList = ref([]); // 门店列表
const isLoading = ref(true); // 加载状态

const orderForm = ref({
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  itemType: '',  // 将itemName改为itemType
  weight: null,
  remark: '',
  storeId: '' // 添加门店ID字段
});

// 获取当前用户信息
const fetchUserInfo = async () => {
  try {
    const response = await getCurrentUserInfo();
    const userInfo = response.data;
    
    // 自动填充发货人信息
    orderForm.value.senderName = userInfo.name || userInfo.username;
    orderForm.value.senderPhone = userInfo.phone || '';
    orderForm.value.senderAddress = userInfo.address || '';
    
    console.log('用户信息获取成功:', userInfo);
  } catch (error) {
    console.error('获取用户信息失败:', error);
    toast.error('获取用户信息失败，请刷新页面重试');
  }
};

// 获取所有门店列表
const fetchStores = async () => {
  try {
    const response = await getAllStores();
    storeList.value = response.data || [];
    console.log('门店列表获取成功:', storeList.value);
  } catch (error) {
    console.error('获取门店列表失败:', error);
    toast.error('获取门店列表失败，请刷新页面重试');
  } finally {
    isLoading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(async () => {
  try {
    // 并行获取用户信息和门店列表
    await Promise.all([fetchUserInfo(), fetchStores()]);
  } catch (error) {
    console.error('初始化数据失败:', error);
  }
});

const submitOrder = async () => {
  // 验证是否选择了门店
  if (!orderForm.value.storeId) {
    toast.error('请选择快递门店');
    return;
  }
  
  isSubmitting.value = true;
  try {
    // 创建一个新的数据对象，将remark字段映射到description字段
    const orderData = {
      ...orderForm.value,
      description: orderForm.value.remark, // 将remark映射到description
    };
    // 删除remark字段，因为后端不识别该字段
    delete orderData.remark;
    
    // 调用API创建订单
    console.log('Submitting order:', orderData);
    const response = await createUserOrder(orderData);
    console.log('Order created:', response.data); // 后端应返回创建的订单信息

    toast.success('订单提交成功！订单号：' + (response.data?.orderNumber || '未知')); // 显示订单号

    // 清空收件人和包裹信息，保留发件人信息
    orderForm.value = {
      ...orderForm.value,
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      itemType: '',
      weight: null,
      remark: '',
      storeId: ''
    };

    // 可选：跳转到订单查询页面
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
