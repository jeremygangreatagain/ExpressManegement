import request from '../utils/request'

/**
 * 获取当前用户相关的订单列表（作为寄件人或收件人）
 * @param {Object} params - 查询参数，包括分页、搜索关键词等
 * @returns {Promise}
 */
export function getUserOrders(params) {
  return request({
    url: '/user/orders',
    method: 'get',
    params
  })
}

/**
 * 获取与当前用户相关的订单列表（作为寄件人或收件人）
 * @param {Object} params - 查询参数，包括分页、搜索关键词、过滤条件等
 * @returns {Promise}
 */
export function getUserRelatedOrders(params) {
  return request({
    url: '/user/related-orders',
    method: 'get',
    params
  })
}

/**
 * 获取用户订单详情
 * @param {String|Number} orderId - 订单ID或订单号
 * @returns {Promise}
 */
export function getUserOrderDetail(orderId) {
  // 确保ID或订单号作为字符串传递
  const param = String(orderId);
  return request({
    url: `/user/orders/${param}`,
    method: 'get'
  })
}

/**
 * 用户确认收货
 * @param {String|Number} orderId - 订单ID
 * @returns {Promise}
 */
export function confirmOrderReceipt(orderId) {
  return request({
    url: `/user/orders/${orderId}/confirm-receipt`,
    method: 'put'
  })
}