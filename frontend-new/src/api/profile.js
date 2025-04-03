import request from '@/utils/request';

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getCurrentUserInfo() {
  return request({
    url: '/user/profile',
    method: 'get'
  });
}

/**
 * 获取当前员工信息
 * @returns {Promise}
 */
export function getCurrentStaffInfo() {
  return request({
    url: '/staff/profile', // 使用员工信息接口
    method: 'get'
  });
}

/**
 * 更新当前用户信息
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function updateCurrentUserInfo(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  });
}

/**
 * 修改密码
 * @param {Object} data - 包含旧密码和新密码
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @returns {Promise}
 */
export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  });
}

/**
 * 更新当前员工信息
 * @param {Object} data - 员工数据
 * @returns {Promise}
 */
export function updateStaffInfo(data) {
  return request({
    url: '/staff/info',
    method: 'put',
    data
  });
}
