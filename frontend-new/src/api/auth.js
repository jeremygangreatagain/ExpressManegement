import request from '../utils/request'

// 获取验证码
export function getCaptcha() {
  return request({
    url: '/captcha/generate',
    method: 'get'
  })
}

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 检查用户名是否可用
export function checkUsername(username) {
  return request({
    url: `/auth/check-username?username=${username}`,
    method: 'get'
  })
}