import { ElMessage, ElNotification } from 'element-plus'
import { RESPONSE_CODE } from '@/utils/request'

/**
 * 统一的成功消息提示
 * @param {string} message - 成功消息
 */
export function showSuccess(message = '操作成功') {
  ElMessage.success(message)
}

/**
 * 统一的警告消息提示
 * @param {string} message - 警告消息
 */
export function showWarning(message) {
  ElMessage.warning(message)
}

/**
 * 统一的错误消息提示
 * @param {string} message - 错误消息
 */
export function showError(message = '操作失败') {
  ElMessage.error(message)
}

/**
 * 统一的通知提示
 * @param {Object} options - 通知配置
 * @param {string} options.title - 标题
 * @param {string} options.message - 消息内容
 * @param {string} options.type - 类型: success/warning/info/error
 * @param {number} options.duration - 显示时长(毫秒)
 */
export function showNotification({ 
  title = '提示', 
  message, 
  type = 'info', 
  duration = 3000 
}) {
  ElNotification({
    title,
    message,
    type,
    duration
  })
}

/**
 * 异步请求的错误处理包装器
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 配置选项
 * @param {string} options.successMsg - 成功消息
 * @param {string} options.errorMsg - 自定义错误消息
 * @param {boolean} options.showSuccess - 是否显示成功消息
 * @param {Function} options.onError - 错误回调函数
 * @returns {Promise} 返回处理后的Promise
 * 
 * @example
 * // 基本用法
 * await withErrorHandler(() => submitCode(data), {
 *   successMsg: '提交成功',
 *   onError: (error) => console.error(error)
 * })
 */
export async function withErrorHandler(asyncFn, options = {}) {
  const {
    successMsg,
    errorMsg,
    showSuccess: shouldShowSuccess = false,
    onError
  } = options

  try {
    const result = await asyncFn()
    
    if (shouldShowSuccess && successMsg) {
      showSuccess(successMsg)
    }
    
    return result
  } catch (error) {
    const message = errorMsg || error.message || '操作失败'
    
    // 调用自定义错误回调
    if (onError) {
      onError(error)
    }
    
    // 默认不显示错误消息，因为request.js已经处理了
    // 如果需要额外提示，可以手动调用showError
    throw error
  }
}

/**
 * 检查响应码是否为成功
 * @param {number} code - 响应码
 * @returns {boolean}
 */
export function isSuccess(code) {
  return code === RESPONSE_CODE.SUCCESS
}

/**
 * 检查响应码是否为客户端错误
 * @param {number} code - 响应码
 * @returns {boolean}
 */
export function isClientError(code) {
  return code >= 400 && code < 500
}

/**
 * 检查响应码是否为服务器错误
 * @param {number} code - 响应码
 * @returns {boolean}
 */
export function isServerError(code) {
  return code >= 500
}

/**
 * 获取响应码对应的描述
 * @param {number} code - 响应码
 * @returns {string}
 */
export function getCodeDescription(code) {
  const descriptions = {
    [RESPONSE_CODE.SUCCESS]: '成功',
    [RESPONSE_CODE.BAD_REQUEST]: '请求参数错误',
    [RESPONSE_CODE.UNAUTHORIZED]: '未授权',
    [RESPONSE_CODE.NOT_FOUND]: '资源不存在',
    [RESPONSE_CODE.SERVER_ERROR]: '服务器错误'
  }
  return descriptions[code] || `未知错误 (${code})`
}
