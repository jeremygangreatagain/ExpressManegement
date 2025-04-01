import request from '../utils/request'

// 获取员工仪表盘数据
export function getStaffDashboardData() {
  return request({
    url: '/staff/dashboard/stats',
    method: 'get'
  })
}

// 获取门店最近订单列表
export function getStoreRecentOrders(limit = 10) {
  return request({
    url: `/staff/orders/recent?limit=${limit}`,
    method: 'get'
  })
}

// 获取门店订单状态分布数据
export function getStoreOrderStatusDistribution() {
  return request({
    url: '/staff/orders/status-distribution',
    method: 'get'
  })
}

// 获取门店近期订单趋势数据
export function getStoreOrderTrend(days = 7) {
  return request({
    url: `/staff/orders/trend?days=${days}`,
    method: 'get'
  })
}