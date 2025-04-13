import request from '../utils/request'

// 获取操作日志列表（分页）
export function getOperationLogs(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

// 获取日志详情
export function getLogDetail(id) {
  return request({
    url: `/admin/logs/${id}`,
    method: 'get'
  })
}

// 导出日志为CSV (管理员)
export function exportLogsCsv(params) { // Renamed function
  return request({
    url: '/admin/logs/export',
    method: 'get',
    params,
    responseType: 'blob' // 指定响应类型为blob，用于文件下载
  })
}

// 导出员工工作日志为CSV
export function exportStaffLogsCsv(params) {
  return request({
    url: '/staff/logs/export',
    method: 'get',
    params,
    responseType: 'blob' // 指定响应类型为blob，用于文件下载
  })
}

// 获取日志类型选项
export function getLogTypeOptions() {
  return request({
    url: '/admin/logs/types',
    method: 'get'
  })
}
