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
          <button 
            @click="confirmBatchDelete" 
            :disabled="selectedOrders.length === 0" 
            class="ml-2 px-4 py-2 bg-red-500 text-white font-medium rounded-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="material-icons mr-1">delete</span>
            批量删除
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
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                <input 
                  type="checkbox" 
                  :checked="isAllSelected" 
                  @change="toggleSelectAll" 
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
                >
              </th>
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
              <td class="px-6 py-4 whitespace-nowrap">
                <input 
                  type="checkbox" 
                  v-model="selectedOrders" 
                  :value="order.id" 
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
                >
              </td>
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
                  @click="confirmDelete(order.orderNumber)" 
                  class="text-red-500 hover:text-red-700"
                >
                  删除
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
    
    <!-- 编辑/创建订单对话框 -->
    <div v-if="showEditDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-200 flex justify-between items-center">
          <h3 class="text-lg font-medium text-gray-900">{{ isCreating ? '新增订单' : '编辑订单' }}</h3>
          <button @click="showEditDialog = false" class="text-gray-400 hover:text-gray-500">
            <span class="material-icons">close</span>
          </button>
        </div>
        <div class="p-6">
          <form @submit.prevent="saveOrder">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <!-- 寄件人信息 -->
              <div>
                <h4 class="text-sm font-medium text-gray-500 mb-4">寄件人信息</h4>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                    <input 
                      type="text" 
                      v-model="editingOrder.senderName" 
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                      required
                    >
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                    <input 
                      type="text" 
                      v-model="editingOrder.senderPhone" 
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                      required
                    >
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                    <textarea 
                      v-model="editingOrder.senderAddress" 
                      rows="3"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                      required
                    ></textarea>
                  </div>
                </div>
              </div>
              
              <!-- 收件人信息 -->
              <div>
                <h4 class="text-sm font-medium text-gray-500 mb-4">收件人信息</h4>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">姓名</label>
                    <input 
                      type="text" 
                      v-model="editingOrder.receiverName" 
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                      required
                    >
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">电话</label>
                    <input 
                      type="text" 
                      v-model="editingOrder.receiverPhone" 
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                      required
                    >
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">地址</label>
                    <textarea 
                      v-model="editingOrder.receiverAddress" 
                      rows="3"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                      required
                    ></textarea>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 订单信息 -->
            <div class="mb-6">
              <h4 class="text-sm font-medium text-gray-500 mb-4">订单信息</h4>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">物品类型</label>
                  <select 
                    v-model="editingOrder.itemType" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    required
                  >
                    <option value="">请选择物品种类</option>
                    <option value="电器">电器</option>
                    <option value="数码产品">数码产品</option>
                    <option value="日常用品">日常用品</option>
                    <option value="文件类">文件类</option>
                    <option value="服装">服装</option>
                    <option value="食品">食品</option>
                    <option value="化妆品">化妆品</option>
                    <option value="玩具">玩具</option>
                    <option value="书籍">书籍</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">重量(kg)</label>
                  <input 
                    type="number" 
                    v-model="editingOrder.weight" 
                    step="0.01"
                    min="0.01"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    required
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">订单状态</label>
                  <select 
                    v-model="editingOrder.status" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    required
                  >
                    <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                      {{ option.label }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">运费</label>
                  <input 
                    type="number" 
                    v-model="editingOrder.price" 
                    step="0.01"
                    min="0"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                    required
                  >
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">支付状态</label>
                  <div class="flex items-center space-x-4 mt-2">
                    <label class="inline-flex items-center">
                      <input type="radio" v-model="editingOrder.isPaid" :value="true" class="h-4 w-4 text-orange-500 focus:ring-orange-500">
                      <span class="ml-2 text-sm text-gray-700">已支付</span>
                    </label>
                    <label class="inline-flex items-center">
                      <input type="radio" v-model="editingOrder.isPaid" :value="false" class="h-4 w-4 text-orange-500 focus:ring-orange-500">
                      <span class="ml-2 text-sm text-gray-700">未支付</span>
                    </label>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">处理门店</label>
                  <select 
                    v-model="editingOrder.storeId" 
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                  >
                    <option value="">请选择处理门店</option>
                    <option v-for="store in storeList" :key="store.id" :value="store.id">
                      {{ store.name }}
                    </option>
                  </select>
                </div>
              </div>
              <div class="mt-4">
                <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
                <textarea 
                  v-model="editingOrder.remark" 
                  rows="2"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-orange-500"
                ></textarea>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3">
              <button 
                type="button"
                @click="showEditDialog = false" 
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
import { getOrderList, getOrderDetail, updateOrder, deleteOrder, batchDeleteOrders, createOrder, getOrderStatusOptions, getOrderLogistics } from '../../api/orders';
import { getStoreList } from '../../api/stores';
import toast from '../../utils/toast';

// 搜索和筛选
const searchQuery = ref('');
const statusFilter = ref('');
const isLoading = ref(false);
const isLoadingDetail = ref(false);

// 订单列表数据
const orders = ref([]);
const selectedOrders = ref([]);

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
  { value: 'PENDING', label: '待处理' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'IN_TRANSIT', label: '运输中' },
  { value: 'DELIVERED', label: '已送达' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'RETURNED', label: '已退回' }
]);

// 门店列表
const storeList = ref([]);

// 对话框控制
const showDetailDialog = ref(false);
const showEditDialog = ref(false);
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
  status: 'PENDING',
  freight: 0,
  insuranceFee: 0,
  isPaid: false,
  remark: ''
});

// 是否全选
const isAllSelected = computed(() => {
  return orders.value.length > 0 && selectedOrders.value.length === orders.value.length;
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
    // 正确处理分页响应
    if (res.data && res.data.records) {
      orders.value = res.data.records;
      totalItems.value = res.data.total || 0;
      // 可选：如果后端返回当前页和大小，也进行更新
      // currentPage.value = res.data.current || currentPage.value;
      // pageSize.value = res.data.size || pageSize.value;
    } else {
      orders.value = [];
      totalItems.value = 0;
      console.warn("获取订单列表响应数据结构不正确:", res);
    }
  } catch (error) {
    console.error('获取订单列表失败:', error);
    // 出错时也清空数据
    orders.value = [];
    totalItems.value = 0;
    toast.error('获取订单列表失败');
  } finally {
    isLoading.value = false;
  }
};

// 获取门店列表
const fetchStores = async () => {
  try {
    // 传递空对象作为参数，获取所有门店数据而不进行分页
    const res = await getStoreList({});
    if (res.code === 200 && res.data) {
      // 确保返回的数据是数组
      storeList.value = Array.isArray(res.data) ? res.data : 
                        (res.data.records ? res.data.records : []);
      console.log('获取门店列表成功:', storeList.value);
    } else {
      console.error('获取门店列表失败:', res.message);
    }
  } catch (error) {
    console.error('获取门店列表失败:', error);
    toast.error('获取门店列表失败');
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
        console.log('获取物流信息成功:', orderDetail.value.logistics);
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
        console.log('获取支付状态:', editingOrder.value.paymentStatus, '转换为isPaid:', editingOrder.value.isPaid);
      } else {
        // 默认未支付
        editingOrder.value.isPaid = false;
      }
      
      // 将description字段映射到remark字段
      if (editingOrder.value.description !== undefined) {
        editingOrder.value.remark = editingOrder.value.description;
        console.log('获取备注:', editingOrder.value.description, '映射到remark:', editingOrder.value.remark);
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
    storeId: null      // 添加门店ID字段
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
      console.log('设置支付状态:', orderData.isPaid, '转换为paymentStatus:', orderData.paymentStatus);
    }
    
    // 将remark字段映射到description字段
    if (orderData.remark !== undefined) {
      orderData.description = orderData.remark;
      console.log('设置备注:', orderData.remark, '映射到description:', orderData.description);
    }
    
    if (isCreating.value) {
      await createOrder(orderData);
      toast.success('创建订单成功');
    } else {
      await updateOrder(orderData.orderNumber, orderData);
      toast.success('更新订单成功');
    }
    showEditDialog.value = false;
    fetchOrders();
  } catch (error) {
    console.error('保存订单失败:', error);
    toast.error('保存订单失败');
  }
};

// 确认删除订单
const confirmDelete = (orderNumber) => {
  if (confirm('确定要删除该订单吗？')) {
    deleteOrderById(orderNumber);
  }
};

// 删除订单
const deleteOrderById = async (orderNumber) => {
  try {
    await deleteOrder(orderNumber);
    toast.success('删除订单成功');
    fetchOrders();
  } catch (error) {
    console.error('删除订单失败:', error);
    toast.error('删除订单失败');
  }
};

// 确认批量删除
const confirmBatchDelete = () => {
  if (selectedOrders.value.length === 0) {
    toast.warning('请先选择要删除的订单');
    return;
  }
  
  if (confirm(`确定要删除选中的 ${selectedOrders.value.length} 条订单吗？`)) {
    batchDelete();
  }
};

// 批量删除
const batchDelete = async () => {
  try {
    await batchDeleteOrders(selectedOrders.value);
    toast.success('批量删除成功');
    selectedOrders.value = [];
    fetchOrders();
  } catch (error) {
    console.error('批量删除失败:', error);
    toast.error('批量删除失败');
  }
};

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedOrders.value = [];
  } else {
    selectedOrders.value = orders.value.map(order => order.id);
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
  // 处理数字状态值
  if (typeof status === 'number' || !isNaN(Number(status))) {
    switch (Number(status)) {
      case 0: // 待取件/待接单
        return 'bg-yellow-100 text-yellow-800';
      case 1: // 已取件/已接单
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
  }
  
  // 处理字符串状态值
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-800';
    case 'PROCESSING':
      return 'bg-blue-100 text-blue-800';
    case 'SHIPPED':
      return 'bg-indigo-100 text-indigo-800';
    case 'IN_TRANSIT':
      return 'bg-purple-100 text-purple-800';
    case 'DELIVERED':
      return 'bg-green-100 text-green-800';
    case 'COMPLETED':
      return 'bg-green-100 text-green-800';
    case 'CANCELLED':
      return 'bg-red-100 text-red-800';
    case 'RETURNED':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  // 处理数字状态值
  if (typeof status === 'number' || !isNaN(Number(status))) {
    switch (Number(status)) {
      case 0: return '待接单';
      case 1: return '已接单';
      case 2: return '运输中';
      case 3: return '已送达';
      case 4: return '已完成';
      case 5: return '已取消';
      default: return '未知状态';
    }
  }
  
  // 处理字符串状态值
  const option = statusOptions.value.find(opt => opt.value === status);
  return option ? option.label : '未知状态';
};

// 初始化
onMounted(async () => {
  try {
    // 获取订单状态选项
    const res = await getOrderStatusOptions();
    if (res.data && Array.isArray(res.data)) {
      statusOptions.value = res.data;
    }
  } catch (error) {
    console.error('获取订单状态选项失败:', error);
  }
  
  // 获取订单列表
  fetchOrders();
  // 获取门店列表
  fetchStores();
});
</script>
