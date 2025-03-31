import request from '@/utils/request';

/**
 * 获取审核列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getAuditList(params) {
  return request({
    url: '/audit/list',
    method: 'get',
    params
  });
}

/**
 * 获取审核详情
 * @param {String} id - 审核ID
 * @returns {Promise}
 */
export function getAuditDetail(id) {
  return request({
    url: `/audit/${id}`,
    method: 'get'
  });
}

/**
 * 审批通过
 * @param {String} id - 审核ID
 * @param {Object} data - 审批数据
 * @returns {Promise}
 */
export function approveAudit(id, data) {
  return request({
    url: `/audit/${id}/approve`,
    method: 'post',
    data
  });
}

/**
 * 审批拒绝
 * @param {String} id - 审核ID
 * @param {Object} data - 拒绝原因等数据
 * @returns {Promise}
 */
export function rejectAudit(id, data) {
  return request({
    url: `/audit/${id}/reject`,
    method: 'post',
    data
  });
}

/**
 * 获取审核类型选项
 * @returns {Promise}
 */
export function getAuditTypeOptions() {
  return request({
    url: '/audit/types',
    method: 'get'
  });
}