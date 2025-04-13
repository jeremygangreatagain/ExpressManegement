<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <div class="flex items-center mb-6">
      <button @click="goBack" class="mr-4 text-gray-600 hover:text-gray-800">
        <span class="material-icons">arrow_back</span>
      </button>
      <h1 class="text-2xl font-bold text-orange-500">订单详情</h1>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="isLoading" class="bg-white rounded-lg shadow-md p-6 text-center">
      <p class="text-gray-500">加载中...</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-else-if="error" class="bg-white rounded-lg shadow-md p-6 text-center">
      <p class="text-red-500">{{ error }}</p>
      <button 
        @click="loadOrderDetail" 
        class="mt-4 px-4 py-2 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
      >
        重试
      </button>
    </div>
    
    <!-- 订单详情内容 -->
    <div v-else-if="order" class="space-y-6">
      <!-- 订单基本信息 -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-lg font-semibold text-gray-800 mb-4 pb-2 border-b border-gray-200">订单信息</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="flex">
            <span class="text-gray-500 w-24">订单号：</span>
            <span class="font-medium">{{ order.orderNumber }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24">创建时间：</span>
            <span>{{ order.createTime }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24">订单状态：</span>
            <span 
              class="px-2 py-1 text-xs font-semibold rounded-full" 
              :class="getStatusClass(order.status)"
            >
              {{ getStatusText(order.status) }}
            </span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24">物品类型：</span>
            <span>{{ order.itemType }}</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24">重量：</span>
            <span>{{ order.weight }} kg</span>
          </div>
          <div class="flex">
            <span class="text-gray-500 w-24">运费：</span>
            <span class="text-orange-500 font-medium">¥{{ order.price }}</span>
          </div>
          <div class="flex col-span-2">
            <span class="text-gray-500 w-24">备注：</span>
            <span>{{ order.remark || '无' }}</span>
          </div>
        </div>
      </div>
      
      <!-- 寄件人和收件人信息 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 寄件人信息 -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-lg font-semibold text-gray-800 mb-4 pb-2 border-b border-gray-200">寄件人信息</h2>
          <div class="space-y-3">
            <div class="flex">
              <span class="text-gray-500 w-24">姓名：</span>
              <span>{{ order.senderName }}</span>
            </div>
            <div class="flex">
              <span class="text-gray-500 w-24">电话：</span>
              <span>{{ order.senderPhone }}</span>
            </div>
            <div class="flex">
              <span class="text-gray-500 w-24">地址：</span>
              <span>{{ order.senderAddress }}</span>
            </div>
          </div>
        </div>
        
        <!-- 收件人信息 -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-lg font-semibold text-gray-800 mb-4 pb-2 border-b border-gray-200">收件人信息</h2>
          <div class="space-y-3">
            <div class="flex">
              <span class="text-gray-500 w-24">姓名：</span>
              <span>{{ order.receiverName }}</span>
            </div>
            <div class="flex">
              <span class="text-gray-500 w-24">电话：</span>
              <span>{{ order.receiverPhone }}</span>
            </div>
            <div class="flex">
              <span class="text-gray-500 w-24">地址：</span>
              <span>{{ order.receiverAddress }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 物流信息 -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-lg font-semibold text-gray-800 mb-4 pb-2 border-b border-gray-200">物流信息</h2>
        <div v-if="order.logistics && order.logistics.length > 0" class="relative">
          <!-- 物流时间线 -->
          <div class="border-l-2 border-orange-300 absolute h-full left-2.5 top-0"></div>
          <ul class="space-y-6 relative">
            <li v-for="(item, index) in order.logistics" :key="index" class="ml-6">
              <div class="absolute w-6 h-6 bg-orange-500 rounded-full -left-3 flex items-center justify-center">
                <span class="material-icons text-white text-sm">local_shipping</span>
              </div>
              <div class="bg-gray-50 p-4 rounded-md">
                <p class="text-sm text-gray-400">{{ item.time }}</p>
                <p class="mt-1">{{ item.content }}</p>
                <p v-if="item.operator" class="mt-1 text-sm text-gray-500">操作人: {{ item.operator }}</p>
              </div>
            </li>
          </ul>
        </div>
        <div v-else class="text-center py-4 text-gray-500">
          暂无物流信息
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div v-if="order.status === 3" class="flex justify-center">
        <button 
          @click="confirmReceipt" 
          class="px-6 py-2 bg-green-500 text-white font-medium rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition-colors"
        >
          确认收货
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUserOrderDetail, confirmOrderReceipt } from '../../api/userOrders';
import toast from '../../utils/toast';

const route = useRoute();
const router = useRouter();
const orderNumber = ref(route.params.orderNumber);
const order = ref(null);
const isLoading = ref(true);
const error = ref(null);

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待取件',
    1: '已取件',
    2: '运输中',
    3: '已送达',
    4: '已完成',
    5: '已取消',
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  };
  return statusMap[status] || `未知状态(${status})`;
};

// 获取订单状态样式
const getStatusClass = (status) => {
  const classMap = {
    0: 'bg-yellow-100 text-yellow-800', // 待取件
    1: 'bg-blue-100 text-blue-800',     // 已取件
    2: 'bg-indigo-100 text-indigo-800', // 运输中
    3: 'bg-green-100 text-green-800',   // 已送达
    4: 'bg-gray-100 text-gray-800',     // 已完成
    5: 'bg-red-100 text-red-800',       // 已取消
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'PROCESSING': 'bg-blue-100 text-blue-800',
    'SHIPPED': 'bg-indigo-100 text-indigo-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'COMPLETED': 'bg-gray-100 text-gray-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  };
  return classMap[status] || 'bg-gray-100 text-gray-800';
};

// 加载订单详情
const loadOrderDetail = async () => {
  isLoading.value = true;
  error.value = null;
  
  try {
    const response = await getUserOrderDetail(orderNumber.value);
    
    if (response.code === 200 && response.data) {
      order.value = response.data;
      console.log('订单详情:', order.value);
    } else {
      error.value = response.message || '获取订单详情失败';
      console.error('API返回错误:', response);
    }
  } catch (err) {
    error.value = '获取订单详情失败，请重试';
    console.error('加载订单详情失败:', err);
  } finally {
    isLoading.value = false;
  }
};

// 确认收货
const confirmReceipt = async () => {
  try {
    await confirmOrderReceipt(order.value.id);
    order.value.status = 4; // 更新状态为已完成
    toast.success('已确认收货');
  } catch (err) {
    console.error('确认收货失败:', err);
    toast.error('确认收货失败，请重试');
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

onMounted(() => {
  loadOrderDetail();
});
</script>