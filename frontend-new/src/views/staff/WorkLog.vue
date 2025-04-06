<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">工作日志</h1>
    
    <!-- 搜索和操作区域 -->
    <div class="bg-white rounded-lg shadow-md p-6 mb-6">
      <div class="flex flex-col md:flex-row gap-4 mb-4">
        <div class="flex-grow">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索日志内容" 
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
          >
        </div>
        <div class="flex gap-2">
          <button 
            @click="searchLogs" 
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
      <div class="flex flex-col md:flex-row gap-4">
        <div class="flex-grow">
          <div class="flex gap-4">
            <div class="w-1/2">
              <label class="block text-sm font-medium text-gray-700 mb-1">开始日期</label>
              <input 
                type="date" 
                v-model="startDate" 
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
            <div class="w-1/2">
              <label class="block text-sm font-medium text-gray-700 mb-1">结束日期</label>
              <input 
                type="date" 
                v-model="endDate" 
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
          </div>
        </div>
        <div class="flex items-end">
          <button 
            @click="addNewLog" 
            class="px-4 py-2 bg-green-500 text-white font-medium rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition-colors"
          >
            <span class="material-icons mr-1">add</span>
            添加日志
          </button>
        </div>
      </div>
    </div>
    
    <!-- 日志列表 -->
    <div class="bg-white rounded-lg shadow-md overflow-hidden">
      <div v-if="isLoading" class="p-6 text-center">
        <p class="text-gray-500">加载中...</p>
      </div>
      
      <div v-else-if="logs.length === 0" class="p-6 text-center">
        <p class="text-gray-500">暂无日志记录</p>
      </div>
      
      <div v-else>
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">日期</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">内容</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">类型</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">相关订单</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(log.createTime) }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-500 max-w-md truncate">
                {{ log.content }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span 
                  :class="{
                    'px-2 py-1 rounded-full text-xs font-medium': true,
                    'bg-blue-100 text-blue-800': log.type === 'ORDER',
                    'bg-green-100 text-green-800': log.type === 'LOGISTICS',
                    'bg-purple-100 text-purple-800': log.type === 'SYSTEM',
                    'bg-yellow-100 text-yellow-800': log.type === 'OTHER'
                  }"
                >
                  {{ getLogTypeName(log.type) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ log.orderNumber || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button 
                  @click="viewLogDetail(log)" 
                  class="text-indigo-600 hover:text-indigo-900 mr-3"
                >
                  查看
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 分页 -->
        <div class="px-6 py-4 flex items-center justify-between border-t border-gray-200">
          <div class="flex-1 flex justify-between sm:hidden">
            <button 
              @click="changePage(currentPage - 1)" 
              :disabled="currentPage === 1"
              :class="{
                'px-4 py-2 border rounded-md text-sm font-medium': true,
                'bg-white text-gray-700 border-gray-300 hover:bg-gray-50': currentPage !== 1,
                'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed': currentPage === 1
              }"
            >
              上一页
            </button>
            <button 
              @click="changePage(currentPage + 1)" 
              :disabled="currentPage === totalPages"
              :class="{
                'px-4 py-2 ml-3 border rounded-md text-sm font-medium': true,
                'bg-white text-gray-700 border-gray-300 hover:bg-gray-50': currentPage !== totalPages,
                'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed': currentPage === totalPages
              }"
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
                  @click="changePage(1)" 
                  :disabled="currentPage === 1"
                  :class="{
                    'relative inline-flex items-center px-2 py-2 rounded-l-md border text-sm font-medium': true,
                    'bg-white text-gray-500 border-gray-300 hover:bg-gray-50': currentPage !== 1,
                    'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed': currentPage === 1
                  }"
                >
                  <span class="sr-only">首页</span>
                  <span class="material-icons text-sm">first_page</span>
                </button>
                <button 
                  @click="changePage(currentPage - 1)" 
                  :disabled="currentPage === 1"
                  :class="{
                    'relative inline-flex items-center px-2 py-2 border text-sm font-medium': true,
                    'bg-white text-gray-500 border-gray-300 hover:bg-gray-50': currentPage !== 1,
                    'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed': currentPage === 1
                  }"
                >
                  <span class="sr-only">上一页</span>
                  <span class="material-icons text-sm">chevron_left</span>
                </button>
                <button 
                  v-for="page in displayedPages" 
                  :key="page" 
                  @click="changePage(page)" 
                  :class="{
                    'relative inline-flex items-center px-4 py-2 border text-sm font-medium': true,
                    'bg-orange-50 text-orange-600 border-orange-500': page === currentPage,
                    'bg-white text-gray-700 border-gray-300 hover:bg-gray-50': page !== currentPage
                  }"
                >
                  {{ page }}
                </button>
                <button 
                  @click="changePage(currentPage + 1)" 
                  :disabled="currentPage === totalPages"
                  :class="{
                    'relative inline-flex items-center px-2 py-2 border text-sm font-medium': true,
                    'bg-white text-gray-500 border-gray-300 hover:bg-gray-50': currentPage !== totalPages,
                    'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed': currentPage === totalPages
                  }"
                >
                  <span class="sr-only">下一页</span>
                  <span class="material-icons text-sm">chevron_right</span>
                </button>
                <button 
                  @click="changePage(totalPages)" 
                  :disabled="currentPage === totalPages"
                  :class="{
                    'relative inline-flex items-center px-2 py-2 rounded-r-md border text-sm font-medium': true,
                    'bg-white text-gray-500 border-gray-300 hover:bg-gray-50': currentPage !== totalPages,
                    'bg-gray-100 text-gray-400 border-gray-300 cursor-not-allowed': currentPage === totalPages
                  }"
                >
                  <span class="sr-only">尾页</span>
                  <span class="material-icons text-sm">last_page</span>
                </button>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 日志详情对话框 -->
    <div v-if="showDetailDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-bold text-gray-800">日志详情</h2>
            <button @click="showDetailDialog = false" class="text-gray-500 hover:text-gray-700">
              <span class="material-icons">close</span>
            </button>
          </div>
          <div class="space-y-4">
            <div>
              <p class="text-sm text-gray-500">创建时间</p>
              <p class="text-base">{{ formatDateTime(selectedLog.createTime) }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">日志类型</p>
              <p class="text-base">
                <span 
                  :class="{
                    'px-2 py-1 rounded-full text-xs font-medium': true,
                    'bg-blue-100 text-blue-800': selectedLog.type === 'ORDER',
                    'bg-green-100 text-green-800': selectedLog.type === 'LOGISTICS',
                    'bg-purple-100 text-purple-800': selectedLog.type === 'SYSTEM',
                    'bg-yellow-100 text-yellow-800': selectedLog.type === 'OTHER'
                  }"
                >
                  {{ getLogTypeName(selectedLog.type) }}
                </span>
              </p>
            </div>
            <div v-if="selectedLog.orderNumber">
              <p class="text-sm text-gray-500">相关订单</p>
              <p class="text-base">{{ selectedLog.orderNumber }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">操作员</p>
              <p class="text-base">{{ selectedLog.operatorName || '-' }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">日志内容</p>
              <p class="text-base whitespace-pre-line">{{ selectedLog.content }}</p>
            </div>
          </div>
          <div class="mt-6 flex justify-end">
            <button 
              @click="showDetailDialog = false" 
              class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加日志对话框 -->
    <div v-if="showAddDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full">
        <div class="p-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-bold text-gray-800">添加工作日志</h2>
            <button @click="showAddDialog = false" class="text-gray-500 hover:text-gray-700">
              <span class="material-icons">close</span>
            </button>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">日志类型</label>
              <select 
                v-model="newLog.type" 
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
                <option value="">请选择日志类型</option>
                <option value="ORDER">订单操作</option>
                <option value="LOGISTICS">物流操作</option>
                <option value="SYSTEM">系统操作</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div v-if="newLog.type === 'ORDER' || newLog.type === 'LOGISTICS'">
              <label class="block text-sm font-medium text-gray-700 mb-1">相关订单号</label>
              <input 
                type="text" 
                v-model="newLog.orderNumber" 
                placeholder="请输入订单号" 
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              >
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">日志内容</label>
              <textarea 
                v-model="newLog.content" 
                rows="5" 
                placeholder="请输入日志内容" 
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              ></textarea>
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button 
              @click="showAddDialog = false" 
              class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
            >
              取消
            </button>
            <button 
              @click="submitNewLog" 
              class="px-4 py-2 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
              :disabled="isSubmitting"
            >
              {{ isSubmitting ? '提交中...' : '提交' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getWorkLogs, addWorkLog, getWorkLogDetail } from '@/api/workLogs';
import { useUserStore } from '../../stores/user'; // 导入用户store

// 初始化用户store
const userStore = useUserStore();

// 状态变量
const logs = ref([]);
const isLoading = ref(false);
const isSubmitting = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const searchQuery = ref('');
const startDate = ref('');
const endDate = ref('');
const showDetailDialog = ref(false);
const showAddDialog = ref(false);
const selectedLog = ref({});
const newLog = ref({
  type: '',
  content: '',
  orderNumber: ''
});

// 员工信息
const staffInfo = ref({
  id: null,
  name: '',
  storeId: null,
  storeName: ''
});

// 计算属性
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));

const displayedPages = computed(() => {
  const maxPagesToShow = 5;
  const range = [];
  
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

// 获取员工信息
const getStaffInfo = () => {
  // 优先使用Pinia store中的数据
  const userInfo = userStore.userInfo;
  if (userInfo && userInfo.id) {
    console.log('[getStaffInfo] Using Pinia store data:', JSON.stringify(userInfo));
    staffInfo.value = {
      id: String(userInfo.id), // 将ID转为字符串以保持精度
      name: userInfo.name || userInfo.username || '',
      storeId: userInfo.storeId,
      storeName: userInfo.storeName || '' // 确保包含storeName
    };
  } else {
    // 如果store为空（例如，刷新后store重新加载前），回退到localStorage
    console.warn('[getStaffInfo] Pinia store empty, falling back to localStorage.');
    const username = localStorage.getItem('username');
    const id = localStorage.getItem('userId');
    const name = localStorage.getItem('userName');
    const storeId = localStorage.getItem('storeId');
    const storeName = localStorage.getItem('storeName');
    
    // 从localStorage中获取完整的用户信息
    const userInfoStr = localStorage.getItem('userInfo');
    let localUserInfo = null;
    
    if (userInfoStr) {
      try {
        localUserInfo = JSON.parse(userInfoStr);
      } catch (error) {
        console.error('解析用户信息失败:', error);
      }
    }
    
    staffInfo.value = {
      id: localUserInfo?.id || id || null, // 直接使用字符串ID，不进行parseInt转换
      name: localUserInfo?.name || name || username || '',
      storeId: localUserInfo?.storeId || (storeId ? parseInt(storeId) : null),
      storeName: localUserInfo?.storeName || storeName || ''
    };
  }
  
  console.log('当前员工信息:', staffInfo.value);
  
  // 验证员工信息是否有效
  if (!staffInfo.value.id || !staffInfo.value.name) {
    console.error('[getStaffInfo] 员工信息无效:', JSON.stringify(staffInfo.value));
    return false;
  }
  
  return true;
};

// 获取日志列表
const fetchLogs = async () => {
  isLoading.value = true;
  
  try {
    // 检查是否有门店ID
    if (!staffInfo.value.storeId) {
      ElMessage.error('未获取到门店信息，请重新登录');
      isLoading.value = false;
      return;
    }
    
    // 调用实际的API获取日志列表
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      startDate: startDate.value,
      endDate: endDate.value,
      storeId: staffInfo.value.storeId, // 获取门店所有员工的日志
      staffId: staffInfo.value.id // 添加员工ID参数（用于验证权限）
    };
    
    console.log('获取工作日志，参数:', params);
    const res = await getWorkLogs(params);
    
    // 假设后端返回的数据结构为 { code: 200, data: { records: [...], total: ... } }
    logs.value = res.data?.records || [];
    totalItems.value = res.data?.total || 0;
    console.log(`获取到 ${logs.value.length} 条日志记录，总计 ${totalItems.value} 条`);
    isLoading.value = false;
  } catch (error) {
    console.error('获取日志列表失败:', error);
    ElMessage.error('获取日志列表失败');
    isLoading.value = false;
  }
};

// 生成模拟数据
const generateMockLogs = () => {
  const mockLogs = [];
  const types = ['ORDER', 'LOGISTICS', 'SYSTEM', 'OTHER'];
  const contents = [
    '处理了客户的快递取件请求',
    '更新了订单的物流状态为"已送达"',
    '系统维护，暂停服务2小时',
    '处理了客户投诉',
    '协助新员工熟悉业务流程'
  ];
  
  for (let i = 1; i <= 10; i++) {
    const type = types[Math.floor(Math.random() * types.length)];
    mockLogs.push({
      id: i,
      type: type,
      content: contents[Math.floor(Math.random() * contents.length)],
      orderNumber: (type === 'ORDER' || type === 'LOGISTICS') ? `EX${Math.floor(Math.random() * 1000000000)}` : null,
      operatorName: staffInfo.value.name,
      createTime: new Date(Date.now() - Math.floor(Math.random() * 30) * 24 * 60 * 60 * 1000)
    });
  }
  
  return mockLogs;
};

// 搜索日志
const searchLogs = () => {
  currentPage.value = 1;
  fetchLogs();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  startDate.value = '';
  endDate.value = '';
  currentPage.value = 1;
  fetchLogs();
};

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  fetchLogs();
};

// 查看日志详情
const viewLogDetail = (log) => {
  selectedLog.value = log;
  showDetailDialog.value = true;
};

// 添加新日志
const addNewLog = () => {
  newLog.value = {
    type: '',
    content: '',
    orderNumber: ''
  };
  showAddDialog.value = true;
};

// 提交新日志
const submitNewLog = async () => {
  // 表单验证
  if (!newLog.value.type) {
    ElMessage.error('请选择日志类型');
    return;
  }
  
  if ((newLog.value.type === 'ORDER' || newLog.value.type === 'LOGISTICS') && !newLog.value.orderNumber) {
    ElMessage.error('请输入相关订单号');
    return;
  }
  
  if (!newLog.value.content) {
    ElMessage.error('请输入日志内容');
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    // 构建日志数据
    const logData = {
      ...newLog.value,
      storeId: staffInfo.value.storeId,
      operatorId: staffInfo.value.id,
      operatorName: staffInfo.value.name
    };
    
    await addWorkLog(logData);
    ElMessage.success('日志添加成功');
    showAddDialog.value = false;
    fetchLogs(); // 刷新日志列表
  } catch (error) {
    console.error('添加日志失败:', error);
    ElMessage.error('添加日志失败');
  } finally {
    isSubmitting.value = false;
  }
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-';
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '-';
  const d = new Date(date);
  return `${formatDate(d)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
};

// 获取日志类型名称
const getLogTypeName = (type) => {
  const typeMap = {
    'ORDER': '订单操作',
    'LOGISTICS': '物流操作',
    'SYSTEM': '系统操作',
    'OTHER': '其他'
  };
  return typeMap[type] || type;
};

// 生命周期钩子
onMounted(() => {
  getStaffInfo();
  fetchLogs();
});
</script>