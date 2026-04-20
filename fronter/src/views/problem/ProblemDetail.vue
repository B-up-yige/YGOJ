<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="problem-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>{{ problem.title }}</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目 ID">{{ problem.id }}</el-descriptions-item>
        <el-descriptions-item label="作者 ID">{{ problem.authorId }}</el-descriptions-item>
        <el-descriptions-item label="时间限制">{{ problem.timeLimit }} ms</el-descriptions-item>
        <el-descriptions-item label="内存限制">{{ problem.memoryLimit }} MB</el-descriptions-item>
      </el-descriptions>

      <!-- 标签区域 -->
      <div class="tags-section" v-if="tags.length > 0">
        <h3>标签</h3>
        <div class="tags-container">
          <el-tag
            v-for="tag in tags"
            :key="tag"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <el-divider />

      <div class="section">
        <h3>题目描述</h3>
        <p>{{ problem.description || '暂无描述' }}</p>
      </div>

      <div class="actions">
        <el-button type="primary" @click="showSubmitDialog">提交代码</el-button>
        <el-button type="success" @click="viewRecords">查看记录</el-button>
        <el-button type="warning" @click="editProblem" v-permission="PERMISSIONS.PERM_PROBLEM_EDIT">编辑题目</el-button>
      </div>

      <!-- 未登录提示 -->
      <el-alert
        v-if="!isLoggedIn"
        title="提示"
        description="您需要登录后才能提交代码。请先登录或注册账号。"
        type="info"
        :closable="false"
        style="margin-top: 20px;"
      >
        <template #default>
          <div style="display: flex; align-items: center; gap: 10px;">
            <span>您需要登录后才能提交代码。</span>
            <el-button type="primary" size="small" @click="router.push('/login')">立即登录</el-button>
            <el-button size="small" @click="router.push('/register')">注册账号</el-button>
          </div>
        </template>
      </el-alert>
    </el-card>

    <!-- 提交代码对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交代码" width="60%">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="编程语言">
          <el-select v-model="submitForm.language" placeholder="请选择编程语言" style="width: 100%">
            <el-option label="Java" value="Java" />
            <el-option label="C++" value="C++" />
            <el-option label="C" value="C" />
            <el-option label="Python 3" value="Python 3" />
          </el-select>
        </el-form-item>
        <el-form-item label="代码">
          <el-input
            v-model="submitForm.code"
            type="textarea"
            :rows="15"
            placeholder="请输入代码"
            style="font-family: monospace;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCode" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProblemInfo, getProblemTags } from '@/api/problem'
import { submitCode } from '@/api/record'
import { useUserStore, PERMISSIONS } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const submitDialogVisible = ref(false)
const notFound = ref(false)

// 计算是否已登录
const isLoggedIn = computed(() => !!userStore.token)

const problem = ref({
  id: route.params.id,
  title: '',
  description: '',
  authorId: null,
  timeLimit: 0,
  memoryLimit: 0
})

const tags = ref([])

const submitForm = ref({
  language: 'Java',
  code: ''
})

const loadProblem = async () => {
  loading.value = true
  try {
    const res = await getProblemInfo(route.params.id)
    problem.value = res.data
    
    // 加载标签
    await loadTags()
  } catch (error) {
    console.error('加载题目失败:', error)
    // 检查是否是资源不存在的错误
    const errorMsg = error.message || ''
    if (errorMsg.includes('不存在') || errorMsg.includes('404') || 
        (error.response && (error.response.status === 404 || error.response.status === 403))) {
      notFound.value = true
    } else {
      ElMessage.error('加载题目失败')
    }
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const res = await getProblemTags(route.params.id)
    if (res.data) {
      // 后端返回的是 Tag 对象数组，需要提取 tag 字段
      tags.value = res.data.map(item => item.tag)
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const goBack = () => {
  router.back()
}

const showSubmitDialog = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  submitDialogVisible.value = true
}

const handleSubmitCode = async () => {
  if (!submitForm.value.code.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  submitting.value = true
  try {
    const data = {
      problemId: route.params.id,
      userId: userStore.userInfo?.id || localStorage.getItem('userId'),
      language: submitForm.value.language,
      code: submitForm.value.code
    }
    await submitCode(data)
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    // 跳转到记录页面查看结果
    router.push('/records')
  } catch (error) {
    // request.js 已经处理了错误提示，这里只需要记录日志
    console.error('提交失败:', error)
    // 如果需要额外的错误处理，可以在这里添加
  } finally {
    submitting.value = false
  }
}

const viewRecords = () => {
  router.push('/records')
}

const editProblem = () => {
  router.push(`/problem/edit/${route.params.id}`)
}

onMounted(() => {
  loadProblem()
})
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.problem-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #fff;
  text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
  font-size: 28px;
  font-weight: bold;
}

.section {
  margin: 20px 0;
}

.section h3 {
  margin-bottom: 10px;
  color: #fff;
  text-shadow: 0 0 8px rgba(102, 126, 234, 0.4);
  font-size: 20px;
}

.tags-section {
  margin: 20px 0;
}

.tags-section h3 {
  margin-bottom: 10px;
  color: #fff;
  text-shadow: 0 0 8px rgba(102, 126, 234, 0.4);
  font-size: 20px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.actions {
  margin-top: 30px;
  text-align: center;
}

/* Element Plus 卡片科技风格 */
:deep(.el-card) {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-descriptions) {
  --el-descriptions-table-border-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-descriptions__label) {
  color: #fff;
  background: rgba(102, 126, 234, 0.1);
}

:deep(.el-descriptions__content) {
  color: #e0e0e0;
}

/* 按钮科技风格 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(56, 239, 125, 0.4);
  transition: all 0.3s ease;
}

:deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(56, 239, 125, 0.6);
}

/* 对话框样式 */
:deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(15, 12, 41, 0.95) 0%, rgba(48, 43, 99, 0.95) 100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 126, 234, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

:deep(.el-dialog__title) {
  color: #fff;
  text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #e0e0e0;
}

:deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #667eea;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  color: #e0e0e0;
}

:deep(.el-textarea__inner) {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #e0e0e0;
  font-family: 'Consolas', 'Monaco', monospace;
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.3);
}

/* 警告框样式 */
:deep(.el-alert) {
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
}

:deep(.el-alert__title) {
  color: #fff;
}

:deep(.el-alert__description) {
  color: #e0e0e0;
}
</style>
