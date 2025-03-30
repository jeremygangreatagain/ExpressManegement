import request from '@/utils/request';

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  });
}

/**
 * 获取用户详情
 * @param {number} id - 用户ID
 * @returns {Promise}
 */
export function getUserDetail(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  });
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function createUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  });
}

/**
 * 更新用户
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function updateUser(data) {
  return request({
    url: '/admin/users',
    method: 'put',
    data
  });
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise}
 */
export function deleteUser(username) {
  return request({
    url: `/admin/users/${username}`,
    method: 'delete'
  });
}

/**
 * 批量删除用户
 * @param {Array<string>} usernames - 用户名数组
 * @returns {Promise}
 */
export function batchDeleteUsers(usernames) {
  return request({
    url: '/admin/users/batch',
    method: 'delete',
    data: { ids: usernames }
  });
}