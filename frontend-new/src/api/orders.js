import request from '../utils/request'

// 获取订单列表（分页）
export function getOrderList(params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(idOrOrderNumber) {
  // 无论是ID还是订单号，都作为字符串传递
  const param = String(idOrOrderNumber);
  console.log('发送订单详情请求，ID/订单号:', param);
  return request({
    url: `/admin/orders/${param}`,
    method: 'get'
  })
}

// 更新订单信息
export function updateOrder(idOrOrderNumber, data) {
  // 确保ID或订单号作为字符串传递，避免JavaScript处理大整数时的精度问题
  const param = String(idOrOrderNumber);
  return request({
    url: `/admin/orders/${param}`,
    method: 'put',
    data
  })
}

// 删除订单
export function deleteOrder(idOrOrderNumber) {
  // 无论是ID还是订单号，都作为字符串传递
  const param = String(idOrOrderNumber);
  return request({
    url: `/admin/orders/${param}`,
    method: 'delete'
  })
}

// 批量删除订单
export function batchDeleteOrders(ids) {
  return request({
    url: '/admin/orders/batch',
    method: 'delete',
    data: { ids }
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/admin/orders',
    method: 'post',
    data
  })
}

// 获取订单状态选项
export function getOrderStatusOptions() {
  return request({
    url: '/orders/status-options',
    method: 'get'
  })
}

// 添加订单物流信息
export function addOrderLogistics(data) {
  return request({
    url: '/admin/logistics',
    method: 'post',
    data
  })
}

// 获取订单物流信息
export function getOrderLogistics(orderId) {
  return request({
    url: `/admin/orders/${orderId}/logistics`,
    method: 'get'
  })
}