import request from '../utils/request'

/**
 * 获取门店订单列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.storeId - 门店ID
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.status - 订单状态
 * @returns {Promise}
 */
export function getStoreOrders(params) {
  return request({
    url: '/staff/orders',
    method: 'get',
    params
  })
}

/**
 * 创建订单
 * @param {Object} data - 订单数据
 * @returns {Promise}
 */
export function createOrder(data) {
  return request({
    url: '/staff/orders',
    method: 'post',
    data
  })
}

/**
 * 更新订单信息
 * @param {string} orderNumber - 订单号
 * @param {Object} data - 订单数据
 * @returns {Promise}
 */
export function updateOrder(orderNumber, data) {
  return request({
    url: `/staff/orders/${orderNumber}`,
    method: 'put',
    data
  })
}

/**
 * 更新订单状态
 * @param {Object} data - 状态更新数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.status - 新状态
 * @param {number} data.staffId - 员工ID
 * @param {string} data.staffName - 员工姓名
 * @param {string} data.remark - 备注信息
 * @returns {Promise}
 */
export function updateOrderStatus(data) {
  return request({
    url: '/staff/order-status',
    method: 'put',
    params: data
  })
}

/**
 * 提交订单删除申请
 * @param {Object} data - 申请数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.staffId - 员工ID
 * @param {string} data.staffName - 员工姓名
 * @param {number} data.storeId - 门店ID
 * @param {string} data.storeName - 门店名称
 * @param {string} data.reason - 删除原因
 * @returns {Promise}
 */
export function submitOrderDeletionRequest(data) {
  return request({
    url: '/staff/order-deletion-request',
    method: 'post',
    params: data
  })
}

/**
 * 获取门店订单统计信息
 * @param {number} storeId - 门店ID
 * @returns {Promise}
 */
export function getStoreOrderStatistics(storeId) {
  return request({
    url: '/staff/order-statistics',
    method: 'get',
    params: { storeId }
  })
}