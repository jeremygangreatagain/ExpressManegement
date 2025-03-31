<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">工作日志</h1>
      <div class="flex space-x-2">
        <button 
          @click="exportToExcel" 
          class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md flex items-center"
        >
          <span class="material-icons mr-1">download</span>
          导出Excel
        </button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="bg-white p-4 rounded-md shadow mb-6">
      <div class="flex flex-wrap items-center gap-4">
        <div class="flex items-center">
          <input 
            v-model="searchKeyword"
            type="text"
            placeholder="搜索操作内容或操作人"
            class="border border-gray-300 rounded-md px-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
        
        <div class="flex items-center">
          <el-select 
            v-model="operationType"
            placeholder="全部操作类型"
            clearable
            class="min-w-[240px]"
            style="width: 240px; z-index: 1000;"
            popper-class="operation-type-dropdown"
            :teleported="true"
            :popper-options="{
              modifiers: [{
                name: 'computeStyles',
                options: {
                  gpuAcceleration: false
                }
              }]
            }"
          >
            <el-option
              v-for="type in operationTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            ></el-option>
          </el-select>
        </div>
        
        <div class="flex items-center">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-72"
            :teleported="true"
            :popper-options="{
              modifiers: [{
                name: 'computeStyles',
                options: {
                  gpuAcceleration: false
                }
              }]
            }"
          ></el-date-picker>
        </div>
        
        <div class="flex items-center">
          <button
            @click="fetchLogs(true)" 
            class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md"
          >
            搜索
          </button>
          <button 
            @click="resetSearch" 
            class="ml-2 bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-md"
          >
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 时间线日志列表 -->
    <div class="bg-white rounded-md shadow p-6">
      <div v-if="logs.length === 0" class="text-center py-10 text-gray-500">
        暂无日志记录
      </div>
      
      <div v-else>
        <!-- 日志项 -->
        <div v-for="log in logs" :key="log.id" class="mb-6 bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow duration-200 log-card"> 
          <!-- 日志头部：时间和订单信息 -->
          <div class="flex justify-between items-center bg-gray-50 px-4 py-3 border-b border-gray-200">
            <!-- 左侧：日期和时间，更醒目 -->
            <div class="text-sm font-medium text-gray-600">
              <span class="material-icons text-gray-500 text-sm mr-1 align-text-bottom">event</span>
              {{ formatDate(log.createTime) }}
            </div>
            
            <!-- 右侧：查看详情按钮 -->
            <button 
              @click="viewLogDetail(log.id)" 
              class="text-blue-500 hover:text-blue-700 text-sm flex items-center"
            >
              <span class="material-icons text-sm mr-1">visibility</span>
              查看详情
            </button>
          </div>
          
          <div class="p-4">
            <!-- 订单信息 (尝试提取) - 更加醒目 -->
            <div class="flex items-center mb-3">
              <div class="font-bold text-lg text-orange-600">
                {{ getRelatedInfo(log.operationParams) }}
              </div>
            </div>
            
            <!-- 操作主体与类型 -->
            <div class="flex flex-wrap gap-2 mb-3">
              <div class="flex items-center">
                <span class="font-medium">{{ log.operatorName }}</span>
                <span class="mx-1">进行了</span>
                <span class="font-medium text-blue-600">{{ log.operationType }}</span>
              </div>
              
              <!-- 操作标签 -->
              <div class="flex flex-wrap gap-1 mt-1">
                <span 
                  v-for="tag in getOperationTags(log)" 
                  :key="tag" 
                  :class="getTagClass(tag)"
                  class="px-2 py-0.5 rounded-full text-xs font-medium"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
            
            <!-- 操作参数详情 -->
            <div class="text-gray-700 text-sm bg-gray-50 p-3 rounded-md border border-gray-100">
              {{ log.operationParams }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="px-6 py-4 flex justify-between items-center border-t border-gray-200 mt-4 bg-white rounded-md shadow">
      <div class="text-sm text-gray-700">
        共 <span class="font-medium">{{ total }}</span> 条记录
      </div>
      <div class="flex space-x-2">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 1"
          class="px-3 py-1 rounded-md bg-gray-200 text-gray-700 disabled:opacity-50"
        >
          上一页
        </button>
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage * pageSize >= total"
          class="px-3 py-1 rounded-md bg-gray-200 text-gray-700 disabled:opacity-50"
        >
          下一页
        </button>
      </div>
    </div>
  </div>

  <!-- 日志详情对话框 -->
  <el-dialog
    v-model="dialogVisible"
    title="日志详情"
    width="600px" 
    :append-to-body="true"
    :destroy-on-close="true"
    :close-on-click-modal="false"
    :z-index="10000"
  >
    <div v-if="currentLog" class="p-4 space-y-4">
      <div>
        <div class="text-lg font-semibold mb-2 border-b pb-1">基本信息</div>
        <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
          <div class="text-gray-600 font-medium">操作类型：</div>
          <div class="col-span-2">{{ currentLog.operationType }}</div>
          <div class="text-gray-600 font-medium">操作时间：</div>
          <div class="col-span-2">{{ formatDate(currentLog.createTime) }}</div>
          <div class="text-gray-600 font-medium">操作人：</div>
          <div class="col-span-2">{{ currentLog.operatorName }} ({{ currentLog.operatorRole }})</div>
          <div class="text-gray-600 font-medium">IP地址：</div>
          <div class="col-span-2">{{ currentLog.operationIp || 'N/A' }}</div> 
        </div>
      </div>
      <div>
        <div class="text-lg font-semibold mb-2 border-b pb-1">操作参数/内容</div>
        <div class="bg-gray-50 p-3 rounded text-sm whitespace-pre-wrap break-words">
          {{ currentLog.operationParams || '无' }}
        </div>
      </div>
       <!-- Optionally display operationResult if available -->
       <div v-if="currentLog.operationResult">
        <div class="text-lg font-semibold mb-2 border-b pb-1">操作结果</div>
        <div class="bg-gray-50 p-3 rounded text-sm whitespace-pre-wrap break-words">
          {{ currentLog.operationResult }}
        </div>
      </div>
    </div>
     <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, onMounted, watch } from 'vue'; // Import watch
import { ElMessage } from 'element-plus';
import { getOperationLogs, exportLogsExcel, getLogTypeOptions, getLogDetail } from '@/api/logs';

export default {
  name: 'WorkLog',
  setup() {
    const logs = ref([]);
    const total = ref(0);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const searchKeyword = ref('');
    const operationType = ref('');
    const dateRange = ref([]);
    const operationTypes = ref([]); // Expects array of { label: string, value: string }
    const dialogVisible = ref(false);
    const currentLog = ref(null);
    
    // 获取日志列表
    const fetchLogs = async (isSearchTrigger = false) => { 
      // Reset to page 1 if triggered by search button/enter key or filter change
      if (isSearchTrigger) {
        currentPage.value = 1;
      }
      try {
        const params = {
          keyword: searchKeyword.value,
          operationType: operationType.value,
          startDate: dateRange.value && dateRange.value[0] ? dateRange.value[0] : null,
          endDate: dateRange.value && dateRange.value[1] ? dateRange.value[1] : null,
          page: currentPage.value,
          size: pageSize.value
        };
        
        const res = await getOperationLogs(params);
        // Ensure ID is treated as string if it comes as number due to previous fix
        logs.value = (res.data?.records || []).map(log => ({
          ...log,
          id: String(log.id) 
        }));
        total.value = res.data?.total || 0;
      } catch (error) {
        console.error('获取日志列表失败:', error);
        ElMessage.error('获取日志列表失败');
      }
    };
    
    // 获取日志类型选项
    const fetchLogTypes = async () => {
      try {
        const res = await getLogTypeOptions(); 
        // 直接使用API返回的数据，确保格式正确
        if (Array.isArray(res.data) && res.data.length > 0) {
          // 确保每个选项都是正确的对象格式
          operationTypes.value = res.data.map(item => {
            // 如果已经是正确的格式，直接返回
            if (typeof item === 'object' && item !== null && 'label' in item && 'value' in item) {
              return item;
            }
            // 如果是字符串，转换为对象格式
            if (typeof item === 'string') {
              return { label: item, value: item };
            }
            // 其他情况，尝试从JSON字符串解析
            try {
              if (typeof item === 'string') {
                const parsed = JSON.parse(item);
                if (parsed && typeof parsed === 'object' && 'label' in parsed) {
                  return parsed;
                }
              }
            } catch (e) {
              // 解析失败，忽略
            }
            // 默认情况，使用字符串表示
            return { label: String(item), value: String(item) };
          });
        } else {
          // 如果API返回空数组或非数组，使用默认值
          setDefaultOperationTypes();
        }
      } catch (error) {
        console.error('获取日志类型选项失败:', error);
        setDefaultOperationTypes(); // 出错时使用默认值
      }
    };

    // Helper function to set default operation types
    const setDefaultOperationTypes = () => {
       operationTypes.value = [
          // 订单相关操作
          { label: '创建订单', value: '创建订单' },
          { label: '更新订单', value: '更新订单' },
          { label: '删除订单', value: '删除订单' },
          { label: '更改订单状态', value: '更改订单状态' },
          { label: '添加物流信息', value: '添加物流信息' },
          { label: '完成订单', value: '完成订单' },
          
          // 用户相关操作
          { label: '用户登录', value: '用户登录' },
          { label: '用户注册', value: '用户注册' },
          { label: '创建用户', value: '创建用户' },
          { label: '更新用户', value: '更新用户' },
          { label: '删除用户', value: '删除用户' },
          { label: '批量删除用户', value: '批量删除用户' },
          
          // 员工相关操作
          { label: '创建员工', value: '创建员工' },
          { label: '更新员工', value: '更新员工' },
          { label: '删除员工', value: '删除员工' },
          { label: '分配员工', value: '分配员工' },
          
          // 门店相关操作
          { label: '创建门店', value: '创建门店' },
          { label: '更新门店', value: '更新门店' },
          { label: '删除门店', value: '删除门店' },
          
          // 系统操作
          { label: '系统维护', value: '系统维护' },
          { label: '数据备份', value: '数据备份' },
          { label: '数据恢复', value: '数据恢复' },
          { label: '系统配置', value: '系统配置' }
      ];
    };
    
    // 重置搜索条件
    const resetSearch = () => {
      searchKeyword.value = '';
      operationType.value = '';
      dateRange.value = [];
      fetchLogs(true); // Trigger fetch with reset page
    };
    
    // 切换页码
    const changePage = (page) => {
      if (page < 1 || page > Math.ceil(total.value / pageSize.value)) return;
      currentPage.value = page;
      fetchLogs(); // Fetch logs for the new page without resetting filters
    };
    
    // 导出Excel
    const exportToExcel = async () => {
      try {
        const params = {
          keyword: searchKeyword.value,
          operationType: operationType.value,
          startDate: dateRange.value && dateRange.value[0] ? dateRange.value[0] : null,
          endDate: dateRange.value && dateRange.value[1] ? dateRange.value[1] : null
        };
        
        ElMessage.info('正在生成Excel文件...');
        const res = await exportLogsExcel(params);
        
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `工作日志_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(link); 
        link.click();
        document.body.removeChild(link); 
        URL.revokeObjectURL(link.href); 
        
        ElMessage.success('导出成功');
      } catch (error) {
        console.error('导出Excel失败:', error);
        ElMessage.error('导出Excel失败，请检查网络或联系管理员');
      }
    };
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return '无效日期';
        }
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
      } catch (e) {
        console.error("Error formatting date:", dateString, e);
        return '日期格式错误';
      }
    };
    
    // 从操作参数中提取关键信息 (如订单号)
    const getRelatedInfo = (params) => {
      if (!params) return '未知';
      let match = params.match(/订单号[\s:]*([A-Za-z0-9]+)/i);
      if (match && match[1]) return `订单 ${match[1]}`;
      match = params.match(/订单ID[\s:]*(\d+)/i);
      if (match && match[1]) return `订单 ${match[1]}`;
      match = params.match(/用户名[\s:]*(\S+)/i);
      if (match && match[1]) return `用户 ${match[1]}`;
      match = params.match(/员工名[\s:]*(\S+)/i);
      if (match && match[1]) return `员工 ${match[1]}`;
      match = params.match(/门店名[\s:]*(\S+)/i);
      if (match && match[1]) return `门店 ${match[1]}`;
      return '相关操作'; 
    };
    
    // 获取操作标签
    const getOperationTags = (log) => {
      const tags = [];
      if (log.operationType) {
        tags.push(log.operationType);
      }
      if (log.operatorRole) {
        tags.push(log.operatorRole);
      }
      return tags;
    };
    
    // 获取标签样式
    const getTagClass = (tag) => {
      const tagMap = {
        '创建订单': 'bg-green-100 text-green-800',
        '更新订单': 'bg-blue-100 text-blue-800',
        '删除订单': 'bg-red-100 text-red-800',
        '更改订单状态': 'bg-yellow-100 text-yellow-800',
        '添加物流信息': 'bg-purple-100 text-purple-800',
        '完成订单': 'bg-teal-100 text-teal-800',
        '用户登录': 'bg-indigo-100 text-indigo-800',
        '用户注册': 'bg-pink-100 text-pink-800',
        '创建用户': 'bg-lime-100 text-lime-800',
        '更新用户': 'bg-cyan-100 text-cyan-800',
        '删除用户': 'bg-rose-100 text-rose-800',
        '创建员工': 'bg-emerald-100 text-emerald-800',
        '更新员工': 'bg-sky-100 text-sky-800',
        '删除员工': 'bg-fuchsia-100 text-fuchsia-800',
        '分配员工': 'bg-violet-100 text-violet-800',
        '创建门店': 'bg-amber-100 text-amber-800',
        '更新门店': 'bg-orange-100 text-orange-800',
        '管理员': 'bg-gray-200 text-gray-700',
        '员工': 'bg-blue-200 text-blue-700',
        '用户': 'bg-green-200 text-green-700',
      };
      return tagMap[tag] || 'bg-gray-100 text-gray-800'; 
    };
    
    // 查看日志详情
    const viewLogDetail = async (id) => {
      const logId = String(id); 
      try {
        const res = await getLogDetail(logId);
        if (res.data) {
          currentLog.value = { ...res.data, id: String(res.data.id) }; 
          dialogVisible.value = true;
        } else {
          ElMessage.warning('未找到日志详情');
        }
      } catch (error) {
        console.error(`获取日志详情失败 (ID: ${logId}):`, error);
        ElMessage.error('获取日志详情失败');
      }
    };
    
    // Watch for changes in searchKeyword and fetch logs (实时搜索)
    watch(searchKeyword, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        fetchLogs(true);
      }
    });
    
    // Watch for changes in operationType and fetch logs
    watch(operationType, (newValue, oldValue) => {
      if (newValue !== oldValue) { 
        fetchLogs(true); 
      }
    });

    // Watch for changes in dateRange and fetch logs
     watch(dateRange, (newValue, oldValue) => {
       if (JSON.stringify(newValue) !== JSON.stringify(oldValue)) {
         fetchLogs(true); 
       }
     }, { deep: true }); 

    onMounted(() => {
      fetchLogs(); 
      fetchLogTypes();
    });
    
    return {
      logs,
      total,
      currentPage,
      pageSize,
      searchKeyword,
      operationType,
      dateRange,
      operationTypes,
      dialogVisible,
      currentLog,
      fetchLogs, 
      resetSearch,
      changePage, 
      exportToExcel,
      formatDate,
      getRelatedInfo, 
      getOperationTags,
      getTagClass,
      viewLogDetail
    };
  }
};
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

/* 下拉框样式修复 */
:deep(.el-select) {
  width: 240px;
}

:deep(.el-select .el-input) {
  width: 100%;
}

:deep(.operation-type-dropdown) {
  z-index: 9999 !important;
}

/* 确保下拉选项可见 */
:deep(.el-select-dropdown) {
  z-index: 9999 !important;
  max-height: 300px;
  overflow-y: auto;
}

/* 修复下拉菜单显示问题 */
:deep(.el-popper) {
  z-index: 9999 !important;
}

:deep(.el-select__popper) {
  z-index: 9999 !important;
}

:deep(.el-picker__popper) {
  z-index: 9999 !important;
}

:deep(.el-picker-panel) {
  z-index: 9999 !important;
}

:deep(.el-overlay) {
  z-index: 9000 !important;
}

/* 强制提高下拉菜单层级 */
:deep(.el-popper.is-pure) {
  z-index: 10001 !important;
}

:deep(.el-select__popper.el-popper) {
  z-index: 10001 !important;
}

:deep(.el-picker__popper.el-popper) {
  z-index: 10001 !important;
}

/* 响应式调整 */
@media (max-width: 768px) {
  :deep(.el-select) {
    width: 100%;
  }
}
</style>
