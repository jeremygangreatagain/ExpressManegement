<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">查询订单</h1>
    
    <!-- 查询表单 -->
    <div class="bg-white rounded-lg shadow-md p-6 mb-6">
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-grow">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="输入订单号、收件人姓名或电话" 
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
          >
        </div>
        <div class="flex gap-2">
          <button 
            @click="searchOrders" 
            class="px-4 py-2 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
          >
            <span class="material-icons mr-1">search</span>
            查询
          </button>
          <button 
            @click="resetSearch" 
            class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
          >
            <span class="material-icons mr-1">refresh</span>
            重置
          </button>
        </div>
      </div>
    </div>
    
    <!-- 订单列表 -->
    <div class="bg-white rounded-lg shadow-md overflow-hidden">
      <div v-if="isLoading" class="p-6 text-center">
        <p class="text-gray-500">加载中...</p>
      </div>
      
      <div v-else-if="orders.length === 0" class="p-6 text-center">
        <p class="text-gray-500">暂无订单数据</p>
      </div>
      
      <div v-else>
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">订单号</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">收件人</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">寄件时间</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in orders" :key="order.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ order.orderNumber }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <div>{{ order.receiverName }}</div>
                <div class="text-xs text-gray-400">{{ order.receiverPhone }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ order.createTime }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                  :class="getStatusClass(order.status)"
                >
                  {{ getStatusText(order.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <button 
                  @click="viewOrderDetail(order.id)" 
                  class="text-orange-500 hover:text-orange-700 mr-3"
                >
                  查看详情
                </button>
                <button 
                  v-if="order.status === 3" 
                  @click="confirmReceipt(order.id)" 
                  class="text-green-500 hover:text-green-700"
                >
                  确认收货
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 分页 -->
        <div class="px-6 py-4 flex items-center justify-between border-t border-gray-200">
          <div class="flex-1 flex justify-between sm:hidden">
            <button 
              @click="prevPage" 
              :disabled="currentPage === 1" 
              class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              上一页
            </button>
            <button 
              @click="nextPage" 
              :disabled="currentPage === totalPages" 
              class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              下一页
            </button>
          </div>
          <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
              <p class="text-sm text-gray-700">
                显示第 <span class="font-medium">{{ (currentPage - 1) * pageSize + 1 }}</span> 至 
                <span class="font-medium">{{ Math.min(currentPage * pageSize, totalItems) }}</span> 条，
                共 <span class="font-medium">{{ totalItems }}</span> 条记录
              </p>
            </div>
            <div>
              <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                <button 
                  @click="prevPage" 
                  :disabled="currentPage === 1" 
                  class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span class="sr-only">上一页</span>
                  <span class="material-icons text-sm">chevron_left</span>
                </button>
                
                <button 
                  v-for="page in displayedPages" 
                  :key="page" 
                  @click="goToPage(page)" 
                  :class="[
                    'relative inline-flex items-center px-4 py-2 border text-sm font-medium',
                    currentPage === page
                      ? 'z-10 bg-orange-500 border-orange-500 text-orange-600'
                      : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                  ]"
                >
                  {{ page }}
                </button>
                
                <button 
                  @click="nextPage" 
                  :disabled="currentPage === totalPages" 
                  class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span class="sr-only">下一页</span>
                  <span class="material-icons text-sm">chevron_right</span>
                </button>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import toast from '../../utils/toast';
import { getUserRelatedOrders, confirmOrderReceipt } from '../../api/userOrders';

const router = useRouter();
const searchQuery = ref('');
const isLoading = ref(false);
const orders = ref([]);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));

// 显示的页码范围
const displayedPages = computed(() => {
  const range = [];
  const maxPagesToShow = 5;
  let startPage = Math.max(1, currentPage.value - Math.floor(maxPagesToShow / 2));
  let endPage = Math.min(totalPages.value, startPage + maxPagesToShow - 1);
  
  if (endPage - startPage + 1 < maxPagesToShow) {
    startPage = Math.max(1, endPage - maxPagesToShow + 1);
  }
  
  for (let i = startPage; i <= endPage; i++) {
    range.push(i);
  }
  
  return range;
});

// 查询参数
const queryParams = ref({
  page: 1,
  size: 10,
  keyword: ''
});

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

// 加载订单数据
const loadOrders = async () => {
  try {
    isLoading.value = true;
    
    // 设置查询参数
    queryParams.value.page = currentPage.value;
    queryParams.value.size = pageSize.value;
    queryParams.value.keyword = searchQuery.value.trim();
    
    // 调用API获取数据
    const response = await getUserRelatedOrders(queryParams.value);
    
    console.log('API响应数据:', response);
    
    if (response.code === 200 && response.data) {
      // 正确处理后端返回的IPage<Order>数据结构
      orders.value = response.data.records || [];
      totalItems.value = response.data.total || 0;
      
      console.log('解析后的订单数据:', orders.value);
      console.log('总记录数:', totalItems.value);
    } else {
      orders.value = [];
      totalItems.value = 0;
      console.error('API返回错误或数据为空:', response);
    }
  } catch (error) {
    console.error('加载订单失败:', error);
    toast.error('加载订单失败，请重试');
    orders.value = [];
    totalItems.value = 0;
  } finally {
    isLoading.value = false;
  }
};

// 查询订单
const searchOrders = async () => {
  currentPage.value = 1;
  await loadOrders();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
  loadOrders();
};

// 查看订单详情
const viewOrderDetail = (orderId) => {
  // 获取当前订单对象
  const order = orders.value.find(o => o.id === orderId);
  if (order && order.orderNumber) {
    // 使用订单号而不是ID导航到详情页
    router.push(`/user/order-detail/${order.orderNumber}`);
  } else {
    toast.error('无法获取订单信息');
  }
};

// 确认收货
const confirmReceipt = async (orderId) => {
  try {
    // 调用API确认收货
    await confirmOrderReceipt(orderId);
    
    // 更新本地数据
    const orderIndex = orders.value.findIndex(order => order.id === orderId);
    if (orderIndex !== -1) {
      orders.value[orderIndex].status = 4; // 已完成状态为4
    }
    
    toast.success('已确认收货');
  } catch (error) {
    console.error('确认收货失败:', error);
    toast.error('确认收货失败，请重试');
  }
};

// 分页操作
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    loadOrders();
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    loadOrders();
  }
};

const goToPage = (page) => {
  currentPage.value = page;
  loadOrders();
};

// 监听搜索关键词变化
watch(searchQuery, (newValue, oldValue) => {
  if (newValue === '' && oldValue !== '') {
    // 当搜索框被清空时，重新加载数据
    resetSearch();
  }
});

// 初始加载数据
onMounted(() => {
  loadOrders();
});
</script>