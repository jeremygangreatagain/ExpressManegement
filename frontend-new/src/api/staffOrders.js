import request from '../utils/request'

/**
 * 获取当前员工所属门店的订单列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} [params.keyword] - 搜索关键词 (可选)
 * @param {number} [params.status] - 订单状态 (可选)
 * @returns {Promise}
 */
export function getStoreOrders(params) {
  // 移除 storeId，后端会从认证信息中获取
  const { storeId, ...restParams } = params;
  console.log('【API调用】获取门店订单列表，参数 (storeId removed):', restParams);
  return request({
    url: '/staff/orders', // 后端接口路径不变
    method: 'get',
    params: restParams // 发送不含 storeId 的参数
  }).then(response => {
    console.log('【API响应】获取门店订单列表，响应:', response);
    return response;
  }).catch(error => {
    console.error('【API错误】获取门店订单列表失败:', error);
    throw error;
  });
}

/**
 * 获取订单详情
 * @param {string} idOrOrderNumber - 订单ID或订单号
 * @returns {Promise}
 */
export function getOrderDetail(idOrOrderNumber) {
  // 无论是ID还是订单号，都作为字符串传递
  const param = String(idOrOrderNumber);
  return request({
    // 修正 URL 路径为单数 /order/
    url: `/staff/order/${param}`,
    method: 'get'
  })
}

/**
 * 获取订单物流信息
 * @param {string} idOrOrderNumber - 订单ID或订单号
 * @returns {Promise}
 */
export function getOrderLogistics(idOrOrderNumber) {
  // 无论是ID还是订单号，都作为字符串传递
  const param = String(idOrOrderNumber);
  return request({
    url: `/staff/orders/${param}/logistics`,
    method: 'get'
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
 * @param {string} [data.orderNumber] - 订单号（可选，作为备用标识）
 * @returns {Promise}
 */
export function updateOrderStatus(data) {
  console.log('【API调用】更新订单状态，参数:', data);
  return request({
    url: '/staff/order-status',
    method: 'put',
    params: data
  })
}

/**
 * 添加物流信息
 * @param {Object} data - 物流数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.status - 物流状态
 * @param {string} data.content - 物流内容
 * @param {string} data.location - 物流地点
 * @param {number} data.operatorId - 操作员ID
 * @param {string} data.operatorName - 操作员姓名
 * @returns {Promise}
 */
export function addLogisticsInfo(data) {
  console.log('【API调用】添加物流信息，参数:', data);
  return request({
    url: '/staff/logistics',
    method: 'post',
    data
  }).then(response => {
    console.log('【API响应】添加物流信息，响应:', response);
    return response;
  }).catch(error => {
    console.error('【API错误】添加物流信息失败:', error);
    throw error;
  });
}

/**
 * 提交订单删除申请
 * @param {Object} data - 申请数据
 * @param {string} data.orderNumber - 订单号
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
