<template>
  <div> <!-- Add root div -->
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
                <button 
                  @click="viewOrderDetail(order.orderNumber)" 
                  class="text-blue-500 hover:text-blue-700 mr-2"
                >
                  查看
                </button>
                <button 
                  @click="editOrder(order.orderNumber)" 
                  class="text-orange-500 hover:text-orange-700 mr-2"
                >
                  编辑
                </button>
                <button 
                  @click="confirmDeleteRequest(order.orderNumber)" 
                  class="text-red-500 hover:text-red-700"
                >
                  申请删除
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
                显示第 <span class="font-medium">{{ orders.length > 0 ? (currentPage - 1) * pageSize + 1 : 0 }}</span> 至 
                <span class="font-medium">{{ orders.length > 0 ? Math.min(currentPage * pageSize, totalItems) : 0 }}</span> 条，
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
                      ? 'z-10 bg-orange-500 border-orange-500 text-white'
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
  
  <!-- 订单详情对话框 -->
  <div v-if="showDetailDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
      <div class="p-6 border-b border-gray-200">
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold text-gray-800">订单详情</h2>
          <button @click="showDetailDialog = false" class="text-gray-500 hover:text-gray-700">
            <span class="material-icons">close</span>
          </button>
        </div>
      </div>
      
      <div v-if="isLoadingDetail" class="p-6 text-center">
        <p class="text-gray-500">加载中...</p>
      </div>
      
      <div v-else class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-4">基本信息</h3>
            <div class="space-y-3">
              <div class="flex">
                <span class="text-gray-500 w-24">订单号：</span>
                <span class="text-gray-800 font-medium">{{ orderDetail.orderNumber }}</span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">创建时间：</span>
                <span class="text-gray-800">{{ formatDate(orderDetail.createTime) }}</span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">订单状态：</span>
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                  :class="getStatusClass(orderDetail.status)"
                >
                  {{ getStatusText(orderDetail.status) }}
                </span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">支付状态：</span>
                <span 
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                  :class="orderDetail.paymentStatus === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                >
                  {{ orderDetail.paymentStatus === 1 ? '已支付' : '未支付' }}
                </span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">物品类型：</span>
                <span class="text-gray-800">{{ orderDetail.itemType || '未指定' }}</span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">重量：</span>
                <span class="text-gray-800">{{ orderDetail.weight }} kg</span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">运费：</span>
                <span class="text-gray-800">¥ {{ orderDetail.price }}</span>
              </div>
              <div class="flex">
                <span class="text-gray-500 w-24">备注：</span>
                <span class="text-gray-800">{{ orderDetail.remark || '无' }}</span>
              </div>
            </div>
          </div>
          
          <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-4">收发件信息</h3>
            <div class="space-y-4">
              <div class="p-3 bg-gray-50 rounded-md">
                <h4 class="text-sm font-medium text-gray-500 mb-2">寄件人信息</h4>
                <div class="space-y-1">
                  <div class="text-gray-800">{{ orderDetail.senderName }}</div>
                  <div class="text-gray-600">{{ orderDetail.senderPhone }}</div>
                  <div class="text-gray-600">{{ orderDetail.senderAddress }}</div>
                </div>
              </div>
              
              <div class="p-3 bg-gray-50 rounded-md">
                <h4 class="text-sm font-medium text-gray-500 mb-2">收件人信息</h4>
                <div class="space-y-1">
                  <div class="text-gray-800">{{ orderDetail.receiverName }}</div>
                  <div class="text-gray-600">{{ orderDetail.receiverPhone }}</div>
                  <div class="text-gray-600">{{ orderDetail.receiverAddress }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="mt-6">
          <h3 class="text-lg font-semibold text-gray-700 mb-4">物流信息</h3>
          <div v-if="!orderDetail.logistics || orderDetail.logistics.length === 0" class="text-gray-500 text-center py-4">
            暂无物流信息
          </div>
          <div v-else class="border-l-2 border-gray-200 ml-4 pl-4 space-y-6">
            <div v-for="(item, index) in orderDetail.logistics" :key="index" class="relative">
              <div class="absolute -left-6 mt-1 w-4 h-4 rounded-full" :class="index === 0 ? 'bg-orange-500' : 'bg-gray-300'"></div>
              <div class="flex flex-col">
                <span class="text-gray-800 font-medium">{{ item.message }}</span>
                <div class="flex text-sm text-gray-500 mt-1">
                  <span>{{ formatDate(item.createTime) }}</span>
                  <span class="mx-2">|</span>
                  <span>{{ item.location || '未知地点' }}</span>
                  <span class="mx-2">|</span>
                  <span>{{ item.operatorName }} ({{ item.operatorRole }})</span>
                </div>
              </div>
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
  
  <!-- 编辑订单对话框 -->
  <div v-if="showEditDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
      <div class="p-6 border-b border-gray-200">
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold text-gray-800">{{ isCreating ? '新增订单' : '编辑订单' }}</h2>
          <button @click="showEditDialog = false" class="text-gray-500 hover:text-gray-700">
            <span class="material-icons">close</span>
          </button>
        </div>
      </div>
      
      <div class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 寄件人信息 -->
          <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-4">寄件人信息</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                <input 
                  type="text" 
                  v-model="editingOrder.senderName" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入寄件人姓名"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                <input 
                  type="text" 
                  v-model="editingOrder.senderPhone" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入寄件人电话"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                <textarea 
                  v-model="editingOrder.senderAddress" 
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入寄件人详细地址"
                ></textarea>
              </div>
            </div>
          </div>
          
          <!-- 收件人信息 -->
          <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-4">收件人信息</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                <input 
                  type="text" 
                  v-model="editingOrder.receiverName" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入收件人姓名"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                <input 
                  type="text" 
                  v-model="editingOrder.receiverPhone" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入收件人电话"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                <textarea 
                  v-model="editingOrder.receiverAddress" 
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入收件人详细地址"
                ></textarea>
              </div>
            </div>
          </div>
        </div>
        
        <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 物品信息 -->
          <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-4">物品信息</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">物品类型</label>
                <input 
                  type="text" 
                  v-model="editingOrder.itemType" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入物品类型，如：文件、日用品等"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">重量 (kg)</label>
                <input 
                  type="number" 
                  v-model="editingOrder.weight" 
                  min="0"
                  step="0.01"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入物品重量"
                >
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">运费 (元)</label>
                <input 
                  type="number" 
                  v-model="editingOrder.price" 
                  min="0"
                  step="0.01"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入运费金额"
                >
              </div>
            </div>
          </div>
          
          <!-- 订单状态 -->
          <div>
            <h3 class="text-lg font-semibold text-gray-700 mb-4">订单状态</h3>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">订单状态</label>
                <select 
                  v-model="editingOrder.status" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
                  <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">支付状态</label>
                <div class="flex items-center space-x-4 mt-2">
                  <label class="inline-flex items-center">
                    <input 
                      type="radio" 
                      v-model="editingOrder.isPaid" 
                      :value="true"
                      class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300"
                    >
                    <span class="ml-2 text-gray-700">已支付</span>
                  </label>
                  <label class="inline-flex items-center">
                    <input 
                      type="radio" 
                      v-model="editingOrder.isPaid" 
                      :value="false"
                      class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300"
                    >
                    <span class="ml-2 text-gray-700">未支付</span>
                  </label>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea 
                  v-model="editingOrder.remark" 
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  placeholder="请输入备注信息（可选）"
                ></textarea>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="p-6 border-t border-gray-200 flex justify-end space-x-2">
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
          保存
        </button>
      </div>
    </div>
  </div>
  
  <!-- 删除申请对话框 -->
  <div v-if="showDeleteRequestDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md">
      <div class="p-6 border-b border-gray-200">
        <div class="flex justify-between items-center">
          <h2 class="text-xl font-bold text-gray-800">申请删除订单</h2>
          <button @click="showDeleteRequestDialog = false" class="text-gray-500 hover:text-gray-700">
            <span class="material-icons">close</span>
          </button>
        </div>
      </div>
      
      <div class="p-6">
        <p class="text-gray-700 mb-4">您正在申请删除订单，请填写删除原因：</p>
        <textarea 
          v-model="deleteReason" 
          rows="4"
          class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
          placeholder="请输入删除原因（必填）"
        ></textarea>
      </div>
      
      <div class="p-6 border-t border-gray-200 flex justify-end space-x-2">
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
    
    <!-- 订单详情对话框 -->
    <div v-if="showDetailDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold text-gray-800">订单详情</h2>
            <button @click="showDetailDialog = false" class="text-gray-500 hover:text-gray-700">
              <span class="material-icons">close</span>
            </button>
          </div>
        </div>
        
        <div v-if="isLoadingDetail" class="p-6 text-center">
          <p class="text-gray-500">加载中...</p>
        </div>
        
        <div v-else class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div>
              <h3 class="text-lg font-semibold text-gray-700 mb-4">基本信息</h3>
              <div class="space-y-3">
                <div class="flex">
                  <span class="text-gray-500 w-24">订单号：</span>
                  <span class="text-gray-800 font-medium">{{ orderDetail.orderNumber }}</span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">创建时间：</span>
                  <span class="text-gray-800">{{ formatDate(orderDetail.createTime) }}</span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">订单状态：</span>
                  <span 
                    class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                    :class="getStatusClass(orderDetail.status)"
                  >
                    {{ getStatusText(orderDetail.status) }}
                  </span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">支付状态：</span>
                  <span 
                    class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full" 
                    :class="orderDetail.paymentStatus === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                  >
                    {{ orderDetail.paymentStatus === 1 ? '已支付' : '未支付' }}
                  </span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">物品类型：</span>
                  <span class="text-gray-800">{{ orderDetail.itemType || '未指定' }}</span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">重量：</span>
                  <span class="text-gray-800">{{ orderDetail.weight }} kg</span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">运费：</span>
                  <span class="text-gray-800">¥ {{ orderDetail.price }}</span>
                </div>
                <div class="flex">
                  <span class="text-gray-500 w-24">备注：</span>
                  <span class="text-gray-800">{{ orderDetail.remark || '无' }}</span>
                </div>
              </div>
            </div>
            
            <div>
              <h3 class="text-lg font-semibold text-gray-700 mb-4">收发件信息</h3>
              <div class="space-y-4">
                <div class="p-3 bg-gray-50 rounded-md">
                  <h4 class="text-sm font-medium text-gray-500 mb-2">寄件人信息</h4>
                  <div class="space-y-1">
                    <div class="text-gray-800">{{ orderDetail.senderName }}</div>
                    <div class="text-gray-600">{{ orderDetail.senderPhone }}</div>
                    <div class="text-gray-600">{{ orderDetail.senderAddress }}</div>
                  </div>
                </div>
                
                <div class="p-3 bg-gray-50 rounded-md">
                  <h4 class="text-sm font-medium text-gray-500 mb-2">收件人信息</h4>
                  <div class="space-y-1">
                    <div class="text-gray-800">{{ orderDetail.receiverName }}</div>
                    <div class="text-gray-600">{{ orderDetail.receiverPhone }}</div>
                    <div class="text-gray-600">{{ orderDetail.receiverAddress }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-6">
            <h3 class="text-lg font-semibold text-gray-700 mb-4">物流信息</h3>
            <div v-if="!orderDetail.logistics || orderDetail.logistics.length === 0" class="text-gray-500 text-center py-4">
              暂无物流信息
            </div>
            <div v-else class="border-l-2 border-gray-200 ml-4 pl-4 space-y-6">
              <div v-for="(item, index) in orderDetail.logistics" :key="index" class="relative">
                <div class="absolute -left-6 mt-1 w-4 h-4 rounded-full" :class="index === 0 ? 'bg-orange-500' : 'bg-gray-300'"></div>
                <div class="flex flex-col">
                  <span class="text-gray-800 font-medium">{{ item.message }}</span>
                  <div class="flex text-sm text-gray-500 mt-1">
                    <span>{{ formatDate(item.createTime) }}</span>
                    <span class="mx-2">|</span>
                    <span>{{ item.location || '未知地点' }}</span>
                    <span class="mx-2">|</span>
                    <span>{{ item.operatorName }} ({{ item.operatorRole }})</span>
                  </div>
                </div>
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
    
    <!-- 编辑订单对话框 -->
    <div v-if="showEditDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold text-gray-800">{{ isCreating ? '新增订单' : '编辑订单' }}</h2>
            <button @click="showEditDialog = false" class="text-gray-500 hover:text-gray-700">
              <span class="material-icons">close</span>
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 寄件人信息 -->
            <div>
              <h3 class="text-lg font-semibold text-gray-700 mb-4">寄件人信息</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.senderName" 
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入寄件人姓名"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.senderPhone" 
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入寄件人电话"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                  <textarea 
                    v-model="editingOrder.senderAddress" 
                    rows="3"
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入寄件人详细地址"
                  ></textarea>
                </div>
              </div>
            </div>
            
            <!-- 收件人信息 -->
            <div>
              <h3 class="text-lg font-semibold text-gray-700 mb-4">收件人信息</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.receiverName" 
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入收件人姓名"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.receiverPhone" 
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入收件人电话"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                  <textarea 
                    v-model="editingOrder.receiverAddress" 
                    rows="3"
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入收件人详细地址"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 物品信息 -->
            <div>
              <h3 class="text-lg font-semibold text-gray-700 mb-4">物品信息</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">物品类型</label>
                  <input 
                    type="text" 
                    v-model="editingOrder.itemType" 
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入物品类型，如：文件、日用品等"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">重量 (kg)</label>
                  <input 
                    type="number" 
                    v-model="editingOrder.weight" 
                    min="0"
                    step="0.01"
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入物品重量"
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">运费 (元)</label>
                  <input 
                    type="number" 
                    v-model="editingOrder.price" 
                    min="0"
                    step="0.01"
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入运费金额"
                  >
                </div>
              </div>
            </div>
            
            <!-- 订单状态 -->
            <div>
              <h3 class="text-lg font-semibold text-gray-700 mb-4">订单状态</h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">订单状态</label>
                  <select 
                    v-model="editingOrder.status" 
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                    <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                      {{ option.label }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">支付状态</label>
                  <div class="flex items-center space-x-4 mt-2">
                    <label class="inline-flex items-center">
                      <input 
                        type="radio" 
                        v-model="editingOrder.isPaid" 
                        :value="true"
                        class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300"
                      >
                      <span class="ml-2 text-gray-700">已支付</span>
                    </label>
                    <label class="inline-flex items-center">
                      <input 
                        type="radio" 
                        v-model="editingOrder.isPaid" 
                        :value="false"
                        class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300"
                      >
                      <span class="ml-2 text-gray-700">未支付</span>
                    </label>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                  <textarea 
                    v-model="editingOrder.remark" 
                    rows="3"
                    class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    placeholder="请输入备注信息（可选）"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="p-6 border-t border-gray-200 flex justify-end space-x-2">
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
            保存
          </button>
        </div>
      </div>
    </div>
    
    <!-- 删除申请对话框 -->
    <div v-if="showDeleteRequestDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div class="p-6 border-b border-gray-200">
          <div class="flex justify-between items-center">
            <h2 class="text-xl font-bold text-gray-800">申请删除订单</h2>
            <button @click="showDeleteRequestDialog = false" class="text-gray-500 hover:text-gray-700">
              <span class="material-icons">close</span>
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <p class="text-gray-700 mb-4">您正在申请删除订单，请填写删除原因：</p>
          <textarea 
            v-model="deleteReason" 
            rows="4"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
            placeholder="请输入删除原因（必填）"
          ></textarea>
        </div>
        
        <div class="p-6 border-t border-gray-200 flex justify-end space-x-2">
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
  </div> <!-- Close root div -->
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'; // Removed watch
// Import functions from correct API files
import { getStoreOrders, submitOrderDeletionRequest, updateOrder, createOrder } from '../../api/staffOrders'; // Use staff-specific API functions
import { getOrderDetail, getOrderLogistics } from '../../api/orders'; // Use common API for details and logistics
import toast from '../../utils/toast';
import { useUserStore } from '../../stores/user'; // Keep userStore if needed for other parts like delete request

// 获取用户信息
const userStore = useUserStore();
// No need for currentStaff ref here, directly use userStore.userInfo

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
const orderToDelete = ref(null); // Will store the order number (String)

// 获取门店订单列表 (No longer needs storeId check)
const fetchOrders = async () => {
  console.log('[fetchOrders] Attempting to fetch orders for the current staff.'); // Updated log

  isLoading.value = true;
  try {
    // Params no longer include storeId
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      status: statusFilter.value
    };
    
    console.log('发送请求参数:', params);
    
    // 使用员工专用API获取门店订单
    const res = await getStoreOrders(params);
    console.log('获取订单响应:', res);

    if (res.code === 200) {
      orders.value = res.data.records || [];
      totalItems.value = res.data.total || 0;
      console.log('成功获取订单数据, 总数:', totalItems.value);
    } else {
      console.error('获取订单列表失败, 错误码:', res.code, '错误信息:', res.message);
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
    // 使用员工专用API获取订单详情
    const res = await getOrderDetail(idOrOrderNumber);
    orderDetail.value = res.data || {};
    
    // 将description字段映射到remark字段，用于显示备注信息
    if (orderDetail.value.description !== undefined) {
      orderDetail.value.remark = orderDetail.value.description;
    }
    
    // 获取物流信息
    try {
      // 使用员工专用API获取物流信息
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
  const storeId = userStore.userInfo?.storeId;
  if (!storeId) {
    toast.error('无法获取门店信息，无法创建订单');
    return;
  }
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
    storeId: storeId // 自动设置为员工所在门店
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
      // 删除isPaid字段，因为后端实体没有这个字段
      delete orderData.isPaid;
    }
    
    // 将remark字段映射到description字段
    if (orderData.remark !== undefined) {
      orderData.description = orderData.remark;
      // 删除remark字段，保持与后端实体一致
      delete orderData.remark;
    }

    // 确保设置了门店ID (should already be set in openCreateDialog or editOrder)
    if (!orderData.storeId) {
      const storeId = userStore.userInfo?.storeId;
      if (!storeId) {
        toast.error('无法获取门店信息，无法保存订单');
        return;
      }
      orderData.storeId = storeId;
    }

    let response;
    // 使用从staffOrders.js导入的API函数
    if (isCreating.value) {
      // 使用员工专用的创建订单API
      response = await createOrder(orderData); 
    } else {
      // 使用员工专用的更新订单API
      response = await updateOrder(orderData.orderNumber, orderData); 
    }
    
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

// 确认删除申请 (Store the order number)
const confirmDeleteRequest = (orderNumber) => { 
  orderToDelete.value = orderNumber; // Store the order number directly
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
    // 简化请求数据，只传递订单号和删除原因，让后端从当前认证用户中获取员工信息
    const requestData = {
      orderNumber: orderToDelete.value,
      reason: deleteReason.value
    };

    const response = await submitOrderDeletionRequest(requestData);
    
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

// onMounted now directly calls fetchOrders
onMounted(() => {
  console.log('[LifeCycle] OrderManagement component mounted.');
  fetchOrders(); // Fetch orders on component mount
});
</script>
