<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">订单管理</h1>
    
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
      
      <div class="flex justify-between items-center">
        <div>
          <button 
            @click="openCreateDialog" 
            class="px-4 py-2 bg-green-500 text-white font-medium rounded-md hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition-colors"
          >
            <span class="material-icons mr-1">add</span>
            新增订单
          </button>
        </div>
      </div>
    </div>
    
    <!-- 订单列表 -->
    <div class="bg-white rounded-lg shadow-md overflow-hidden">
      <div v-if="isLoading" class="p-6 text-center">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500"></div>
        <p class="mt-2 text-gray-600">加载中...</p>
      </div>
      
      <div v-else-if="orders.length === 0" class="p-6 text-center">
        <p class="text-gray-600">暂无订单数据</p>
      </div>
      
      <div v-else>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">订单号</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">寄件人</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">收件人</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">物品类型</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">重量(kg)</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">支付状态</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="order in orders" :key="order.id || order.orderNumber" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ order.orderNumber }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div>{{ order.senderName }}</div>
                  <div class="text-xs text-gray-400">{{ order.senderPhone }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div>{{ order.receiverName }}</div>
                  <div class="text-xs text-gray-400">{{ order.receiverPhone }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ order.itemType || '未指定' }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ order.weight || 0 }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <span :class="['px-2 py-1 rounded-full text-xs', getStatusClass(order.status)]">
                    {{ getStatusText(order.status) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <span :class="[order.paymentStatus === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800', 'px-2 py-1 rounded-full text-xs']">
                    {{ order.paymentStatus === 1 ? '已支付' : '未支付' }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(order.createTime) }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div class="flex space-x-2">
                    <button 
                      @click="viewOrderDetail(order.orderNumber)" 
                      class="text-blue-600 hover:text-blue-900 focus:outline-none"
                    >
                      <span class="material-icons text-sm">visibility</span>
                    </button>
                    <button 
                      @click="editOrder(order.orderNumber)" 
                      class="text-orange-600 hover:text-orange-900 focus:outline-none"
                    >
                      <span class="material-icons text-sm">edit</span>
                    </button>
                    <button 
                      @click="confirmDeleteRequest(order.orderNumber)" 
                      class="text-red-600 hover:text-red-900 focus:outline-none"
                    >
                      <span class="material-icons text-sm">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 分页控制 -->
        <div class="px-6 py-4 flex items-center justify-between border-t border-gray-200">
          <div class="flex-1 flex justify-between sm:hidden">
            <button 
              @click="prevPage" 
              :disabled="currentPage === 1" 
              :class="[currentPage === 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50', 'relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md']"
            >
              上一页
            </button>
            <button 
              @click="nextPage" 
              :disabled="currentPage === totalPages" 
              :class="[currentPage === totalPages ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50', 'ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md']"
            >
              下一页
            </button>
          </div>
          <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
              <p class="text-sm text-gray-700">
                显示第 <span class="font-medium">{{ (currentPage - 1) * pageSize + 1 }}</span> 至 <span class="font-medium">{{ Math.min(currentPage * pageSize, totalItems) }}</span> 条，共 <span class="font-medium">{{ totalItems }}</span> 条
              </p>
            </div>
            <div>
              <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                <button 
                  @click="prevPage" 
                  :disabled="currentPage === 1" 
                  :class="[currentPage === 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-500 hover:bg-gray-50', 'relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 text-sm font-medium']"
                >
                  <span class="material-icons text-sm">chevron_left</span>
                </button>
                <button 
                  v-for="page in displayedPages" 
                  :key="page" 
                  @click="goToPage(page)" 
                  :class="[page === currentPage ? 'z-10 bg-orange-50 border-orange-500 text-orange-600' : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50', 'relative inline-flex items-center px-4 py-2 border text-sm font-medium']"
                >
                  {{ page }}
                </button>
                <button 
                  @click="nextPage" 
                  :disabled="currentPage === totalPages" 
                  :class="[currentPage === totalPages ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-500 hover:bg-gray-50', 'relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 text-sm font-medium']"
                >
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
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold text-gray-800">订单详情</h2>
            <button @click="showDetailDialog = false" class="text-gray-500 hover:text-gray-700 focus:outline-none">
              <span class="material-icons">close</span>
            </button>
          </div>
        </div>
        
        <div v-if="isLoadingDetail" class="p-6 text-center">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500"></div>
          <p class="mt-2 text-gray-600">加载中...</p>
        </div>
        
        <div v-else class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h3 class="text-lg font-semibold mb-4 text-gray-700">基本信息</h3>
              <div class="space-y-3">
                <div class="flex">
                  <span class="w-24 text-gray-500">订单号：</span>
                  <span class="font-medium">{{ orderDetail.orderNumber }}</span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">创建时间：</span>
                  <span>{{ formatDate(orderDetail.createTime) }}</span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">订单状态：</span>
                  <span :class="['px-2 py-1 rounded-full text-xs', getStatusClass(orderDetail.status)]">
                    {{ getStatusText(orderDetail.status) }}
                  </span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">支付状态：</span>
                  <span :class="[orderDetail.paymentStatus === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800', 'px-2 py-1 rounded-full text-xs']">
                    {{ orderDetail.paymentStatus === 1 ? '已支付' : '未支付' }}
                  </span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">物品类型：</span>
                  <span>{{ orderDetail.itemType || '未指定' }}</span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">重量(kg)：</span>
                  <span>{{ orderDetail.weight || 0 }}</span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">运费(元)：</span>
                  <span>{{ orderDetail.price || 0 }}</span>
                </div>
                <div class="flex">
                  <span class="w-24 text-gray-500">备注：</span>
                  <span>{{ orderDetail.remark || '无' }}</span>
                </div>
              </div>
            </div>
            
            <div>
              <div class="mb-6">
                <h3 class="text-lg font-semibold mb-4 text-gray-700">寄件人信息</h3>
                <div class="space-y-3">
                  <div class="flex">
                    <span class="w-24 text-gray-500">姓名：</span>
                    <span>{{ orderDetail.senderName }}</span>
                  </div>
                  <div class="flex">
                    <span class="w-24 text-gray-500">电话：</span>
                    <span>{{ orderDetail.senderPhone }}</span>
                  </div>
                  <div class="flex">
                    <span class="w-24 text-gray-500">地址：</span>
                    <span>{{ orderDetail.senderAddress }}</span>
                  </div>
                </div>
              </div>
              
              <div>
                <h3 class="text-lg font-semibold mb-4 text-gray-700">收件人信息</h3>
                <div class="space-y-3">
                  <div class="flex">
                    <span class="w-24 text-gray-500">姓名：</span>
                    <span>{{ orderDetail.receiverName }}</span>
                  </div>
                  <div class="flex">
                    <span class="w-24 text-gray-500">电话：</span>
                    <span>{{ orderDetail.receiverPhone }}</span>
                  </div>
                  <div class="flex">
                    <span class="w-24 text-gray-500">地址：</span>
                    <span>{{ orderDetail.receiverAddress }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 物流信息 -->
          <div class="mt-8">
            <h3 class="text-lg font-semibold mb-4 text-gray-700">物流信息</h3>
            <div v-if="!orderDetail.logistics || orderDetail.logistics.length === 0" class="text-gray-500">
              暂无物流信息
            </div>
            <div v-else class="border-l-2 border-orange-500 pl-4 space-y-6">
              <div v-for="(item, index) in orderDetail.logistics" :key="index" class="relative">
                <div class="absolute -left-6 top-0 w-4 h-4 rounded-full bg-orange-500"></div>
                <div class="mb-1 text-sm text-gray-500">{{ formatDate(item.createTime) }}</div>
                <div class="font-medium">{{ item.message }}</div>
                <div class="text-sm text-gray-500">
                  <span>{{ item.location || '未知地点' }}</span>
                  <span class="mx-2">|</span>
                  <span>{{ item.operatorName || '系统' }}</span>
                  <span class="ml-1 text-xs">({{ item.operatorRole || '系统' }})</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑/创建订单对话框 -->
    <div v-if="showEditDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold text-gray-800">{{ isCreating ? '新增订单' : '编辑订单' }}</h2>
            <button @click="showEditDialog = false" class="text-gray-500 hover:text-gray-700 focus:outline-none">
              <span class="material-icons">close</span>
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 寄件人信息 -->
            <div>
              <h3 class="text-lg font-semibold mb-4 text-gray-700">寄件人信息</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.senderName" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.senderPhone" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                  <textarea 
                    v-model="editingOrder.senderAddress" 
                    rows="3"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  ></textarea>
                </div>
              </div>
            </div>
            
            <!-- 收件人信息 -->
            <div>
              <h3 class="text-lg font-semibold mb-4 text-gray-700">收件人信息</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.receiverName" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.receiverPhone" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                  <textarea 
                    v-model="editingOrder.receiverAddress" 
                    rows="3"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 订单信息 -->
          <div class="mt-6">
            <h3 class="text-lg font-semibold mb-4 text-gray-700">订单信息</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物品类型</label>
                <input 
                  type="text" 
                  v-model="editingOrder.itemType" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">重量(kg)</label>
                <input 
                  type="number" 
                  v-model="editingOrder.weight" 
                  min="0"
                  step="0.1"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">运费(元)</label>
                <input 
                  type="number" 
                  v-model="editingOrder.price" 
                  min="0"
                  step="0.1"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">订单状态</label>
                <select 
                  v-model="editingOrder.status" 
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
                  <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">支付状态</label>
                <div class="mt-2">
                  <label class="inline-flex items-center">
                    <input type="checkbox" v-model="editingOrder.isPaid" class="form-checkbox h-5 w-5 text-orange-500">
                    <span class="ml-2 text-gray-700">已支付</span>
                  </label>
                </div>
              </div>
            </div>
            <div class="mt-4">
              <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
              <textarea 
                v-model="editingOrder.remark" 
                rows="3"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
              ></textarea>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end space-x-3">
            <button 
              @click="showEditDialog = false" 
              class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
            >
              取消
            </button>
            <button 
              @click="saveOrder" 
              class="px-4 py-2 bg-orange-500 text-white font-medium rounded-md hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
            >
              {{ isCreating ? '创建' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除申请对话框 -->
    <div v-if="showDeleteRequestDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-lg w-full">
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold text-gray-800">申请删除订单</h2>
            <button @click="showDeleteRequestDialog = false" class="text-gray-500 hover:text-gray-700 focus:outline-none">
              <span class="material-icons">close</span>
            </button>
          </div>
        </div>
        <div class="p-6">
          <p class="mb-4">您正在申请删除订单 <strong>{{ orderToDelete }}</strong>。请填写删除原因，提交后将由管理员审核。</p>
          <textarea 
            v-model="deleteReason" 
            rows="4" 
            placeholder="请填写删除原因..."
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
          ></textarea>
        </div>
        <div class="p-6 bg-gray-50 flex justify-end space-x-3">
          <button 
            @click="showDeleteRequestDialog = false" 
            class="px-4 py-2 bg-gray-200 text-gray-700 font-medium rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors"
          >
            取消
          </button>
          <button 
            @click="submitDeleteRequest" 
            class="px-4 py-2 bg-red-500 text-white font-medium rounded-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-colors"
          >
            提交申请
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getOrderDetail, getOrderLogistics } from '../../api/orders';
import { getCurrentStaffInfo } from '../../api/profile'; // 改为导入获取员工信息函数
import toast from '../../utils/toast';

// 当前员工信息
const currentStaff = ref({}); // 初始化为空对象

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
  { value: 0, label: '待取件' },
  { value: 1, label: '已取件' },
  { value: 2, label: '运输中' },
  { value: 3, label: '已送达' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已取消' }
]);

// 对话框控制
const showDetailDialog = ref(false);
const showEditDialog = ref(false);
const showDeleteRequestDialog = ref(false);
const isCreating = ref(false);
const orderDetail = ref({});
const editingOrder = ref({
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  itemType: '',
  weight: 0,
  status: 0,
  price: 0,
  isPaid: false,
  remark: ''
});

// 删除申请相关
const deleteReason = ref('');
const orderToDelete = ref(null);

// 获取门店订单列表
const fetchOrders = async () => {
  if (!currentStaff.value.storeId) {
    toast.error('您未分配到门店，无法查看订单');
    return;
  }

  isLoading.value = true;
  try {
    const params = {
      storeId: currentStaff.value.storeId,
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      status: statusFilter.value
    };
    
    const res = await fetch(`/api/staff/orders?${new URLSearchParams(params).toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then(response => response.json());

    if (res.code === 200) {
      orders.value = res.data.records || [];
      totalItems.value = res.data.total || 0;
    } else {
      toast.error(res.message || '获取订单列表失败');
    }
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
    }
    
    // 获取物流信息
    try {
      const logisticsRes = await getOrderLogistics(idOrOrderNumber);
      if (logisticsRes.code === 200 && logisticsRes.data) {
        // 将物流信息映射到订单详情中
        orderDetail.value.logistics = logisticsRes.data.map(item => ({
          message: item.content,
          location: item.location,
          operatorName: item.operatorName,
          operatorRole: item.operatorRole,
          createTime: item.createTime
        }));
      }
    } catch (logisticsError) {
      console.error('获取物流信息失败:', logisticsError);
      // 不影响订单详情的显示，只是物流信息为空
      orderDetail.value.logistics = [];
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

// 编辑订单
const editOrder = async (orderNumber) => {
  try {
    isLoadingDetail.value = true;
    const response = await getOrderDetail(orderNumber);
    if (response.code === 200) {
      editingOrder.value = { ...response.data };
      // 确保price字段存在
      if (editingOrder.value.price === undefined || editingOrder.value.price === null) {
        editingOrder.value.price = 0;
      }
      
      // 将paymentStatus整数值转换为isPaid布尔值
      if (editingOrder.value.paymentStatus !== undefined) {
        editingOrder.value.isPaid = editingOrder.value.paymentStatus === 1;
      } else {
        // 默认未支付
        editingOrder.value.isPaid = false;
      }
      
      // 将description字段映射到remark字段
      if (editingOrder.value.description !== undefined) {
        editingOrder.value.remark = editingOrder.value.description;
      }
      
      isCreating.value = false;
      showEditDialog.value = true;
    } else {
      toast.error(response.message || '获取订单详情失败');
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    toast.error('获取订单详情失败');
  } finally {
    isLoadingDetail.value = false;
  }
};

// 打开创建对话框
const openCreateDialog = () => {
  editingOrder.value = {
    senderName: '',
    senderPhone: '',
    senderAddress: '',
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    weight: 0,
    price: 0,
    itemType: '',
    status: 0,
    paymentStatus: 0, // 设置默认支付状态为未支付
    isPaid: false,    // 前端显示用
    remark: '',
    storeId: currentStaff.value.storeId // 自动设置为员工所在门店
  };
  isCreating.value = true;
  showEditDialog.value = true;
};

// 保存订单
const saveOrder = async () => {
  try {
    // 在发送请求前，将isPaid布尔值转换为paymentStatus整数值
    const orderData = { ...editingOrder.value };
    
    // 将isPaid布尔值转换为paymentStatus整数值
    if (orderData.isPaid !== undefined) {
      orderData.paymentStatus = orderData.isPaid ? 1 : 0; // 1-已支付, 0-未支付
    }
    
    // 将remark字段映射到description字段
    if (orderData.remark !== undefined) {
      orderData.description = orderData.remark;
    }
    
    // 确保设置了门店ID
    if (!orderData.storeId) {
      orderData.storeId = currentStaff.value.storeId;
    }
    
    const url = isCreating.value ? '/api/staff/orders' : `/api/staff/orders/${orderData.orderNumber}`;
    const method = isCreating.value ? 'POST' : 'PUT';
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(orderData)
    }).then(res => res.json());
    
    if (response.code === 200) {
      toast.success(isCreating.value ? '创建订单成功' : '更新订单成功');
      showEditDialog.value = false;
      fetchOrders();
    } else {
      toast.error(response.message || (isCreating.value ? '创建订单失败' : '更新订单失败'));
    }
  } catch (error) {
    console.error('保存订单失败:', error);
    toast.error('保存订单失败');
  }
};

// 确认删除申请
const confirmDeleteRequest = (orderNumber) => {
  orderToDelete.value = orderNumber;
  deleteReason.value = '';
  showDeleteRequestDialog.value = true;
};

// 提交删除申请
const submitDeleteRequest = async () => {
  if (!deleteReason.value.trim()) {
    toast.warning('请填写删除原因');
    return;
  }
  
  try {
    const requestData = {
      orderId: orderToDelete.value,
      staffId: currentStaff.value.id,
      staffName: currentStaff.value.name,
      storeId: currentStaff.value.storeId,
      storeName: currentStaff.value.storeName,
      reason: deleteReason.value
    };
    
    const response = await fetch('/api/staff/order-deletion-request', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    }).then(res => res.json());
    
    if (response.code === 200) {
      toast.success('删除申请已提交，等待管理员审核');
      showDeleteRequestDialog.value = false;
      orderToDelete.value = null;
      deleteReason.value = '';
    } else {
      toast.error(response.message || '提交删除申请失败');
    }
  } catch (error) {
    console.error('提交删除申请失败:', error);
    toast.error('提交删除申请失败');
  }
};

// 搜索订单
const searchOrders = () => {
  currentPage.value = 1;
  fetchOrders();
};

// 重置搜索
const resetSearch = () => {
  searchQuery.value = '';
  statusFilter.value = '';
  currentPage.value = 1;
  fetchOrders();
};

// 分页控制
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
    minute: '2-digit'
  });
};

// 获取状态样式类
const getStatusClass = (status) => {
  switch (Number(status)) {
    case 0: // 待取件
      return 'bg-yellow-100 text-yellow-800';
    case 1: // 已取件
      return 'bg-blue-100 text-blue-800';
    case 2: // 运输中
      return 'bg-purple-100 text-purple-800';
    case 3: // 已送达
      return 'bg-green-100 text-green-800';
    case 4: // 已完成
      return 'bg-green-100 text-green-800';
    case 5: // 已取消
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  const option = statusOptions.value.find(opt => opt.value === Number(status));
  return option ? option.label : '未知状态';
};

// 获取当前员工信息并加载订单
const loadStaffAndOrders = async () => {
  try {
    const res = await getCurrentStaffInfo(); // 改为调用获取员工信息函数
    if (res.code === 200 && res.data) {
      currentStaff.value = res.data;
      // 确保获取到员工信息后再加载订单
      if (currentStaff.value.storeId) {
        fetchOrders();
      } else {
        toast.error('无法获取员工门店信息，无法加载订单');
        isLoading.value = false; // 停止加载状态
      }
    } else {
      toast.error(res.message || '获取员工信息失败');
      isLoading.value = false; // 停止加载状态
    }
  } catch (error) {
    console.error('获取员工信息失败:', error);
    toast.error('获取员工信息失败');
    isLoading.value = false; // 停止加载状态
  }
};

// 初始化
onMounted(() => {
  loadStaffAndOrders(); // 调用新的初始化函数
});
</script>
