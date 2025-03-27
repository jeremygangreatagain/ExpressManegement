import request from '../utils/request'

// 获取管理员仪表盘数据
export function getAdminDashboardData() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

// 获取最近订单列表
export function getRecentOrders(limit = 10) {
  return request({
    url: `/admin/orders/recent?limit=${limit}`,
    method: 'get'
  })
}

// 获取订单状态分布数据
export function getOrderStatusDistribution() {
  return request({
    url: '/admin/orders/status-distribution',
    method: 'get'
  })
}

// 获取近期订单趋势数据
export function getOrderTrend(days = 7) {
  return request({
    url: `/admin/orders/trend?days=${days}`,
    method: 'get'
  })
}