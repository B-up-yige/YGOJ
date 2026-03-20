import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(loginStr, password) {
  return request({
    url: '/user/login',
    method: 'post',
    params: { loginStr, password }
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 获取用户信息
export function getUserinfo(id) {
  return request({
    url: `/user/userinfo/${id}`,
    method: 'get'
  })
}

// 根据用户名获取用户信息
export function getUserinfoByUsername(username) {
  return request({
    url: `/user/userinfo/username/${username}`,
    method: 'get'
  })
}

// 通过 token 获取用户 ID
export function getUserIdByToken(token) {
  return request({
    url: '/user/userinfo',
    method: 'post',
    data: { token }
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}
