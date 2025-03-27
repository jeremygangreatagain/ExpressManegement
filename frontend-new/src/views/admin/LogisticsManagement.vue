<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">物流事务</h1>
    
    <!-- 搜索和操作区域 -->
    <div class="bg-white rounded-lg shadow-md p-6 mb-6">
      <div class="flex flex-col md:flex-row gap-4 mb-4">
        <div class="flex-grow">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="输入订单号、收件人姓名或电话" 
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
          >
        </div>
        <div class="flex gap-2">
          <select 
            v-model="statusFilter" 
            class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
          >
            <option value="">全部状态</option>
            <option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
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
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">寄件人</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">物流信息</th>
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
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <div>{{ order.senderName }}</div>
                <div class="text-xs text-gray-400">{{ order.senderPhone }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(order.createTime) }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                  :class="getStatusClass(order.status)"
                >
                  {{ getStatusText(order.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span v-if="order.logistics && order.logistics.length > 0" class="text-green-500">
                  {{ order.logistics.length }}条记录
                </span>
                <span v-else class="text-gray-400">暂无记录</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <button 
                  @click="viewOrderDetail(order.orderNumber)" 
                  class="text-blue-500 hover:text-blue-700 mr-2"
                >
                  查看
                </button>
                <button 
                  @click="addLogistics(order.orderNumber)" 
                  class="text-orange-500 hover:text-orange-700"
                >
                  添加物流
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
    
    <!-- 订单详情对话框 -->
    <div v-if="showDetailDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200 flex justify-between items-center">
          <h3 class="text-lg font-medium text-gray-900">订单详情</h3>
          <button @click="showDetailDialog = false" class="text-gray-400 hover:text-gray-500">
            <span class="material-icons">close</span>
          </button>
        </div>
        <div class="p-6">
          <div v-if="isLoadingDetail" class="text-center py-4">
            <p class="text-gray-500">加载中...</p>
          </div>
          <div v-else>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <h4 class="text-sm font-medium text-gray-500 mb-2">基本信息</h4>
                <div class="bg-gray-50 p-4 rounded-md">
                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <p class="text-xs text-gray-500">订单号</p>
                      <p class="text-sm font-medium">{{ orderDetail.orderNumber }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">创建时间</p>
                      <p class="text-sm font-medium">{{ formatDate(orderDetail.createTime) }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">订单状态</p>
                      <p class="text-sm font-medium">
                        <span 
                          class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                          :class="getStatusClass(orderDetail.status)"
                        >
                          {{ getStatusText(orderDetail.status) }}
                        </span>
                      </p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">物品类型</p>
                      <p class="text-sm font-medium">{{ orderDetail.itemType }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">重量(kg)</p>
                      <p class="text-sm font-medium">{{ orderDetail.weight }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">备注</p>
                      <p class="text-sm font-medium">{{ orderDetail.remark || '无' }}</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-500 mb-2">费用信息</h4>
                <div class="bg-gray-50 p-4 rounded-md">
                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <p class="text-xs text-gray-500">运费</p>
                      <p class="text-sm font-medium">¥{{ orderDetail.price?.toFixed(2) }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">总费用</p>
                      <p class="text-sm font-medium text-orange-500">¥{{ orderDetail.price?.toFixed(2) }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">支付状态</p>
                      <p class="text-sm font-medium">
                        <span 
                          class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                          :class="orderDetail.paymentStatus === 1 ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'"
                        >
                          {{ orderDetail.paymentStatus === 1 ? '已支付' : '未支付' }}
                        </span>
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <h4 class="text-sm font-medium text-gray-500 mb-2">寄件人信息</h4>
                <div class="bg-gray-50 p-4 rounded-md">
                  <div class="space-y-2">
                    <div>
                      <p class="text-xs text-gray-500">姓名</p>
                      <p class="text-sm font-medium">{{ orderDetail.senderName }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">电话</p>
                      <p class="text-sm font-medium">{{ orderDetail.senderPhone }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">地址</p>
                      <p class="text-sm font-medium">{{ orderDetail.senderAddress }}</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-500 mb-2">收件人信息</h4>
                <div class="bg-gray-50 p-4 rounded-md">
                  <div class="space-y-2">
                    <div>
                      <p class="text-xs text-gray-500">姓名</p>
                      <p class="text-sm font-medium">{{ orderDetail.receiverName }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">电话</p>
                      <p class="text-sm font-medium">{{ orderDetail.receiverPhone }}</p>
                    </div>
                    <div>
                      <p class="text-xs text-gray-500">地址</p>
                      <p class="text-sm font-medium">{{ orderDetail.receiverAddress }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 物流信息 -->
            <div class="mb-6">
              <h4 class="text-sm font-medium text-gray-500 mb-2">物流信息</h4>
              <div v-if="orderDetail.logistics && orderDetail.logistics.length > 0" class="bg-gray-50 p-4 rounded-md">
                <div class="relative">
                  <div class="border-l-2 border-orange-500 ml-3 py-2">
                    <div v-for="(item, index) in orderDetail.logistics" :key="index" class="mb-6 ml-6 relative">
                      <div class="absolute -left-[27px] mt-1">
                        <div class="h-4 w-4 rounded-full bg-orange-500"></div>
                      </div>
                      <div class="text-sm">
                        <p class="font-medium">{{ item.message }}</p>
                        <p class="text-xs text-gray-500 mt-1">{{ formatDate(item.createTime) }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="bg-gray-50 p-4 rounded-md text-center text-gray-500">
                暂无物流信息
              </div>
            </div>
          </div>
        </div>
        <div class="p-6 border-t border-gray-200 flex justify-end">
          <button 
            @click="showDetailDialog = false" 
            class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
    
    <!-- 添加物流信息对话框 -->
    <div v-if="showLogisticsDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full">
        <div class="p-6 border-b border-gray-200 flex justify-between items-center">
          <h3 class="text-lg font-medium text-gray-900">添加物流信息</h3>
          <button @click="showLogisticsDialog = false" class="text-gray-400 hover:text-gray-500">
            <span class="material-icons">close</span>
          </button>
        </div>
        <div class="p-6">
          <form @submit.prevent="saveLogistics">
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">订单号</label>
              <input 
                type="text" 
                v-model="currentOrderNumber" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-100" 
                readonly
              >
            </div>
            
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">物流状态</label>
              <select 
                v-model="newLogistics.status" 
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              >
                <option value="">请选择物流状态</option>
                <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </div>
            
            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">物流信息</label>
              <textarea 
                v-model="newLogistics.message" 
                rows="3"
                placeholder="请输入物流信息，如：包裹已到达XX分拣中心"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                required
              ></textarea>
            </div>
            
            <div class="flex justify-end space-x-3">
              <button 
                type="button"
                @click="showLogisticsDialog = false" 
                class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
              >
                取消
              </button>
              <button 
                type="submit" 
                class="px-4 py-2 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
              >
                保存
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getOrderList, getOrderDetail, addOrderLogistics } from '../../api/orders';
import toast from '../../utils/toast';

// 搜索和筛选
const searchQuery = ref('');
const statusFilter = ref('');
const isLoading = ref(false);
const isLoadingDetail = ref(false);

// 订单列表数据
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

// 订单状态选项
const statusOptions = ref([
  { value: 'PENDING', label: '待取件' },
  { value: 'PICKED_UP', label: '已取件' },
  { value: 'IN_TRANSIT', label: '运输中' },
  { value: 'DELIVERED', label: '已送达' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]);

// 对话框控制
const showDetailDialog = ref(false);
const showLogisticsDialog = ref(false);
const orderDetail = ref({});
const currentOrderNumber = ref('');
const newLogistics = ref({
  status: '',
  message: ''
});

// 获取订单列表
const fetchOrders = async () => {
  isLoading.value = true;
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      status: statusFilter.value
    };
    
    const res = await getOrderList(params);
    orders.value = res.data.records || [];
    totalItems.value = res.data.total || 0;
  } catch (error) {
    console.error('获取订单列表失败:', error);
    toast.error('获取订单列表失败');
  } finally {
    isLoading.value = false;
  }
};

// 获取订单详情
const fetchOrderDetail = async (idOrOrderNumber) => {
  isLoadingDetail.value = true;
  try {
    const res = await getOrderDetail(idOrOrderNumber);
    orderDetail.value = res.data || {};
    
    // 将description字段映射到remark字段，用于显示备注信息
    if (orderDetail.value.description !== undefined) {
      orderDetail.value.remark = orderDetail.value.description;
      console.log('订单详情 - 获取备注:', orderDetail.value.description, '映射到remark:', orderDetail.value.remark);
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    toast.error('获取订单详情失败');
  } finally {
    isLoadingDetail.value = false;
  }
};

// 查看订单详情
const viewOrderDetail = async (orderNumber) => {
  await fetchOrderDetail(orderNumber);
  showDetailDialog.value = true;
};

// 添加物流信息
const addLogistics = (orderNumber) => {
  currentOrderNumber.value = orderNumber;
  newLogistics.value = {
    status: '',
    message: ''
  };
  showLogisticsDialog.value = true;
};

// 保存物流信息
const saveLogistics = async () => {
  try {
    if (!newLogistics.value.status || !newLogistics.value.message) {
      toast.warning('请填写完整的物流信息');
      return;
    }
    
    const data = {
      orderNumber: currentOrderNumber.value,
      status: newLogistics.value.status,
      message: newLogistics.value.message
    };
    
    await addOrderLogistics(data);
    toast.success('添加物流信息成功');
    showLogisticsDialog.value = false;
    fetchOrders(); // 刷新订单列表
  } catch (error) {
    console.error('添加物流信息失败:', error);
    toast.error('添加物流信息失败');
  }
};

// 搜索订单
const searchOrders = () => {
  currentPage.value = 1; // 重置到第一页
  fetchOrders();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  statusFilter.value = '';
  currentPage.value = 1;
  fetchOrders();
};

// 分页相关方法
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    fetchOrders();
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    fetchOrders();
  }
};

const goToPage = (page) => {
  currentPage.value = page;
  fetchOrders();
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

// 获取状态文本
const getStatusText = (status) => {
  const option = statusOptions.value.find(opt => opt.value === status);
  return option ? option.label : '未知状态';
};

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800';
    case 'PICKED_UP':
      return 'bg-blue-100 text-blue-800';
    case 'IN_TRANSIT':
      return 'bg-indigo-100 text-indigo-800';
    case 'DELIVERED':
      return 'bg-green-100 text-green-800';
    case 'COMPLETED':
      return 'bg-green-100 text-green-800';
    case 'CANCELLED':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// 初始化数据
onMounted(() => {
  fetchOrders();
});
</script>