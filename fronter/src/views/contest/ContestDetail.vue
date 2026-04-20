<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="contest-detail" v-loading="loading">
    <!-- 比赛头部信息 -->
    <el-card class="contest-header-card">
      <div class="contest-header">
        <div class="contest-title-section">
          <h1 class="contest-title">{{ contest.title }}</h1>
          <div class="contest-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              <span>作者：用户 {{ contest.authorId }}</span>
            </span>
            <span class="meta-separator">•</span>
            <span class="meta-item">
              <el-tag :type="getStatusType(contest.status)" size="small">
                {{ getStatusText(contest.status) }}
              </el-tag>
            </span>
          </div>
          <div class="contest-time-info">
            <div class="time-item">
              <el-icon><Clock /></el-icon>
              <span class="time-label">开始时间</span>
              <span class="time-value">{{ formatDateTime(contest.startTime) }}</span>
            </div>
            <div class="time-item">
              <el-icon><Clock /></el-icon>
              <span class="time-label">结束时间</span>
              <span class="time-value">{{ formatDateTime(contest.endTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容区域 -->
    <div class="main-content">
      <!-- 比赛内容 -->
      <el-card class="contest-content-card">
        <!-- 比赛时间提示 -->
        <el-alert
          v-if="!canSubmit"
          :title="timeAlertMessage"
          :type="alertType"
          show-icon
          style="margin-bottom: 20px;"
        />

        <!-- 比赛描述 -->
        <div class="contest-section">
          <h2 class="section-title">比赛描述</h2>
          <div class="section-content">
            <p>{{ contest.description || '暂无描述' }}</p>
          </div>
        </div>

        <el-divider />

        <!-- 比赛题目 -->
        <div class="contest-section">
          <div class="section-header">
            <h2 class="section-title">比赛题目</h2>
            <el-button 
              type="primary" 
              @click="showAddProblemDialog" 
              v-permission="PERMISSIONS.PERM_CONTEST_MANAGE"
            >
              添加题目
            </el-button>
          </div>
          <el-table :data="problems" style="width: 100%">
            <el-table-column prop="problemLabel" label="题号" width="100" />
            <el-table-column prop="problemId" label="题目ID" width="120" />
            <el-table-column label="我的状态" width="150">
              <template #default="scope">
                <el-tag v-if="userProgress[scope.row.problemId]" :type="getStatusTagType(userProgress[scope.row.problemId])">
                  {{ getStatusText(userProgress[scope.row.problemId]) }}
                </el-tag>
                <span v-else style="color: var(--color-text-tertiary);">未提交</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button 
                  link 
                  type="primary" 
                  @click="viewProblem(scope.row.problemId, scope.row.problemLabel)"
                  :disabled="!canSubmit"
                >
                  {{ canSubmit ? '做题' : (now < new Date(contest.startTime) ? '未开始' : '已结束') }}
                </el-button>
                <el-button 
                  link 
                  type="danger" 
                  @click="handleDeleteProblem(scope.row.problemId)" 
                  v-permission="PERMISSIONS.PERM_CONTEST_MANAGE"
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
        <el-button size="large" @click="editContest" v-permission="PERMISSIONS.PERM_CONTEST_MANAGE" class="sidebar-btn">
          <el-icon><Edit /></el-icon>
          <span>编辑比赛</span>
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
        <el-form-item label="题号" required>
          <el-input v-model="addProblemForm.problemLabel" placeholder="请输入题号（如：A、B、C）" />
        </el-form-item>
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Clock, Edit, Back } from '@element-plus/icons-vue'
import { getContestInfo, getContestProblems, addContestProblem, delContestProblem, getUserContestProgress } from '@/api/contest'
import { useUserStore, PERMISSIONS } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const notFound = ref(false)
const contest = ref({})
const problems = ref([])
const userProgress = ref({}) // 用户过题情况
const addProblemDialogVisible = ref(false)
const addProblemForm = ref({
  contestId: null,
  problemId: null,
  problemLabel: ''
})
const adding = ref(false)
const canSubmit = ref(true)
const timeAlertMessage = ref('')
const alertType = ref('info')
const now = ref(new Date())

const loadContest = async () => {
  loading.value = true
  try {
    const res = await getContestInfo(route.params.id)
    contest.value = res.data
    
    // 检查比赛时间
    checkContestTime()
    
    // 加载比赛题目
    const problemsRes = await getContestProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
    
    // 加载用户过题情况
    await loadUserProgress()
  } catch (error) {
    console.error('加载比赛详情失败:', error)
    // 检查是否是资源不存在的错误
    const errorMsg = error.message || ''
    if (errorMsg.includes('不存在') || errorMsg.includes('404') || 
        (error.response && (error.response.status === 404 || error.response.status === 403))) {
      notFound.value = true
    } else {
      ElMessage.error('加载比赛详情失败')
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
    const res = await getUserContestProgress(userId, route.params.id)
    if (res.data) {
      userProgress.value = res.data
    }
  } catch (error) {
    console.error('加载用户过题情况失败:', error)
  }
}

// 检查比赛时间
const checkContestTime = () => {
  if (!contest.value || !contest.value.startTime || !contest.value.endTime) return
  
  const currentTime = new Date()
  now.value = currentTime
  const startTime = new Date(contest.value.startTime)
  const endTime = new Date(contest.value.endTime)
  
  if (currentTime < startTime) {
    canSubmit.value = false
    timeAlertMessage.value = `比赛尚未开始，开始时间：${formatDateTime(contest.value.startTime)}`
    alertType.value = 'warning'
  } else if (currentTime > endTime) {
    canSubmit.value = false
    timeAlertMessage.value = `比赛已结束，结束时间：${formatDateTime(contest.value.endTime)}`
    alertType.value = 'error'
  } else {
    canSubmit.value = true
    timeAlertMessage.value = ''
    alertType.value = 'success'
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusType = (status) => {
  const types = {
    UPCOMING: 'info',
    RUNNING: 'success',
    ENDED: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    UPCOMING: '未开始',
    RUNNING: '进行中',
    ENDED: '已结束',
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

// 获取状态标签类型
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

const viewProblem = (problemId, problemLabel) => {
  // 跳转到比赛中的题目页面，携带比赛ID和题号
  router.push(`/contest/${route.params.id}/problem/${problemLabel}`)
}

const showAddProblemDialog = () => {
  addProblemForm.value = {
    contestId: parseInt(route.params.id),
    problemId: null,
    problemLabel: ''
  }
  addProblemDialogVisible.value = true
}

const handleAddProblem = async () => {
  if (!addProblemForm.value.problemLabel || !addProblemForm.value.problemLabel.trim()) {
    ElMessage.warning('请输入题号')
    return
  }
  if (!addProblemForm.value.problemId) {
    ElMessage.warning('请输入题目ID')
    return
  }

  adding.value = true
  try {
    await addContestProblem(addProblemForm.value)
    ElMessage.success('添加题目成功')
    addProblemDialogVisible.value = false
    // 重新加载题目列表
    const problemsRes = await getContestProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    console.error('添加题目失败:', error)
    const errorMsg = error.response?.data?.message || '添加题目失败'
    ElMessage.error(errorMsg)
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

    await delContestProblem(parseInt(route.params.id), problemId)
    ElMessage.success('删除成功')
    // 重新加载题目列表
    const problemsRes = await getContestProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题目失败:', error)
      ElMessage.error('删除题目失败')
    }
  }
}

const goBack = () => {
  router.back()
}

const editContest = () => {
  router.push(`/contest/edit/${route.params.id}`)
}

onMounted(() => {
  loadContest()
})
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.contest-detail {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 比赛头部卡片 */
.contest-header-card {
  margin-bottom: 20px;
}

.contest-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.contest-title-section {
  flex: 1;
}

.contest-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.3;
}

[data-theme='dark'] .contest-title {
  text-shadow: 0 0 15px rgba(102, 126, 234, 0.6);
}

.contest-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
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

.contest-time-info {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 6px;
  border-left: 3px solid #667eea;
}

[data-theme='dark'] .time-item {
  background: rgba(102, 126, 234, 0.1);
}

.time-item .el-icon {
  font-size: 18px;
  color: #667eea;
}

.time-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.time-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* 主体内容区域 */
.main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* 比赛内容卡片 */
.contest-content-card {
  flex: 1;
  min-width: 0;
}

.contest-section {
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
  white-space: pre-wrap;
}

.section-content p {
  margin: 0;
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

  .contest-content-card {
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

  .contest-time-info {
    gap: 10px;
  }

  .time-item {
    padding: 6px 12px;
  }

  .time-label {
    font-size: 11px;
  }

  .time-value {
    font-size: 12px;
  }
}
</style>
