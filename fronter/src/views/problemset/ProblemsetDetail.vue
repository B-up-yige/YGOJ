<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="problemset-detail" v-loading="loading">
    <!-- 题集头部信息 -->
    <el-card class="problemset-header-card">
      <div class="problemset-header">
        <div class="problemset-title-section">
          <h1 class="problemset-title">{{ problemset.title }}</h1>
          <div class="problemset-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              <span>作者：{{ problemset.author?.nickname || '未知用户' }}</span>
            </span>
            <span class="meta-separator">•</span>
            <span class="meta-item">
              <el-tag :type="problemset.isPublic ? 'success' : 'info'" size="small">
                {{ problemset.isPublic ? '公开' : '私有' }}
              </el-tag>
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容区域 -->
    <div class="main-content">
      <!-- 题集内容 -->
      <el-card class="problemset-content-card">
        <!-- 题集描述 -->
        <div class="problemset-section">
          <h2 class="section-title">题集描述</h2>
          <div class="section-content markdown-body" v-html="renderedDescription"></div>
        </div>

        <el-divider />

        <!-- 题集题目 -->
        <div class="problemset-section">
          <div class="section-header">
            <h2 class="section-title">题集题目</h2>
            <el-button 
              v-if="canEditOrDelete()"
              type="primary" 
              @click="showAddProblemDialog"
            >
              添加题目
            </el-button>
          </div>
          <el-table :data="problems" style="width: 100%">
            <el-table-column prop="problemTitle" label="题目标题" min-width="250">
              <template #default="scope">
                <span>{{ scope.row.problemTitle || '未知题目' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="我的状态" width="150">
              <template #default="scope">
                <el-tag v-if="userProgress[scope.row.problemId]" :type="getStatusTagType(userProgress[scope.row.problemId])">
                  {{ getStatusText(userProgress[scope.row.problemId]) }}
                </el-tag>
                <span v-else style="color: var(--color-text-tertiary);">未提交</span>
              </template>
            </el-table-column>
            <el-table-column label="添加时间">
              <template #default="scope">
                {{ formatDateTime(scope.row.addedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button link type="primary" @click="viewProblem(scope.row.problemId)">
                  查看题目
                </el-button>
                <el-button 
                  v-if="canEditOrDelete()"
                  link 
                  type="danger" 
                  @click="handleDeleteProblem(scope.row.problemId)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 侧边操作栏 -->
      <div class="sidebar">
        <el-button 
          v-if="canEditOrDelete()"
          size="large" 
          @click="editProblemset"
          class="sidebar-btn"
        >
          <el-icon><Edit /></el-icon>
          <span>编辑题集</span>
        </el-button>
        <el-button size="large" @click="goBack" class="sidebar-btn">
          <el-icon><Back /></el-icon>
          <span>返回</span>
        </el-button>
      </div>
    </div>

    <!-- 添加题目对话框 -->
    <el-dialog v-model="addProblemDialogVisible" title="添加题目" width="500px">
      <el-form :model="addProblemForm" label-width="100px">
        <el-form-item label="题目ID" required>
          <el-input-number v-model="addProblemForm.problemId" :min="1" placeholder="请输入题目ID" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addProblemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddProblem" :loading="adding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Back } from '@element-plus/icons-vue'
import { getProblemsetInfo, getProblemsetProblems, addProblemsetProblem, delProblemsetProblem, getUserProblemsetProgress } from '@/api/problemset'
import { useUserStore, PERMISSIONS } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const notFound = ref(false)
const problemset = ref({})
const problems = ref([])
const userProgress = ref({}) // 用户过题情况
const addProblemDialogVisible = ref(false)
const addProblemForm = ref({
  problemsetId: null,
  problemId: null
})
const adding = ref(false)

// 渲染 Markdown 描述
const renderedDescription = computed(() => {
  return renderMarkdown(problemset.value.description || '暂无描述')
})

const loadProblemset = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    const res = await getProblemsetInfo(route.params.id, userId)
    problemset.value = res.data
    
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
    
    // 加载用户过题情况
    await loadUserProgress()
  } catch (error) {
    console.error('加载题集详情失败:', error)
    // 检查是否是资源不存在或无权限的错误
    const errorMsg = error.message || ''
    if (errorMsg.includes('不存在') || errorMsg.includes('404') || errorMsg.includes('无权') ||
        (error.response && (error.response.status === 403 || error.response.status === 404))) {
      notFound.value = true
    } else {
      ElMessage.error('加载题集详情失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载用户过题情况
const loadUserProgress = async () => {
  const userId = userStore.userInfo?.id || localStorage.getItem('userId')
  if (!userId) return
  
  try {
    const res = await getUserProblemsetProgress(userId, route.params.id)
    if (res.data) {
      userProgress.value = res.data
    }
  } catch (error) {
    console.error('加载用户过题情况失败:', error)
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  const texts = {
    AC: '通过',
    WA: '答案错误',
    TLE: '超时',
    MLE: '超内存',
    RE: '运行错误',
    CE: '编译错误',
    waiting: '判题中'
  }
  return texts[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    AC: 'success',
    WA: 'danger',
    TLE: 'warning',
    MLE: 'warning',
    RE: 'danger',
    CE: 'info',
    waiting: ''
  }
  return types[status] || 'info'
}

const viewProblem = (problemId) => {
  router.push(`/problem/${problemId}`)
}

// 检查是否可以编辑或删除题集（创建者或管理员）
const canEditOrDelete = () => {
  const currentUserId = userStore.userInfo?.id || localStorage.getItem('userId')
  if (!currentUserId) return false
  
  // 如果是创建者，可以编辑删除
  if (problemset.value.authorId === parseInt(currentUserId)) {
    return true
  }
  
  // 如果有管理权限，也可以编辑删除
  return userStore.hasPermission(PERMISSIONS.PERM_PROBLEMSET_MANAGE_OWN) || 
         userStore.hasPermission(PERMISSIONS.PERM_PROBLEMSET_MANAGE_ALL)
}

const showAddProblemDialog = () => {
  addProblemForm.value = {
    problemsetId: parseInt(route.params.id),
    problemId: null
  }
  addProblemDialogVisible.value = true
}

const handleAddProblem = async () => {
  if (!addProblemForm.value.problemId) {
    ElMessage.warning('请输入题目ID')
    return
  }

  adding.value = true
  try {
    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    await addProblemsetProblem(addProblemForm.value, userId)
    ElMessage.success('添加题目成功')
    addProblemDialogVisible.value = false
    // 重新加载题目列表
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    console.error('添加题目失败:', error)
    if (error.response && error.response.status === 403) {
      ElMessage.error('无权向该题集添加题目')
    } else {
      const errorMsg = error.response?.data?.message || '添加题目失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    adding.value = false
  }
}

const handleDeleteProblem = async (problemId) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    await delProblemsetProblem(parseInt(route.params.id), problemId, userId)
    ElMessage.success('删除成功')
    // 重新加载题目列表
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题目失败:', error)
      if (error.response && error.response.status === 403) {
        ElMessage.error('无权从该题集删除题目')
      } else {
        ElMessage.error('删除题目失败')
      }
    }
  }
}

const goBack = () => {
  router.back()
}

const editProblemset = () => {
  router.push(`/problemset/edit/${route.params.id}`)
}

onMounted(() => {
  loadProblemset()
})
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.problemset-detail {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 题集头部卡片 */
.problemset-header-card {
  margin-bottom: 20px;
}

.problemset-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.problemset-title-section {
  flex: 1;
}

.problemset-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

[data-theme='dark'] .problemset-title {
  text-shadow: 0 0 15px rgba(102, 126, 234, 0.6);
}

.problemset-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.meta-item .el-icon {
  font-size: 16px;
  color: #667eea;
}

.meta-separator {
  color: var(--color-text-secondary);
  opacity: 0.5;
}

/* 主体内容区域 */
.main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* 题集内容卡片 */
.problemset-content-card {
  flex: 1;
  min-width: 0;
}

.problemset-section {
  margin: 24px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
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

:deep(.el-divider) {
  margin: 24px 0;
  border-color: rgba(102, 126, 234, 0.2);
}

[data-theme='dark'] :deep(.el-divider) {
  border-color: rgba(102, 126, 234, 0.3);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }

  .problemset-content-card {
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
}
</style>
