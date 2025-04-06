import request from '../utils/request'

/**
 * 获取员工工作日志列表 (分页)
 * @param {Object} params - 查询参数
 * @param {number} params.page - 当前页码
 * @param {number} params.size - 每页大小
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise}
 */
export function getWorkLogs(params) {
  return request({
    url: '/staff/logs',
    method: 'get',
    params
  })
}

/**
 * 获取工作日志详情
 * @param {number} id - 日志ID
 * @returns {Promise}
 */
export function getWorkLogDetail(id) {
  return request({
    url: `/staff/logs/${id}`,
    method: 'get'
  })
}

/**
 * 添加工作日志
 * @param {Object} data - 日志数据
 * @returns {Promise}
 */
export function addWorkLog(data) {
  return request({
    url: '/staff/logs',
    method: 'post',
    data
  })
}

/**
 * 获取日志类型选项
 * @returns {Promise}
 */
export function getLogTypeOptions() {
  return request({
    url: '/staff/logs/types',
    method: 'get'
  })
}