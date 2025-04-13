<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">审核管理</h1>
      <div class="flex space-x-2">
        <button 
          @click="refreshAudits" 
          class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md flex items-center"
        >
          <span class="material-icons mr-1">refresh</span>
          刷新
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
            placeholder="搜索申请人或订单号"
            class="border border-gray-300 rounded-md px-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
        </div>
        
        <div class="flex items-center">
          <el-select 
            v-model="auditType"
            placeholder="全部审核类型"
            clearable
            class="min-w-[240px]"
            style="width: 240px; z-index: 1000;"
            popper-class="audit-type-dropdown"
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
              v-for="type in auditTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            ></el-option>
          </el-select>
        </div>
        
        <div class="flex items-center">
          <el-select 
            v-model="auditStatus"
            placeholder="全部状态"
            clearable
            class="min-w-[180px]"
            style="width: 180px; z-index: 1000;"
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
              v-for="status in statusOptions"
              :key="status.value"
              :label="status.label"
              :value="status.value"
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
            @click="fetchAudits(true)" 
            class="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md"
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

    <!-- 审核列表 -->
    <div class="bg-white rounded-md shadow overflow-hidden">
      <div v-if="audits.length === 0" class="text-center py-10 text-gray-500">
        暂无审核记录
      </div>
      
      <div v-else>
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                申请编号
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                申请类型
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                申请人
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                申请时间
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                状态
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="audit in audits" :key="audit.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                {{ audit.id }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="getTypeClass(audit.type)"
                >
                  {{ audit.type || '订单删除申请' }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ audit.staffName || audit.applicantName }} ({{ audit.applicantRole || '快递员' }})
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(audit.createTime) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="getStatusClass(audit.status)"
                >
                  {{ getStatusText(audit.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button 
                  @click="viewAuditDetail(audit.id)" 
                  class="text-blue-600 hover:text-blue-900 mr-3"
                >
                  查看
                </button>
                <button 
                  v-if="audit.status === 'PENDING' || audit.status === 0"
                  @click="handleApprove(audit.id)" 
                  class="text-green-600 hover:text-green-900 mr-3"
                >
                  通过
                </button>
                <button 
                  v-if="audit.status === 'PENDING' || audit.status === 0"
                  @click="handleReject(audit.id)" 
                  class="text-red-600 hover:text-red-900"
                >
                  拒绝
                </button>
              </td>
            </tr>
          </tbody>
        </table>
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

  <!-- 审核详情对话框 -->
  <el-dialog
    v-model="dialogVisible"
    title="审核详情"
    width="600px" 
    :append-to-body="true"
    :destroy-on-close="true"
    :close-on-click-modal="false"
    :z-index="10000"
  >
    <div v-if="currentAudit" class="p-4 space-y-4">
      <div>
        <div class="text-lg font-semibold mb-2 border-b pb-1">基本信息</div>
        <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
          <div class="text-gray-600 font-medium">申请编号：</div>
          <div class="col-span-2">{{ currentAudit.id }}</div>
          <div class="text-gray-600 font-medium">申请类型：</div>
          <div class="col-span-2">
            <span 
              class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
              :class="getTypeClass(currentAudit.type)"
            >
              {{ currentAudit.type }}
            </span>
          </div>
          <div class="text-gray-600 font-medium">申请时间：</div>
          <div class="col-span-2">{{ formatDate(currentAudit.createTime) }}</div>
          <div class="text-gray-600 font-medium">申请人：</div>
          <div class="col-span-2">{{ currentAudit.applicantName }} ({{ currentAudit.applicantRole }})</div>
          <div class="text-gray-600 font-medium">状态：</div>
          <div class="col-span-2">
            <span 
              class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
              :class="getStatusClass(currentAudit.status)"
            >
              {{ getStatusText(currentAudit.status) }}
            </span>
          </div>
          <div v-if="currentAudit.status !== 'PENDING'" class="text-gray-600 font-medium">处理时间：</div>
          <div v-if="currentAudit.status !== 'PENDING'" class="col-span-2">{{ formatDate(currentAudit.updateTime) }}</div>
          <div v-if="currentAudit.status !== 'PENDING'" class="text-gray-600 font-medium">处理人：</div>
          <div v-if="currentAudit.status !== 'PENDING'" class="col-span-2">{{ currentAudit.handlerName || '系统自动' }}</div>
        </div>
      </div>
      
      <div>
        <div class="text-lg font-semibold mb-2 border-b pb-1">申请内容</div>
        <div class="bg-gray-50 p-3 rounded text-sm whitespace-pre-wrap break-words">
          {{ currentAudit.content || '无' }}
        </div>
      </div>
      
      <div v-if="currentAudit.relatedOrderId">
        <div class="text-lg font-semibold mb-2 border-b pb-1">相关订单</div>
        <div class="bg-gray-50 p-3 rounded text-sm">
          <div class="flex justify-between items-center">
            <span>订单编号: {{ currentAudit.relatedOrderId }}</span>
            <button 
              @click="viewOrderDetail(currentAudit.relatedOrderId)" 
              class="text-blue-500 hover:text-blue-700 text-sm flex items-center"
            >
              <span class="material-icons text-sm mr-1">visibility</span>
              查看订单
            </button>
          </div>
        </div>
      </div>
      
      <div v-if="currentAudit.status === 'REJECTED' && currentAudit.rejectReason">
        <div class="text-lg font-semibold mb-2 border-b pb-1">拒绝原因</div>
        <div class="bg-gray-50 p-3 rounded text-sm whitespace-pre-wrap break-words text-red-600">
          {{ currentAudit.rejectReason }}
        </div>
      </div>
      
      <!-- 审批操作区域 -->
      <div v-if="currentAudit.status === 'PENDING'" class="border-t pt-4 mt-4">
        <div class="text-lg font-semibold mb-2">审批操作</div>
        <div class="flex space-x-4">
          <button 
            @click="handleApprove(currentAudit.id, true)" 
            class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-md flex-1"
          >
            通过申请
          </button>
          <button 
            @click="handleReject(currentAudit.id, true)" 
            class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md flex-1"
          >
            拒绝申请
          </button>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 拒绝原因对话框 -->
  <el-dialog
    v-model="rejectDialogVisible"
    title="拒绝原因"
    width="500px"
    :append-to-body="true"
    :destroy-on-close="true"
    :close-on-click-modal="false"
    :z-index="10001"
  >
    <div class="p-4">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input 
            v-model="rejectForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请输入拒绝原因"
          ></el-input>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :disabled="!rejectForm.reason.trim()">确认拒绝</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAuditList, getAuditDetail, approveAudit, rejectAudit, getAuditTypeOptions } from '@/api/audit';

export default {
  name: 'AuditManagement',
  setup() {
    const audits = ref([]);
    const total = ref(0);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const searchKeyword = ref('');
    const auditType = ref('');
    const auditStatus = ref('');
    const dateRange = ref([]);
    const auditTypes = ref([]); // 审核类型选项
    const dialogVisible = ref(false);
    const currentAudit = ref(null);
    const rejectDialogVisible = ref(false);
    const rejectForm = ref({ reason: '', auditId: '' });
    
    // 状态选项
    const statusOptions = [
      { label: '待审核', value: 'PENDING' },
      { label: '已通过', value: 'APPROVED' },
      { label: '已拒绝', value: 'REJECTED' }
    ];
    
    // 获取审核列表
    const fetchAudits = async (isSearchTrigger = false) => { 
      // 如果是搜索触发，重置到第一页
      if (isSearchTrigger) {
        currentPage.value = 1;
      }
      try {
        const params = {
          keyword: searchKeyword.value,
          type: auditType.value,
          status: auditStatus.value,
          startDate: dateRange.value && dateRange.value[0] ? dateRange.value[0] : null,
          endDate: dateRange.value && dateRange.value[1] ? dateRange.value[1] : null,
          page: currentPage.value,
          size: pageSize.value
        };
        
        const res = await getAuditList(params);
        // 正确处理分页响应
        if (res.data && res.data.records) {
          audits.value = res.data.records.map(audit => ({
            ...audit,
            id: String(audit.id) // 保留 ID 转换
          }));
          total.value = res.data.total || 0;
          // 可选：如果后端返回当前页和大小，也进行更新
          // currentPage.value = res.data.current || currentPage.value;
          // pageSize.value = res.data.size || pageSize.value;
        } else {
          audits.value = [];
          total.value = 0;
          console.warn("获取审核列表响应数据结构不正确:", res);
        }
      } catch (error) {
        console.error('获取审核列表失败:', error);
        // 出错时也清空数据
        audits.value = [];
        total.value = 0;
        ElMessage.error('获取审核列表失败');
      }
    };
    
    // 获取审核类型选项
    const fetchAuditTypes = async () => {
      try {
        const res = await getAuditTypeOptions();
        if (Array.isArray(res.data) && res.data.length > 0) {
          auditTypes.value = res.data.map(item => {
            if (typeof item === 'object' && item !== null && 'label' in item && 'value' in item) {
              return item;
            }
            if (typeof item === 'string') {
              return { label: item, value: item };
            }
            return { label: String(item), value: String(item) };
          });
        } else {
          setDefaultAuditTypes();
        }
      } catch (error) {
        console.error('获取审核类型选项失败:', error);
        setDefaultAuditTypes(); // 出错时使用默认值
      }
    };

    // 设置默认审核类型
    const setDefaultAuditTypes = () => {
      auditTypes.value = [
        { label: '订单删除申请', value: '订单删除申请' },
        { label: '订单修改申请', value: '订单修改申请' },
        { label: '退款申请', value: '退款申请' },
        { label: '其他申请', value: '其他申请' }
      ];
    };
    
    // 重置搜索条件
    const resetSearch = () => {
      searchKeyword.value = '';
      auditType.value = '';
      auditStatus.value = '';
      dateRange.value = [];
      fetchAudits(true); // 触发搜索并重置页码
    };
    
    // 切换页码
    const changePage = (page) => {
      if (page < 1 || page > Math.ceil(total.value / pageSize.value)) return;
      currentPage.value = page;
      fetchAudits(); // 获取新页的数据，不重置筛选条件
    };
    
    // 刷新审核列表
    const refreshAudits = () => {
      fetchAudits(false); // 不重置页码
      ElMessage.success('刷新成功');
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
    
    // 获取状态文本
    const getStatusText = (status) => {
      // 处理数字状态值
      if (typeof status === 'number' || !isNaN(Number(status))) {
        const numStatus = Number(status);
        switch (numStatus) {
          case 0: return '待审核';
          case 1: return '已通过';
          case 2: return '已拒绝';
          default: return `未知状态(${status})`;
        }
      }
      
      // 处理字符串状态值
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝',
        '0': '待审核',
        '1': '已通过',
        '2': '已拒绝'
      };
      return statusMap[status] || `未知状态(${status})`;
    };
    
    // 获取状态样式
    const getStatusClass = (status) => {
      // 处理数字状态值
      if (typeof status === 'number' || !isNaN(Number(status))) {
        const numStatus = Number(status);
        switch (numStatus) {
          case 0: return 'bg-yellow-100 text-yellow-800'; // 待审核
          case 1: return 'bg-green-100 text-green-800';  // 已通过
          case 2: return 'bg-red-100 text-red-800';      // 已拒绝
          default: return 'bg-gray-100 text-gray-800';
        }
      }
      
      // 处理字符串状态值
      const statusMap = {
        'PENDING': 'bg-yellow-100 text-yellow-800',
        'APPROVED': 'bg-green-100 text-green-800',
        'REJECTED': 'bg-red-100 text-red-800',
        '0': 'bg-yellow-100 text-yellow-800',
        '1': 'bg-green-100 text-green-800',
        '2': 'bg-red-100 text-red-800'
      };
      return statusMap[status] || 'bg-gray-100 text-gray-800';
    };
    
    // 获取类型样式
    const getTypeClass = (type) => {
      const typeMap = {
        '订单删除申请': 'bg-red-500 text-white', // 更明显的红色背景
        '订单修改申请': 'bg-blue-400 text-white', // 更鲜明的蓝色
        '退款申请': 'bg-orange-400 text-white', // 更鲜明的橙色
        '其他申请': 'bg-purple-400 text-white', // 紫色背景
        '账户申请': 'bg-green-400 text-white', // 绿色背景
        '物流申请': 'bg-indigo-400 text-white', // 靛蓝色背景
        '投诉申请': 'bg-yellow-500 text-white' // 黄色背景
      };
      return typeMap[type] || 'bg-gray-400 text-white'; // 默认更深的灰色
    };
    
    // 查看审核详情
    const viewAuditDetail = async (id) => {
      try {
        const res = await getAuditDetail(id);
        if (res.data) {
          // 处理审核详情数据，确保数据格式正确
          const auditData = { ...res.data, id: String(res.data.id) };
          
          // 确保申请类型正确显示
          if (!auditData.type && auditData.auditType) {
            auditData.type = auditData.auditType;
          } else if (!auditData.type) {
            auditData.type = '订单删除申请'; // 默认类型
          }
          
          // 确保申请人信息正确显示
          if (!auditData.applicantName && auditData.staffName) {
            auditData.applicantName = auditData.staffName;
          }
          
          if (!auditData.applicantRole) {
            auditData.applicantRole = auditData.role || '快递员';
          }
          
          // 将reason字段映射到content字段，确保申请内容正确显示
          if (!auditData.content && auditData.reason) {
            auditData.content = auditData.reason;
          }
          
          // 设置当前审核数据并显示对话框
          currentAudit.value = auditData;
          dialogVisible.value = true;
        } else {
          ElMessage.warning('未找到审核详情');
        }
      } catch (error) {
        console.error(`获取审核详情失败 (ID: ${id}):`, error);
        ElMessage.error('获取审核详情失败');
      }
    };
    
    // 查看订单详情（可以跳转到订单管理页面）
    const viewOrderDetail = (orderId) => {
      // 这里可以实现跳转到订单详情页面的逻辑
      console.log('查看订单详情:', orderId);
      // 可以通过路由跳转或者打开新的对话框
    };
    
    // 处理审批通过
    const handleApprove = (id, fromDialog = false) => {
      ElMessageBox.confirm(
        '确定要通过此审核申请吗？',
        '审批确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await approveAudit(id, {});
          ElMessage.success('审批通过成功');
          // 如果是从详情对话框操作的，关闭对话框
          if (fromDialog) {
            dialogVisible.value = false;
          }
          // 刷新列表
          fetchAudits();
        } catch (error) {
          console.error('审批通过失败:', error);
          ElMessage.error('审批通过失败');
        }
      }).catch(() => {
        // 用户取消操作
      });
    };
    
    // 处理审批拒绝
    const handleReject = (id, fromDialog = false) => {
      rejectForm.value.auditId = id;
      rejectForm.value.reason = '';
      rejectDialogVisible.value = true;
    };
    
    // 确认拒绝
    const confirmReject = async () => {
      if (!rejectForm.value.reason.trim()) {
        ElMessage.warning('请输入拒绝原因');
        return;
      }
      
      try {
        await rejectAudit(rejectForm.value.auditId, { reason: rejectForm.value.reason });
        ElMessage.success('已拒绝审核申请');
        rejectDialogVisible.value = false;
        // 如果详情对话框也是打开的，关闭它
        if (dialogVisible.value) {
          dialogVisible.value = false;
        }
        // 刷新列表
        fetchAudits();
      } catch (error) {
        console.error('拒绝审核失败:', error);
        ElMessage.error('拒绝审核失败');
      }
    };
    
    // 监听搜索关键词变化
    watch(searchKeyword, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        fetchAudits(true);
      }
    });
    
    // 监听审核类型变化
    watch(auditType, (newValue, oldValue) => {
      if (newValue !== oldValue) { 
        fetchAudits(true); 
      }
    });
    
    // 监听状态变化
    watch(auditStatus, (newValue, oldValue) => {
      if (newValue !== oldValue) { 
        fetchAudits(true); 
      }
    });

    // 监听日期范围变化
    watch(dateRange, (newValue, oldValue) => {
      if (JSON.stringify(newValue) !== JSON.stringify(oldValue)) {
        fetchAudits(true); 
      }
    }, { deep: true }); 

    onMounted(() => {
      fetchAudits(); 
      fetchAuditTypes();
    });
    
    return {
      audits,
      total,
      currentPage,
      pageSize,
      searchKeyword,
      auditType,
      auditStatus,
      dateRange,
      auditTypes,
      statusOptions,
      dialogVisible,
      currentAudit,
      rejectDialogVisible,
      rejectForm,
      fetchAudits,
      resetSearch,
      changePage,
      refreshAudits,
      formatDate,
      getStatusText,
      getStatusClass,
      getTypeClass,
      viewAuditDetail,
      viewOrderDetail,
      handleApprove,
      handleReject,
      confirmReject
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

/* 下拉框样式修复 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input) {
  width: 100%;
}

:deep(.audit-type-dropdown) {
  z-index: 9999 !important;
}
</style>
