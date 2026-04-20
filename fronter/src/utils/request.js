import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'

// 响应码常量定义
export const RESPONSE_CODE = {
  SUCCESS: 200,           // 成功
  BAD_REQUEST: 400,       // 请求参数错误
  UNAUTHORIZED: 401,      // 未授权/token失效
  NOT_FOUND: 404,         // 资源不存在
  SERVER_ERROR: 500,      // 服务器内部错误
}

// 响应码对应的消息映射
const CODE_MESSAGE = {
  [RESPONSE_CODE.SUCCESS]: '操作成功',
  [RESPONSE_CODE.BAD_REQUEST]: '请求参数错误',
  [RESPONSE_CODE.UNAUTHORIZED]: '未授权，请重新登录',
  [RESPONSE_CODE.NOT_FOUND]: '请求的资源不存在',
  [RESPONSE_CODE.SERVER_ERROR]: '服务器内部错误',
}

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 成功响应
    if (res.code === RESPONSE_CODE.SUCCESS) {
      return res
    }
    
    // 错误处理
    const errorCode = res.code || RESPONSE_CODE.SERVER_ERROR
    const errorMsg = res.msg || CODE_MESSAGE[errorCode] || '请求失败'
    
    // 根据错误码分类处理
    switch (errorCode) {
      case RESPONSE_CODE.BAD_REQUEST:
        // 400 参数错误 - 显示警告消息
        ElMessage.warning(errorMsg)
        break
        
      case RESPONSE_CODE.UNAUTHORIZED:
        // 401 未授权 - 清除token并跳转登录
        ElNotification({
          title: '登录失效',
          message: '您的登录已过期，请重新登录',
          type: 'warning',
          duration: 3000
        })
        handleLogout()
        break
        
      case RESPONSE_CODE.NOT_FOUND:
        // 404 资源不存在 - 不显示消息，让页面自己处理
        // ElMessage.error(errorMsg)
        break
        
      case RESPONSE_CODE.SERVER_ERROR:
        // 500 服务器错误
        ElNotification({
          title: '系统错误',
          message: errorMsg,
          type: 'error',
          duration: 5000
        })
        break
        
      default:
        // 其他错误码
        ElMessage.error(errorMsg)
        break
    }
    
    // 返回拒绝的Promise，让调用方可以捕获错误
    return Promise.reject(new Error(errorMsg))
  },
  (error) => {
    // HTTP 网络错误处理
    let errorMsg = '网络错误'
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      switch (status) {
        case 400:
          errorMsg = '请求参数错误'
          break
        case 401:
          errorMsg = '未授权，请重新登录'
          handleLogout()
          break
        case 403:
          errorMsg = '拒绝访问'
          break
        case 404:
          errorMsg = '请求的资源不存在'
          break
        case 500:
          errorMsg = '服务器内部错误'
          break
        case 502:
          errorMsg = '网关错误'
          break
        case 503:
          errorMsg = '服务不可用'
          break
        case 504:
          errorMsg = '网关超时'
          break
        default:
          errorMsg = `请求失败 (${status})`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMsg = '网络连接失败，请检查网络'
    } else if (error.code === 'ECONNABORTED') {
      // 请求超时
      errorMsg = '请求超时，请稍后重试'
    }
    
    // 显示错误消息
    ElNotification({
      title: '请求失败',
      message: errorMsg,
      type: 'error',
      duration: 5000
    })
    
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 处理登出逻辑
 */
function handleLogout() {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  sessionStorage.clear()
  
  // 跳转到登录页（避免重复跳转）
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

export default request
