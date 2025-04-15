<template>
  <div> <!-- Add a single root wrapper div -->
    <div class="min-h-screen bg-gray-100 p-6">
      <div class="container mx-auto">
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">工作日志</h1>
        </div>
      
      <!-- 搜索和操作区域 -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-6">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex-grow md:flex-grow-0">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索日志内容" 
              class="w-full md:w-64 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
            >
          </div>
          
          <div class="flex items-center w-full md:w-auto">
            <div class="flex gap-4 w-full">
              <div class="w-1/2">
                <input 
                  type="date" 
                  v-model="startDate" 
                  placeholder="开始日期"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
              </div>
              <div class="w-1/2">
                <input 
                  type="date" 
                  v-model="endDate" 
                  placeholder="结束日期"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
              </div>
            </div>
          </div>
          
          <div class="flex items-center gap-2">
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
            <button 
              @click="addNewLog" 
              class="px-4 py-2 bg-green-500 text-white font-medium rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition-colors"
            >
              <span class="material-icons mr-1">add</span>
              添加日志
            </button>
            <button
              @click="exportToCsv"
              class="px-4 py-2 bg-red-500 text-white font-medium rounded-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-colors"
            >
              <span class="material-icons mr-1">download</span>
              导出CSV
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
        
        <div v-else class="p-4">
          <!-- 日志卡片列表 -->
          <div v-for="log in logs" :key="log.id" class="mb-6 bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow duration-200 log-card">
            <!-- 日志头部：时间和操作 -->
            <div class="flex justify-between items-center bg-gray-50 px-4 py-3 border-b border-gray-200">
              <div class="text-sm font-medium text-gray-600">
                <span class="material-icons text-gray-500 text-sm mr-1 align-text-bottom">event</span>
                {{ formatDate(log.createTime) }}
              </div>
              
              <button 
                @click="viewLogDetail(log)" 
                class="text-blue-500 hover:text-blue-700 text-sm flex items-center"
              >
                <span class="material-icons text-sm mr-1">visibility</span>
                查看详情
              </button>
            </div>
            
            <!-- 卡片内容区域 -->
             <div class="p-4">
               <!-- 相关订单信息 -->
               <div v-if="log.orderNumber" class="flex items-center mb-3">
                 <div class="font-bold text-lg text-orange-600">
                   订单 {{ log.orderNumber }}
                 </div>
               </div>
               
               <!-- 操作类型与内容 -->
               <div class="flex flex-wrap gap-2 mb-3">
                 <div class="flex items-center">
                   <span class="font-medium">{{ log.operatorName || staffInfo.name }}</span>
                   <span class="mx-1">进行了</span>
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
                 </div>
               </div>
               
               <!-- 日志内容 -->
               <div class="text-gray-700 text-sm bg-gray-50 p-3 rounded-md border border-gray-100">
                 {{ formatLogContent(log.content) }}
               </div>
             </div>
           </div>
         </div>
         
         <!-- 分页 -->
        <div class="px-6 py-4 flex justify-between items-center border-t border-gray-200 mt-4">
            <div class="text-sm text-gray-700">
              共 <span class="font-medium">{{ totalItems }}</span> 条记录
            </div>
            <div class="flex space-x-2">
              <button 
                @click="changePage(currentPage - 1)" 
                :disabled="currentPage === 1"
                class="px-3 py-1 rounded-md bg-gray-200 text-gray-700 disabled:opacity-50 flex items-center"
              >
                <span class="material-icons text-sm mr-1">chevron_left</span>
                上一页
              </button>
              <button 
                @click="changePage(currentPage + 1)" 
                :disabled="currentPage === totalPages"
                class="px-3 py-1 rounded-md bg-gray-200 text-gray-700 disabled:opacity-50 flex items-center"
              >
                下一页
                <span class="material-icons text-sm ml-1">chevron_right</span>
              </button>
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
              <p class="text-base whitespace-pre-line">{{ formatLogContent(selectedLog.content) }}</p>
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
  </div> <!-- Close the single root wrapper div -->
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getWorkLogs, addWorkLog, getWorkLogDetail } from '@/api/workLogs';
import { exportStaffLogsCsv } from '@/api/logs'; // Import the staff export function
import { useUserStore } from '../../stores/user';

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
    
    // 输出完整的API响应，查看实际数据结构
    console.log('API响应完整数据:', res);
    
    // 检查响应结构
    if (res.records) {
      // 如果records直接在res对象上
      logs.value = res.records.map(log => ({
        id: log.id,
        type: log.operationType === '添加物流信息' ? 'LOGISTICS' : 
              log.operationType === '添加订单' ? 'ORDER' : 
              log.operationType === '系统操作' ? 'SYSTEM' : 'OTHER',
        content: formatLogContent(log.operationParams || log.operationResult || ''),
        orderNumber: log.orderNumber || (log.operationParams && log.operationParams.includes('订单ID:') ? 
                    log.operationParams.split('订单ID:')[1].split(',')[0].trim() : null),
        operatorName: log.operatorName || '',
        createTime: log.createTime || log.operationTime
      }));
      totalItems.value = res.total || 0;
    } else if (res.data) {
      // 如果records在res.data对象上
      logs.value = res.data.records.map(log => ({
        id: log.id,
        type: log.operationType === '添加物流信息' ? 'LOGISTICS' : 
              log.operationType === '添加订单' ? 'ORDER' : 
              log.operationType === '系统操作' ? 'SYSTEM' : 'OTHER',
        content: formatLogContent(log.operationParams || log.operationResult || ''),
        orderNumber: log.orderNumber || (log.operationParams && log.operationParams.includes('订单ID:') ? 
                    log.operationParams.split('订单ID:')[1].split(',')[0].trim() : null),
        operatorName: log.operatorName || '',
        createTime: log.createTime || log.operationTime
      }));
      totalItems.value = res.data.total || 0;
    } else {
      // 如果是其他结构，尝试直接使用res作为数组
      logs.value = Array.isArray(res) ? res.map(log => ({
        id: log.id,
        type: log.operationType === '添加物流信息' ? 'LOGISTICS' : 
              log.operationType === '添加订单' ? 'ORDER' : 
              log.operationType === '系统操作' ? 'SYSTEM' : 'OTHER',
        content: formatLogContent(log.operationParams || log.operationResult || ''),
        orderNumber: log.orderNumber || (log.operationParams && log.operationParams.includes('订单ID:') ? 
                    log.operationParams.split('订单ID:')[1].split(',')[0].trim() : null),
        operatorName: log.operatorName || '',
        createTime: log.createTime || log.operationTime
      })) : [];
      totalItems.value = Array.isArray(res) ? res.length : 0;
      console.warn('未识别的API响应结构，尝试直接使用响应作为数组');
    }
    
    console.log(`获取到 ${logs.value.length} 条日志记录，总计 ${totalItems.value} 条`);
    console.log('日志数据示例:', logs.value.length > 0 ? logs.value[0] : '无数据');
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

// 导出日志为CSV
const exportToCsv = async () => {
  if (!staffInfo.value || !staffInfo.value.id) {
    ElMessage.error('无法获取当前员工信息，无法导出');
    return;
  }
  try {
    const params = {
      keyword: searchQuery.value,
      startDate: startDate.value,
      endDate: endDate.value,
      storeId: staffInfo.value.storeId,
      staffId: staffInfo.value.id,
      operatorId: staffInfo.value.id // 确保包含操作员ID
    };
    
    ElMessage.info('正在生成CSV文件...');
    const response = await exportStaffLogsCsv(params);
    
    // 创建Blob对象
    const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
    
    // 创建下载链接
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `工作日志_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // 释放URL对象
    URL.revokeObjectURL(link.href);
    
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出日志失败:', error);
    ElMessage.error('导出日志失败，请检查网络或联系管理员');
  }
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

// 获取订单状态名称
const getOrderStatusName = (status) => {
  const statusMap = {
    '0': '待接单',
    '1': '已接单',
    '2': '运输中',
    '3': '已送达',
    '4': '已完成',
    '5': '已取消'
  };
  return statusMap[status] || status;
};

// 处理日志内容，将订单状态数字转换为文字
const formatLogContent = (content) => {
  if (!content) return '';
  
  // 匹配"状态: 数字"的模式
  return content.replace(/状态:\s*(\d+)/g, (match, statusCode) => {
    return `状态: ${getOrderStatusName(statusCode)}`;
  });
};

// 生命周期钩子
onMounted(() => {
  getStaffInfo();
  fetchLogs();
});

</script>

<style scoped>
/* 自定义样式 */
.material-icons {
  font-family: 'Material Icons';
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}

/* 日志卡片悬停效果 */
.log-card {
  transition: all 0.3s ease;
}

.log-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
</style>
