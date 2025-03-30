import request from '@/utils/request';

/**
 * 获取员工列表 (分页)
 * @param {Object} params - 查询参数
 * @param {number} params.page - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词 (姓名/手机号/门店)
 * @returns {Promise}
 */
export function getStaffs(params) {
  return request({
    url: '/admin/staffs', // 假设后端员工列表 API 端点
    method: 'get',
    params
  });
}

/**
 * 添加员工
 * @param {Object} data - 员工数据
 * @returns {Promise}
 */
export function addStaff(data) {
  return request({
    url: '/admin/staffs', // 假设后端添加员工 API 端点
    method: 'post',
    data
  });
}

/**
 * 更新员工信息 (包括状态)
 * @param {Object} data - 员工数据 (必须包含 id)
 * @returns {Promise}
 */
export function updateStaff(data) {
  // 确保 username 存在
  if (!data || !data.username) {
    return Promise.reject(new Error('更新员工信息必须提供 username'));
  }
  return request({
    url: `/admin/staffs/username/${data.username}`, // 使用 username
    method: 'put',
    data
  });
}

/**
 * 删除员工 (by username)
 * @param {string} username - 员工用户名
 * @returns {Promise}
 */
export function deleteStaff(username) {
  return request({
    url: `/admin/staffs/username/${username}`, // 使用 username
    method: 'delete'
  });
}

/**
 * 批量删除员工
 * @param {Array<number>} ids - 员工 ID 数组
 * @returns {Promise}
 */
export function batchDeleteStaffs(ids) {
  return request({
    url: '/admin/staffs/batch', // 假设后端批量删除员工 API 端点
    method: 'delete',
    data: { ids } // 确保发送的数据格式为 { "ids": [...] }
  });
}

// 注意：请根据实际后端 API 端点调整上述 URL。
