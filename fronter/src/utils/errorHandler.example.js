/**
 * 前端错误处理使用指南
 * 
 * 本文件展示如何在Vue组件中正确使用错误处理机制
 */

// ============================================
// 1. 基本用法 - 自动错误提示
// ============================================
// request.js 会自动处理所有错误提示，无需手动捕获
import { submitCode } from '@/api/record'

async function basicUsage() {
  try {
    const result = await submitCode(data)
    // 成功时的处理
    console.log('提交成功', result)
  } catch (error) {
    // 错误已由 request.js 自动提示
    // 这里只需记录日志或做额外处理
    console.error('操作失败', error)
  }
}

// ============================================
// 2. 使用错误处理工具函数
// ============================================
import { withErrorHandler, showSuccess } from '@/utils/errorHandler'
import { addProblem } from '@/api/problem'

async function withErrorHandlerUsage() {
  await withErrorHandler(
    () => addProblem(problemData),
    {
      successMsg: '题目创建成功',
      onError: (error) => {
        // 自定义错误处理逻辑
        console.error('创建题目失败', error)
      }
    }
  )
}

// ============================================
// 3. 条件性显示成功消息
// ============================================
import { editProblem } from '@/api/problem'

async function conditionalSuccess() {
  try {
    await editProblem(problemData)
    // 只在需要时显示成功消息
    showSuccess('更新成功')
  } catch (error) {
    // 错误已自动提示
    console.error('更新失败', error)
  }
}

// ============================================
// 4. 表单验证 + API调用
// ============================================
import { register } from '@/api/user'

async function formValidationExample(formData) {
  // 1. 前端验证
  if (!formData.username) {
    ElMessage.warning('用户名不能为空')
    return
  }
  
  if (!validateEmail(formData.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  
  // 2. API调用（错误自动提示）
  try {
    await register(formData)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    // 错误已由 request.js 处理
    console.error('注册失败', error)
  }
}

// ============================================
// 5. 批量操作错误处理
// ============================================
import { delProblem } from '@/api/problem'

async function batchDelete(ids) {
  const failedIds = []
  
  for (const id of ids) {
    try {
      await delProblem(id)
    } catch (error) {
      // 记录失败的ID
      failedIds.push(id)
      console.error(`删除题目 ${id} 失败`, error)
    }
  }
  
  if (failedIds.length === 0) {
    ElMessage.success('全部删除成功')
  } else {
    ElMessage.warning(`部分删除失败: ${failedIds.join(', ')}`)
  }
}

// ============================================
// 6. 响应码检查
// ============================================
import { RESPONSE_CODE, isSuccess } from '@/utils/request'

async function checkResponseCode() {
  try {
    const result = await someApi()
    
    // 检查响应码
    if (isSuccess(result.code)) {
      console.log('操作成功')
    }
    
    // 或直接比较
    if (result.code === RESPONSE_CODE.SUCCESS) {
      console.log('操作成功')
    }
  } catch (error) {
    console.error('请求失败', error)
  }
}

// ============================================
// 7. 全局错误监听（可选）
// ============================================
// 在 main.js 中添加全局错误处理
/*
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue 错误:', err)
  console.error('错误信息:', info)
  ElNotification({
    title: '应用错误',
    message: '发生未知错误，请刷新页面重试',
    type: 'error'
  })
}
*/

// ============================================
// 8. Loading 状态管理
// ============================================
import { ref } from 'vue'
import { getProblemInfo } from '@/api/problem'

function loadingStateExample() {
  const loading = ref(false)
  const problem = ref(null)
  
  async function loadProblem(id) {
    loading.value = true
    try {
      const result = await getProblemInfo(id)
      problem.value = result.data
    } catch (error) {
      // 错误已自动提示
      console.error('加载题目失败', error)
    } finally {
      loading.value = false
    }
  }
  
  return { loading, problem, loadProblem }
}

// ============================================
// 9. 重试机制
// ============================================
async function retryRequest(apiCall, maxRetries = 3) {
  let lastError
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await apiCall()
    } catch (error) {
      lastError = error
      console.warn(`第 ${i + 1} 次尝试失败`, error)
      
      // 等待一段时间后重试（指数退避）
      if (i < maxRetries - 1) {
        await new Promise(resolve => setTimeout(resolve, 1000 * Math.pow(2, i)))
      }
    }
  }
  
  throw lastError
}

// 使用示例
/*
await retryRequest(() => submitCode(data), 3)
*/

// ============================================
// 10. 错误边界组件（高级用法）
// ============================================
// 创建 ErrorBoundary.vue 组件捕获子组件错误
/*
<template>
  <div v-if="hasError" class="error-boundary">
    <h3>组件加载失败</h3>
    <el-button @click="retry">重试</el-button>
  </div>
  <slot v-else></slot>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'

const hasError = ref(false)
const error = ref(null)

onErrorCaptured((err) => {
  hasError.value = true
  error.value = err
  console.error('子组件错误:', err)
  return false // 阻止错误向上传播
})

function retry() {
  hasError.value = false
  error.value = null
}
</script>
*/
