<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="problem-detail" v-loading="loading">
    <!-- 题目头部信息 -->
    <el-card class="problem-header-card">
      <div class="problem-header">
        <div class="problem-title-section">
          <h1 class="problem-title">{{ problem.title }}</h1>
          <div class="problem-author">
            <span class="author-label">作者：</span>
            <el-tag type="info" size="small">用户 {{ problem.authorId }}</el-tag>
          </div>
          <div class="problem-tags-line" v-if="tags.length > 0">
            <div class="header-tags">
              <el-tag
                v-for="tag in tags"
                :key="tag"
                size="small"
                class="header-tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
          <div class="problem-id-line">
            <span class="problem-id">题目 {{ problem.id }}</span>
          </div>
        </div>
        <div class="problem-limits">
          <div class="limit-item">
            <el-icon><Timer /></el-icon>
            <span class="limit-label">时间限制</span>
            <span class="limit-value">{{ problem.timeLimit }} ms</span>
          </div>
          <div class="limit-item">
            <el-icon><Coin /></el-icon>
            <span class="limit-label">内存限制</span>
            <span class="limit-value">{{ problem.memoryLimit }} MB</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容区域：题目内容 + 侧边栏 -->
    <div class="main-content">
      <!-- 题目主要内容 -->
      <el-card class="problem-content-card">
        <!-- 未登录提示 -->
        <el-alert
          v-if="!isLoggedIn"
          type="info"
          :closable="false"
          class="login-alert"
        >
          <template #default>
            <div class="alert-content">
              <el-icon><InfoFilled /></el-icon>
              <span>您需要登录后才能提交代码。</span>
              <el-button type="primary" size="small" @click="router.push('/login')">立即登录</el-button>
              <el-button size="small" @click="router.push('/register')">注册账号</el-button>
            </div>
          </template>
        </el-alert>

        <!-- 题目描述 -->
        <div class="problem-section">
          <h2 class="section-title">题目描述</h2>
          <div class="section-content markdown-body" v-html="renderedDescription"></div>
        </div>
      </el-card>

      <!-- 侧边操作栏 -->
      <div class="sidebar">
        <el-button type="primary" size="large" @click="showSubmitDialog" class="sidebar-btn">
          <el-icon><Upload /></el-icon>
          <span>提交代码</span>
        </el-button>
        <el-button size="large" @click="viewMySubmissions" class="sidebar-btn">
          <el-icon><List /></el-icon>
          <span>我的提交</span>
        </el-button>
        <el-button 
          type="warning" 
          size="large" 
          @click="editProblem" 
          v-permission="PERMISSIONS.PERM_PROBLEM_EDIT"
          class="sidebar-btn"
        >
          <el-icon><Edit /></el-icon>
          <span>编辑题目</span>
        </el-button>
        <el-button size="large" @click="goBack" class="sidebar-btn">
          <el-icon><Back /></el-icon>
          <span>返回</span>
        </el-button>
      </div>
    </div>

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
import { Timer, Coin, Upload, List, Edit, Back, InfoFilled } from '@element-plus/icons-vue'
import { getProblemInfo, getProblemTags } from '@/api/problem'
import { submitCode } from '@/api/record'
import { useUserStore, PERMISSIONS } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const submitDialogVisible = ref(false)
const notFound = ref(false)

// 计算是否已登录
const isLoggedIn = computed(() => !!userStore.token)

// 渲染 Markdown 描述
const renderedDescription = computed(() => {
  return renderMarkdown(problem.value.description || '暂无描述')
})

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
    // 跳转到记录页面，默认筛选我的提交和当前题目
    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    router.push(`/records?userId=${userId}&problemId=${route.params.id}`)
  } catch (error) {
    // request.js 已经处理了错误提示，这里只需要记录日志
    console.error('提交失败:', error)
    // 如果需要额外的错误处理，可以在这里添加
  } finally {
    submitting.value = false
  }
}

const viewMySubmissions = () => {
  const userId = userStore.userInfo?.id || localStorage.getItem('userId')
  if (!userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  // 跳转到该用户在该题目的提交记录
  router.push(`/records?userId=${userId}&problemId=${route.params.id}`)
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
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 题目头部卡片 */
.problem-header-card {
  margin-bottom: 20px;
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.problem-title-section {
  flex: 1;
}

.problem-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

[data-theme='dark'] .problem-title {
  text-shadow: 0 0 15px rgba(102, 126, 234, 0.6);
}

.problem-id {
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.problem-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 6px;
  border-left: 3px solid #667eea;
  width: fit-content;
}

[data-theme='dark'] .problem-author {
  background: rgba(102, 126, 234, 0.1);
}

.author-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.problem-tags-line {
  margin-bottom: 8px;
}

.header-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.header-tag-item {
  cursor: default;
  transition: all 0.3s ease;
  border-radius: 4px;
}

.header-tag-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.problem-id-line {
  margin-top: 4px;
}

.problem-id {
  font-size: 13px;
  color: var(--color-text-secondary);
  opacity: 0.7;
  font-style: italic;
}

.problem-limits {
  display: flex;
  gap: 30px;
  align-items: center;
}

.limit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 8px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

[data-theme='dark'] .limit-item {
  background: rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
}

.limit-item .el-icon {
  font-size: 20px;
  color: #667eea;
}

.limit-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.limit-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* 主体内容区域 */
.main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* 题目内容卡片 */
.problem-content-card {
  flex: 1;
  min-width: 0;
}

/* 登录提示 */
.login-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.alert-content .el-icon {
  font-size: 18px;
  color: #667eea;
}

/* 侧边操作栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 160px;
  position: sticky;
  top: 20px;
}

.sidebar-btn {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: auto;
  padding: 16px 12px;
  margin: 0 !important;
  border: 1px solid var(--color-border) !important;
}

/* 统一所有按钮的边框和背景 */
.sidebar :deep(.el-button) {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.sidebar :deep(.el-button--primary) {
  background: var(--color-surface) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-primary) !important;
}

.sidebar :deep(.el-button:hover) {
  background: rgba(102, 126, 234, 0.1) !important;
  border-color: #667eea !important;
}

.sidebar-btn .el-icon {
  font-size: 24px;
}

.sidebar-btn span {
  font-size: 13px;
  text-align: center;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .problem-content-card {
    order: 1;
  }

  .sidebar {
    order: 2;
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
    position: static;
  }

  .sidebar-btn {
    flex: 1;
    min-width: calc(50% - 6px);
    padding: 12px 8px;
  }

  .sidebar-btn .el-icon {
    font-size: 20px;
  }

  .sidebar-btn span {
    font-size: 12px;
  }

  .problem-limits {
    gap: 15px;
  }

  .limit-item {
    padding: 6px 12px;
  }

  .limit-label {
    font-size: 11px;
  }

  .limit-value {
    font-size: 14px;
  }
}

.problem-section {
  margin: 24px 0;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
}

[data-theme='dark'] .section-title {
  text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

.section-content {
  line-height: 1.8;
  color: var(--color-text-secondary);
  font-size: 15px;
}

.section-content p {
  margin: 0;
}

/* Markdown 样式 */
.markdown-body {
  color: var(--color-text-primary);
  font-size: 15px;
  line-height: 1.8;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--color-text-primary);
}

.markdown-body :deep(h1) {
  font-size: 2em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--color-border);
}

.markdown-body :deep(h2) {
  font-size: 1.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid var(--color-border);
}

.markdown-body :deep(h3) {
  font-size: 1.25em;
}

.markdown-body :deep(h4) {
  font-size: 1em;
}

.markdown-body :deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-top: 0;
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(blockquote) {
  margin: 16px 0;
  padding: 0 1em;
  color: var(--color-text-secondary);
  border-left: 0.25em solid #667eea;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 4px;
}

[data-theme='dark'] .markdown-body :deep(blockquote) {
  background: rgba(102, 126, 234, 0.1);
}

.markdown-body :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(102, 126, 234, 0.1);
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  color: var(--color-text-primary);
}

.markdown-body :deep(pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 6px;
  margin: 16px 0;
  border: 1px solid var(--color-border);
}

[data-theme='dark'] .markdown-body :deep(pre) {
  background-color: rgba(0, 0, 0, 0.3);
  border-color: rgba(102, 126, 234, 0.3);
}

.markdown-body :deep(pre code) {
  display: inline;
  max-width: 100%;
  padding: 0;
  margin: 0;
  overflow: visible;
  line-height: inherit;
  word-wrap: normal;
  background-color: transparent;
  border: 0;
}

.markdown-body :deep(table) {
  border-spacing: 0;
  border-collapse: collapse;
  margin: 16px 0;
  width: 100%;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  padding: 6px 13px;
  border: 1px solid var(--color-border);
}

.markdown-body :deep(table tr) {
  background-color: transparent;
  border-top: 1px solid var(--color-border);
}

.markdown-body :deep(table tr:nth-child(2n)) {
  background-color: rgba(102, 126, 234, 0.03);
}

[data-theme='dark'] .markdown-body :deep(table tr:nth-child(2n)) {
  background-color: rgba(102, 126, 234, 0.08);
}

.markdown-body :deep(table th) {
  font-weight: 600;
  background-color: rgba(102, 126, 234, 0.05);
}

[data-theme='dark'] .markdown-body :deep(table th) {
  background-color: rgba(102, 126, 234, 0.1);
}

.markdown-body :deep(a) {
  color: #667eea;
  text-decoration: none;
  transition: all 0.2s ease;
}

.markdown-body :deep(a:hover) {
  color: #764ba2;
  text-decoration: underline;
}

.markdown-body :deep(img) {
  max-width: 100%;
  box-sizing: border-box;
  border-radius: 6px;
  margin: 16px 0;
}

.markdown-body :deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background-color: var(--color-border);
  border: 0;
}

/* 标签容器 */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: default;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* Element Plus 卡片科技风格 */
:deep(.el-card) {
  background: var(--color-surface);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

[data-theme='dark'] :deep(.el-card) {
  background: rgba(255, 255, 255, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

[data-theme='dark'] :deep(.el-card:hover) {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5);
  border-color: rgba(102, 126, 234, 0.3);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-divider) {
  margin: 24px 0;
  border-color: rgba(102, 126, 234, 0.2);
}

[data-theme='dark'] :deep(.el-divider) {
  border-color: rgba(102, 126, 234, 0.3);
}

/* 按钮科技风格 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  //box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  font-weight: 500;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

:deep(.el-button) {
  transition: all 0.3s ease;
  font-weight: 500;
}

:deep(.el-button:hover) {
  transform: translateY(-1px);
}

/* 对话框样式 */
:deep(.el-dialog) {
  background: var(--color-surface);
  backdrop-filter: blur(10px);
  border: 1px solid var(--color-border);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

[data-theme='dark'] :deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(15, 12, 41, 0.95) 0%, rgba(48, 43, 99, 0.95) 100%);
  border: 1px solid rgba(102, 126, 234, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

:deep(.el-dialog__title) {
  color: var(--color-text-primary);
}

[data-theme='dark'] :deep(.el-dialog__title) {
  color: #fff;
  text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--color-text-secondary);
}

[data-theme='dark'] :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #e0e0e0;
}

:deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #667eea;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  color: var(--color-text-primary);
}

[data-theme='dark'] :deep(.el-form-item__label) {
  color: #e0e0e0;
}

:deep(.el-textarea__inner) {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  color: var(--color-text-primary);
  font-family: 'Consolas', 'Monaco', monospace;
}

[data-theme='dark'] :deep(.el-textarea__inner) {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #e0e0e0;
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.3);
}

/* 警告框样式 */
:deep(.el-alert) {
  background: rgba(102, 126, 234, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
}

[data-theme='dark'] :deep(.el-alert) {
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
}

:deep(.el-alert__title) {
  color: var(--color-text-primary);
}

[data-theme='dark'] :deep(.el-alert__title) {
  color: #fff;
}

:deep(.el-alert__description) {
  color: var(--color-text-secondary);
}

[data-theme='dark'] :deep(.el-alert__description) {
  color: #e0e0e0;
}
</style>
