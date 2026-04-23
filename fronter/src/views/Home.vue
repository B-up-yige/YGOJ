<template>
  <div class="home">
    <!-- YGOJ Logo Section -->
    <div class="logo-section fade-in">
      <div class="logo-icon-large">⚡</div>
      <h1 class="logo-title">YGOJ</h1>
      <p class="logo-subtitle">Online Judge System</p>
    </div>

    <el-row :gutter="24">
      <!-- 快捷操作 -->
      <el-col :xs="24" :sm="8">
        <el-card shadow="always" class="action-card slide-in">
          <template #header>
            <div class="card-header">
              <span><el-icon><Operation /></el-icon> 快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <button @click="goToProblems" class="action-btn action-btn-primary">
              <el-icon><Document /></el-icon>
              <span>浏览题目</span>
            </button>
            <button @click="goToContests" class="action-btn action-btn-success">
              <el-icon><Trophy /></el-icon>
              <span>浏览比赛</span>
            </button>
            <button @click="goToProblemsets" class="action-btn action-btn-warning">
              <el-icon><Collection /></el-icon>
              <span>浏览题集</span>
            </button>
            <button @click="goToRecords" class="action-btn" style="background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);">
              <el-icon><List /></el-icon>
              <span>查看提交</span>
            </button>
            <button v-if="isAdmin" @click="createProblem" class="action-btn" style="background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);">
              <el-icon><Plus /></el-icon>
              <span>创建题目</span>
            </button>
          </div>
        </el-card>
      </el-col>

      <!-- 最新提交 -->
      <el-col :xs="24" :sm="8">
        <el-card shadow="always" class="record-card slide-in" style="animation-delay: 0.1s;">
          <template #header>
            <div class="card-header">
              <span><el-icon><TrendCharts /></el-icon> 最新提交</span>
              <el-button link type="primary" @click="goToRecords" size="small">查看更多</el-button>
            </div>
          </template>
          <div class="record-list">
            <div v-for="record in recentRecords" :key="record.id" class="record-item" @click="viewRecord(record.id)">
              <div class="record-info">
                <span class="record-id">#{{ record.id }}</span>
                <span class="record-problem">题目 #{{ record.problemId }}</span>
              </div>
              <el-tag :type="getStatusType(record.status)" size="small">
                {{ getStatusText(record.status) }}
              </el-tag>
            </div>
            <el-empty v-if="recentRecords.length === 0" description="暂无提交记录" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 最近比赛 -->
      <el-col :xs="24" :sm="8">
        <el-card shadow="always" class="contest-card slide-in" style="animation-delay: 0.15s;">
          <template #header>
            <div class="card-header">
              <span><el-icon><Timer /></el-icon> 最近比赛</span>
              <el-button link type="primary" @click="goToContests" size="small">查看更多</el-button>
            </div>
          </template>
          <div class="contest-list">
            <div v-for="contest in recentContests" :key="contest.id" class="contest-item" @click="viewContest(contest.id)">
              <div class="contest-info">
                <h4 class="contest-title">{{ contest.title }}</h4>
                <p class="contest-time">
                  <el-icon><Timer /></el-icon>
                  {{ formatContestTime(contest.startTime) }}
                </p>
              </div>
              <el-tag :type="getContestStatusType(contest.status)" size="small">
                {{ getContestStatusText(contest.status) }}
              </el-tag>
            </div>
            <el-empty v-if="recentContests.length === 0" description="暂无比赛" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新题库 -->
    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="24">
        <el-card shadow="always" class="problem-card slide-in" style="animation-delay: 0.2s;">
          <template #header>
            <div class="card-header">
              <span><el-icon><Notebook /></el-icon> 最新题库</span>
              <el-button link type="primary" @click="goToProblems" size="small">查看更多</el-button>
            </div>
          </template>
          <div class="problem-list-full">
            <div v-for="problem in latestProblems" :key="problem.id" class="problem-item-full" @click="viewProblem(problem.id)">
              <div class="problem-info-full">
                <span class="problem-id">#{{ problem.id }}</span>
                <span class="problem-title">{{ problem.title }}</span>
              </div>
              <div class="problem-meta">
                <el-tag v-if="problem.difficulty" :type="getDifficultyType(problem.difficulty)" size="small">
                  {{ getDifficultyText(problem.difficulty) }}
                </el-tag>
                <span v-if="problem.accepted" class="problem-stats">
                  通过: {{ problem.accepted }} / {{ problem.submitted || 0 }}
                </span>
              </div>
            </div>
            <el-empty v-if="latestProblems.length === 0" description="暂无题目" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRecordList } from '@/api/record'
import { getContestList } from '@/api/contest'
import { getProblemList } from '@/api/problem'
import { Trophy, Collection, Document, List, Plus, Operation, TrendCharts, Bell, Timer, Notebook } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => {
  // 这里可以根据用户角色判断，暂时返回 false
  return false
})

const recentRecords = ref([])
const recentContests = ref([])
const latestProblems = ref([])

const getStatusType = (status) => {
  const statusMap = {
    'Accepted': 'success',
    'Wrong Answer': 'danger',
    'Time Limit Exceeded': 'warning',
    'Memory Limit Exceeded': 'warning',
    'Runtime Error': 'danger',
    'Compilation Error': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'Accepted': '通过',
    'Wrong Answer': '答案错误',
    'Time Limit Exceeded': '超时',
    'Memory Limit Exceeded': '超内存',
    'Runtime Error': '运行错误',
    'Compilation Error': '编译错误'
  }
  return textMap[status] || status
}

const goToProblems = () => {
  router.push('/problems')
}

const goToContests = () => {
  router.push('/contests')
}

const goToProblemsets = () => {
  router.push('/problemsets')
}

const goToRecords = () => {
  router.push('/records')
}

const createProblem = () => {
  router.push('/problem/create')
}

const viewRecord = (id) => {
  router.push(`/record/${id}`)
}

const viewContest = (id) => {
  router.push(`/contest/${id}`)
}

const viewProblem = (id) => {
  router.push(`/problem/${id}`)
}

const formatContestTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getContestStatusType = (status) => {
  const statusMap = {
    'upcoming': 'info',
    'running': 'success',
    'finished': 'warning'
  }
  return statusMap[status] || 'info'
}

const getContestStatusText = (status) => {
  const textMap = {
    'upcoming': '未开始',
    'running': '进行中',
    'finished': '已结束'
  }
  return textMap[status] || status
}

const getDifficultyType = (difficulty) => {
  const typeMap = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return typeMap[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  return difficulty || '未知'
}

const loadRecentRecords = async () => {
  try {
    const res = await getRecordList(1, 10) // 获取第一页，10 条记录
    recentRecords.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新记录失败:', error)
  }
}

const loadRecentContests = async () => {
  try {
    const res = await getContestList(1, 5, '') // 获取第一页，5 条比赛
    recentContests.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最近比赛失败:', error)
  }
}

const loadLatestProblems = async () => {
  try {
    const res = await getProblemList(1, 10, '', '') // 获取第一页，10 条题目
    latestProblems.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新题目失败:', error)
  }
}

onMounted(() => {
  loadRecentRecords()
  loadRecentContests()
  loadLatestProblems()
})
</script>

<style scoped>
.home {
  max-width: 1280px;
  margin: 0 auto;
}

/* Logo Section */
.logo-section {
  text-align: center;
  padding: var(--spacing-2xl) 0;
  margin-bottom: var(--spacing-xl);
}

.logo-icon-large {
  font-size: 4rem;
  margin-bottom: var(--spacing-md);
  animation: pulse 2s ease-in-out infinite;
  display: inline-block;
}

@keyframes pulse {
  0%, 100% { 
    opacity: 1;
    transform: scale(1);
  }
  50% { 
    opacity: 0.8;
    transform: scale(1.05);
  }
}

.logo-title {
  font-size: 3.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.03em;
  margin: 0 0 var(--spacing-sm);
  line-height: 1;
}

.logo-subtitle {
  font-size: 1.125rem;
  color: var(--color-text-secondary);
  font-weight: 500;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin: 0;
}

/* Card Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 1.125rem;
  color: var(--color-text-primary);
}

.card-header span {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.card-header span .el-icon {
  font-size: 1.25rem;
  color: var(--color-primary);
}

/* Element Plus Cards */
:deep(.el-card) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  transition: all var(--transition-base);
  overflow: hidden;
}

:deep(.el-card:hover) {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  border-color: var(--color-primary-light);
}

:deep(.el-card__header) {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(to right, rgba(37, 99, 235, 0.02), transparent);
}

:deep(.el-card__body) {
  padding: var(--spacing-xl);
}

/* Quick Actions */
.action-card .quick-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.action-btn {
  height: 48px;
  width: 100%;
  border: none;
  border-radius: var(--radius-md);
  padding: 0 var(--spacing-lg);
  font-size: 0.9375rem;
  font-weight: 500;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.action-btn .el-icon {
  font-size: 1.125rem;
  flex-shrink: 0;
}

.action-btn span {
  text-align: center;
}

.action-btn-primary {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
}

.action-btn-primary:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.action-btn-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.action-btn-success:hover {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.action-btn-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.action-btn-warning:hover {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.action-btn-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.action-btn-warning:hover {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow-md);
}

.action-btn .el-icon {
  font-size: 1.125rem;
  flex-shrink: 0;
}

.action-btn span {
  flex: 1;
  text-align: center;
}

/* Record List */
.record-card .record-list {
  max-height: 320px;
  overflow-y: auto;
}

/* Contest List */
.contest-card .contest-list {
  max-height: 320px;
  overflow-y: auto;
}

.contest-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  background-color: var(--color-bg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.contest-item:hover {
  background-color: rgba(16, 185, 129, 0.04);
  border-color: #10b981;
  transform: translateX(4px);
}

.contest-info {
  flex: 1;
  min-width: 0;
}

.contest-title {
  margin: 0 0 var(--spacing-xs);
  font-size: 0.9375rem;
  color: var(--color-text-primary);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contest-time {
  margin: 0;
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.contest-time .el-icon {
  font-size: 0.875rem;
}

/* Problem List - Full Width */
.problem-card .problem-list-full {
  max-height: 500px;
  overflow-y: auto;
}

.problem-item-full {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-sm);
  background-color: var(--color-bg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.problem-item-full:hover {
  background-color: rgba(245, 158, 11, 0.04);
  border-color: #f59e0b;
  transform: translateX(4px);
}

.problem-info-full {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex: 1;
  min-width: 0;
}

.problem-info-full .problem-id {
  font-weight: 600;
  color: var(--color-primary);
  font-size: 1rem;
  flex-shrink: 0;
}

.problem-info-full .problem-title {
  color: var(--color-text-primary);
  font-size: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.problem-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

.problem-stats {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  background-color: var(--color-bg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.record-item:hover {
  background-color: rgba(37, 99, 235, 0.04);
  border-color: var(--color-primary-light);
  transform: translateX(4px);
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-id {
  font-weight: 600;
  color: var(--color-primary);
  font-size: 0.9375rem;
}

.record-problem {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

/* Timeline */
.notice-card :deep(.el-timeline) {
  padding-top: var(--spacing-sm);
}

.notice-card :deep(.el-timeline-item__node) {
  width: 12px;
  height: 12px;
  border: 2px solid;
}

.notice-card :deep(.el-timeline-item__timestamp) {
  font-size: 0.8125rem;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.notice-card :deep(.el-timeline-item__content) {
  margin-top: var(--spacing-sm);
}

.notice-item {
  border: none;
  box-shadow: none;
  background-color: transparent;
  padding: 0;
}

.notice-item h4 {
  margin: 0 0 var(--spacing-xs);
  font-size: 1rem;
  color: var(--color-text-primary);
  font-weight: 600;
}

.notice-item p {
  margin: 0;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

/* Empty State */
:deep(.el-empty) {
  padding: var(--spacing-xl) 0;
}

/* Responsive */
@media (max-width: 1200px) {
  .el-col {
    margin-bottom: var(--spacing-xl);
  }
}

@media (max-width: 768px) {
  .home {
    padding: 0;
  }
  
  :deep(.el-card__body) {
    padding: var(--spacing-lg);
  }
}
</style>
