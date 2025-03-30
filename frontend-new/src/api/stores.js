import request from '@/utils/request';

/**
 * 获取门店列表
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词
 * @returns {Promise}
 */
export function getStoreList(params) {
  return request({
    url: '/admin/stores',
    method: 'get',
    params
  });
}

/**
 * 获取门店详情
 * @param {number} id - 门店ID
 * @returns {Promise}
 */
export function getStoreDetail(id) {
  return request({
    url: `/admin/stores/${id}`,
    method: 'get'
  });
}

/**
 * 创建门店
 * @param {Object} data - 门店数据
 * @returns {Promise}
 */
export function createStore(data) {
  return request({
    url: '/admin/stores',
    method: 'post',
    data
  });
}

/**
 * 更新门店
 * @param {Object} data - 门店数据
 * @returns {Promise}
 */
export function updateStore(data) {
  return request({
    url: `/admin/stores/${data.id}`,
    method: 'put',
    data
  });
}

/**
 * 删除门店
 * @param {number} id - 门店ID
 * @returns {Promise}
 */
export function deleteStore(id) {
  return request({
    url: `/admin/stores/${id}`,
    method: 'delete'
  });
}

/**
 * 批量删除门店
 * @param {Array} ids - 门店ID数组
 * @returns {Promise}
 */
export function batchDeleteStores(ids) {
  return request({
    url: '/admin/stores/batch',
    method: 'delete',
    data: ids
  });
}

// 省市区数据已移至前端静态文件 src/utils/regionData.js