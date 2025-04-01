<template>
  <div class="min-h-screen bg-gray-100 p-6">
    <h1 class="text-2xl font-bold text-orange-500 mb-6">门店数据统计</h1>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <div class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-lg">
        <h3 class="text-gray-700 text-sm font-medium">今日订单</h3>
        <p class="text-2xl font-semibold text-orange-500 mt-2">{{ dashboardData.todayOrders || 0 }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-lg">
        <h3 class="text-gray-700 text-sm font-medium">待处理</h3>
        <p class="text-2xl font-semibold text-orange-500 mt-2">{{ dashboardData.pendingOrders || 0 }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-lg">
        <h3 class="text-gray-700 text-sm font-medium">配送中</h3>
        <p class="text-2xl font-semibold text-orange-500 mt-2">{{ dashboardData.inDeliveryOrders || 0 }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-lg">
        <h3 class="text-gray-700 text-sm font-medium">已完成</h3>
        <p class="text-2xl font-semibold text-orange-500 mt-2">{{ dashboardData.completedOrders || 0 }}</p>
      </div>
    </div>
    
    <!-- 图表展示区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 订单状态分布饼图 -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-gray-700 text-base font-medium mb-4">门店订单状态分布</h3>
        <div ref="orderStatusChart" class="w-full h-64"></div>
      </div>
      
      <!-- 近期订单趋势折线图 -->
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-gray-700 text-base font-medium mb-4">门店近期订单趋势</h3>
        <div ref="orderTrendChart" class="w-full h-64"></div>
      </div>
    </div>
    
    <!-- 最近订单表格 -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="p-6 border-b border-gray-200">
        <h3 class="text-gray-700 text-base font-medium">门店最近订单</h3>
      </div>
      <div class="overflow-x-auto">
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
            <tr v-if="recentOrders.length === 0">
              <td colspan="6" class="px-6 py-4 text-center text-sm text-gray-500">暂无订单数据</td>
            </tr>
            <tr v-for="order in recentOrders" :key="order.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ order.orderNumber }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ order.receiverName }}
                <div class="text-xs text-gray-400">{{ order.receiverPhone }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ order.senderName }}
                <div class="text-xs text-gray-400">{{ order.senderPhone }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(order.createTime) }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-center">
                <span :class="getStatusClass(order.status)" class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full">
                  {{ getStatusText(order.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">
                <button @click="viewOrderDetail(order.orderNumber)" class="text-orange-500 hover:text-orange-700 mr-2">查看</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { getStaffDashboardData, getStoreRecentOrders, getStoreOrderStatusDistribution, getStoreOrderTrend } from '../../api/staffDashboard'
import { getOrderDetail } from '../../api/orders'

// 引用DOM元素
const orderStatusChart = ref(null)
const orderTrendChart = ref(null)

// 图表实例
let statusChartInstance = null
let trendChartInstance = null

// 路由
const router = useRouter()

// 数据状态
const dashboardData = ref({
  todayOrders: 0,
  pendingOrders: 0,
  inDeliveryOrders: 0,
  completedOrders: 0
})
const recentOrders = ref([])

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    const res = await getStaffDashboardData()
    dashboardData.value = res.data || dashboardData.value
  } catch (error) {
    console.error('获取门店仪表盘数据失败:', error)
  }
}

// 获取最近订单
const fetchRecentOrders = async () => {
  try {
    const res = await getStoreRecentOrders(10)
    recentOrders.value = res.data || []
  } catch (error) {
    console.error('获取门店最近订单失败:', error)
  }
}

// 初始化订单状态分布图表
const initOrderStatusChart = async () => {
  try {
    const res = await getStoreOrderStatusDistribution()
    const data = res.data || []
    
    if (orderStatusChart.value) {
      statusChartInstance = echarts.init(orderStatusChart.value)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: data.map(item => item.name)
        },
        color: ['#FF6700', '#FF9E5C', '#FFB74D', '#FFCC80', '#FFE0B2'],
        series: [
          {
            name: '订单状态',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: data
          }
        ]
      }
      
      statusChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('初始化门店订单状态图表失败:', error)
  }
}

// 初始化订单趋势图表
const initOrderTrendChart = async () => {
  try {
    const res = await getStoreOrderTrend(7)
    const data = res.data || { dates: [], counts: [] }
    
    if (orderTrendChart.value) {
      trendChartInstance = echarts.init(orderTrendChart.value)
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.dates,
          axisLine: {
            lineStyle: {
              color: '#999'
            }
          },
          axisLabel: {
            color: '#666'
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#666'
          },
          splitLine: {
            lineStyle: {
              color: '#eee'
            }
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        series: [
          {
            name: '订单数',
            type: 'line',
            data: data.counts,
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: {
              color: '#FF6700'
            },
            lineStyle: {
              color: '#FF6700',
              width: 3
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: 'rgba(255, 103, 0, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(255, 103, 0, 0.1)'
                }
              ])
            }
          }
        ]
      }
      
      trendChartInstance.setOption(option)
    }
  } catch (error) {
    console.error('初始化门店订单趋势图表失败:', error)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态文本
const getStatusText = (status) => {
  // 后端使用数字表示状态
  const statusMap = {
    0: '待取件',
    1: '已取件',
    2: '运输中',
    3: '已送达',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || `未知(${status})`
}

// 获取状态样式
const getStatusClass = (status) => {
  // 后端使用数字表示状态
  const statusClassMap = {
    0: 'bg-yellow-100 text-yellow-800',  // 待取件
    1: 'bg-blue-100 text-blue-800',      // 已取件
    2: 'bg-indigo-100 text-indigo-800',  // 运输中
    3: 'bg-green-100 text-green-800',    // 已送达
    4: 'bg-green-100 text-green-800',    // 已完成
    5: 'bg-red-100 text-red-800'         // 已取消
  }
  return statusClassMap[status] || 'bg-gray-100 text-gray-800'
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  if (statusChartInstance) statusChartInstance.resize()
  if (trendChartInstance) trendChartInstance.resize()
}

onMounted(async () => {
  // 获取数据
  await fetchDashboardData()
  await fetchRecentOrders()
  
  // 初始化图表
  initOrderStatusChart()
  initOrderTrendChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 查看订单详情
const viewOrderDetail = async (orderNumber) => {
  try {
    const res = await getOrderDetail(orderNumber)
    if (res.code === 200) {
      // 跳转到订单管理页面并显示该订单详情
      router.push({
        path: '/staff/orders',
        query: { orderNumber }
      })
    } else {
      console.error('获取订单详情失败:', res.message)
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

onUnmounted(() => {
  // 销毁图表实例
  if (statusChartInstance) statusChartInstance.dispose()
  if (trendChartInstance) trendChartInstance.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>