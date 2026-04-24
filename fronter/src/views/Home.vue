<template>
  <div class="home">
    <!-- YGOJ Logo Section -->
    <div class="logo-section fade-in">
      <div class="logo-icon-large">⚡</div>
      <h1 class="logo-title">YGOJ</h1>
      <p class="logo-subtitle">Online Judge System</p>
    </div>

    <!-- 第一行：比赛 + 题集 -->
    <el-row :gutter="24" style="margin-top: 32px;">
      <!-- 最近比赛 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="always" class="section-card slide-in" style="animation-delay: 0.1s;">
          <template #header>
            <div class="section-header">
              <div class="header-left">
                <div class="header-icon contest-icon">
                  <el-icon><Trophy /></el-icon>
                </div>
                <div class="header-text">
                  <h3 class="section-title">最近比赛</h3>
                  <p class="section-subtitle">Recent Contests</p>
                </div>
              </div>
              <el-button type="primary" link @click="goToContests" size="small">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="content-table">
            <div class="table-header contest-header">
              <div class="col-title">比赛名称</div>
              <div class="col-time">时间</div>
              <div class="col-status">状态</div>
            </div>
            <div v-for="contest in recentContests" :key="contest.id" 
                 class="table-row contest-row"
                 @click="viewContest(contest.id)">
              <div class="col-title">
                <span class="row-icon">🏆</span>
                <span class="text">{{ contest.title }}</span>
              </div>
              <div class="col-time">
                <el-icon><Timer /></el-icon>
                {{ formatContestTime(contest.startTime) }}
              </div>
              <div class="col-status">
                <el-tag :type="getContestStatusType(contest.status)" size="small" effect="light">
                  {{ getContestStatusText(contest.status) }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="recentContests.length === 0" description="暂无比赛" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 最新题集 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="always" class="section-card slide-in" style="animation-delay: 0.15s;">
          <template #header>
            <div class="section-header">
              <div class="header-left">
                <div class="header-icon problemset-icon">
                  <el-icon><FolderOpened /></el-icon>
                </div>
                <div class="header-text">
                  <h3 class="section-title">最新题集</h3>
                  <p class="section-subtitle">Latest Problem Sets</p>
                </div>
              </div>
              <el-button type="primary" link @click="goToProblemsets" size="small">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="content-table">
            <div class="table-header problemset-header">
              <div class="col-title">题集名称</div>
              <div class="col-count">题目数</div>
              <div class="col-author">创建者</div>
            </div>
            <div v-for="problemset in recentProblemsets" :key="problemset.id" 
                 class="table-row problemset-row"
                 @click="viewProblemset(problemset.id)">
              <div class="col-title">
                <span class="row-icon">📚</span>
                <span class="text">{{ problemset.title }}</span>
              </div>
              <div class="col-count">
                <el-icon><Document /></el-icon>
                {{ problemset.problemCount || 0 }} 题
              </div>
              <div class="col-author">
                <el-icon><User /></el-icon>
                {{ problemset.author?.nickname || problemset.author?.username || `#${problemset.authorId || '未知'}` }}
              </div>
            </div>
            <el-empty v-if="recentProblemsets.length === 0" description="暂无题集" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：题库 + 提交 -->
    <el-row :gutter="24" style="margin-top: 24px;">
      <!-- 最新题库 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="always" class="section-card slide-in" style="animation-delay: 0.2s;">
          <template #header>
            <div class="section-header">
              <div class="header-left">
                <div class="header-icon problem-icon">
                  <el-icon><Notebook /></el-icon>
                </div>
                <div class="header-text">
                  <h3 class="section-title">最新题库</h3>
                  <p class="section-subtitle">Problem Library</p>
                </div>
              </div>
              <el-button type="primary" link @click="goToProblems" size="small">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="content-table">
            <div class="table-header problem-header">
              <div class="col-id">题号</div>
              <div class="col-title">题目名称</div>
              <div class="col-stats">通过率</div>
            </div>
            <div v-for="problem in latestProblems" :key="problem.id" 
                 class="table-row problem-row"
                 @click="viewProblem(problem.id)">
              <div class="col-id">
                <span class="id-badge">#{{ problem.id }}</span>
              </div>
              <div class="col-title">
                <span class="row-icon">💻</span>
                <span class="text">{{ problem.title }}</span>
              </div>
              <div class="col-stats">
                <span class="stat-text">
                  {{ problem.accepted || 0 }} / {{ problem.submitted || 0 }}
                </span>
              </div>
            </div>
            <el-empty v-if="latestProblems.length === 0" description="暂无题目" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 最新提交 -->
      <el-col :xs="24" :sm="12">
        <el-card shadow="always" class="section-card slide-in" style="animation-delay: 0.25s;">
          <template #header>
            <div class="section-header">
              <div class="header-left">
                <div class="header-icon record-icon">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="header-text">
                  <h3 class="section-title">最新提交</h3>
                  <p class="section-subtitle">Latest Submissions</p>
                </div>
              </div>
              <el-button type="primary" link @click="goToRecords" size="small">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="content-table">
            <div class="table-header record-header">
              <div class="col-id">提交ID</div>
              <div class="col-problem">题目</div>
              <div class="col-user">用户</div>
              <div class="col-status">状态</div>
            </div>
            <div v-for="record in recentRecords" :key="record.id" 
                 class="table-row record-row"
                 @click="viewRecord(record.id)">
              <div class="col-id">
                <span class="id-badge">#{{ record.id }}</span>
              </div>
              <div class="col-problem">
                <span class="row-icon">📝</span>
                <span class="text">题目 #{{ record.problemId }}</span>
              </div>
              <div class="col-user">
                <el-icon><User /></el-icon>
                {{ record.userName || record.userNickname || `#${record.userId || '未知'}` }}
              </div>
              <div class="col-status">
                <el-tag :type="getStatusType(record.status)" size="small" effect="light">
                  {{ getStatusText(record.status) }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="recentRecords.length === 0" description="暂无提交记录" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 第三行：讨论 -->
    <el-row :gutter="24" style="margin-top: 24px; margin-bottom: 32px;">
      <el-col :span="24">
        <el-card shadow="always" class="section-card slide-in" style="animation-delay: 0.3s;">
          <template #header>
            <div class="section-header">
              <div class="header-left">
                <div class="header-icon discussion-icon">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="header-text">
                  <h3 class="section-title">最新讨论</h3>
                  <p class="section-subtitle">Recent Discussions</p>
                </div>
              </div>
              <el-button type="primary" link @click="goToDiscussions" size="small">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="content-table">
            <div class="table-header discussion-header">
              <div class="col-title">帖子标题</div>
              <div class="col-category">板块</div>
              <div class="col-author">作者</div>
              <div class="col-stats">回复/浏览</div>
            </div>
            <div v-for="post in recentPosts" :key="post.id" 
                 class="table-row discussion-row"
                 @click="viewPost(post.id)">
              <div class="col-title">
                <span class="row-icon">💬</span>
                <span class="text">{{ post.title }}</span>
              </div>
              <div class="col-category">
                <el-tag size="small" effect="light" type="info">
                  {{ post.categoryName || '综合' }}
                </el-tag>
              </div>
              <div class="col-author">
                <el-icon><User /></el-icon>
                {{ post.author?.username || post.author?.nickname || `#${post.authorId || '未知'}` }}
              </div>
              <div class="col-stats">
                <span class="stat-text">
                  💬 {{ post.commentCount || 0 }} / 👁️ {{ post.viewCount || 0 }}
                </span>
              </div>
            </div>
            <el-empty v-if="recentPosts.length === 0" description="暂无讨论" :image-size="80" />
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
import { getProblemsetList } from '@/api/problemset'
import { getPostList } from '@/api/discussion'
import { Trophy, Collection, Document, List, Plus, Operation, TrendCharts, Bell, Timer, Notebook, FolderOpened, ArrowRight, User, Clock, Cpu, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => {
  // 这里可以根据用户角色判断，暂时返回 false
  return false
})

const recentRecords = ref([])
const recentContests = ref([])
const latestProblems = ref([])
const recentProblemsets = ref([])
const recentPosts = ref([])

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

const goToDiscussions = () => {
  router.push('/discussions')
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

const viewProblemset = (id) => {
  router.push(`/problemset/${id}`)
}

const viewPost = (id) => {
  router.push(`/discussion/${id}`)
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
    const res = await getRecordList(1, 12) // 获取第一页，12 条记录
    recentRecords.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新记录失败:', error)
  }
}

const loadRecentContests = async () => {
  try {
    const res = await getContestList(1, 12, '') // 获取第一页，12 条比赛
    recentContests.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最近比赛失败:', error)
  }
}

const loadLatestProblems = async () => {
  try {
    const res = await getProblemList(1, 12, '', '') // 获取第一页，12 条题目
    latestProblems.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新题目失败:', error)
  }
}

const loadRecentProblemsets = async () => {
  try {
    const res = await getProblemsetList(1, 12, '') // 获取第一页，12 条题集
    recentProblemsets.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新题集失败:', error)
  }
}

const loadRecentPosts = async () => {
  try {
    const res = await getPostList({ page: 1, pageSize: 12 }) // 获取第一页，12 条帖子
    recentPosts.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新讨论失败:', error)
  }
}

onMounted(() => {
  loadRecentContests()
  loadRecentProblemsets()
  loadLatestProblems()
  loadRecentRecords()
  loadRecentPosts()
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

/* Section Cards */
.section-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

/* Section Header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
  flex-shrink: 0;
}

.contest-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.problemset-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.problem-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.record-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.discussion-icon {
  background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3);
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.section-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.section-subtitle {
  margin: 0;
  font-size: 0.8125rem;
  color: var(--color-text-tertiary);
  font-weight: 500;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

/* Content Table */
.content-table {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.table-header {
  display: grid;
  padding: var(--spacing-md) var(--spacing-lg);
  background: linear-gradient(to right, rgba(37, 99, 235, 0.03), transparent);
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  border-bottom: 2px solid var(--color-border);
  margin-bottom: var(--spacing-sm);
}

/* Contest header */
.contest-header {
  grid-template-columns: 2fr 2fr 1fr;
}

.contest-header .col-title {
  justify-self: start;
}

.contest-header .col-time {
  justify-self: start;
}

.contest-header .col-status {
  justify-self: center;
}

/* Problemset header */
.problemset-header {
  grid-template-columns: 2fr 1fr 1fr;
}

.problemset-header .col-title {
  justify-self: start;
}

.problemset-header .col-count {
  justify-self: center;
}

.problemset-header .col-author {
  justify-self: center;
}

/* Problem header */
.problem-header {
  grid-template-columns: 100px 2fr 120px;
}

.problem-header .col-id {
  justify-self: start;
}

.problem-header .col-title {
  justify-self: start;
}

.problem-header .col-stats {
  justify-self: end;
}

/* Record header */
.record-header {
  grid-template-columns: 100px 2fr 120px 120px;
}

.record-header .col-id {
  justify-self: start;
}

.record-header .col-problem {
  justify-self: start;
}

.record-header .col-user {
  justify-self: start;
}

.record-header .col-status {
  justify-self: center;
}

/* Discussion header */
.discussion-header {
  grid-template-columns: 2fr 100px 120px 150px;
}

.discussion-header .col-title {
  justify-self: start;
}

.discussion-header .col-category {
  justify-self: center;
}

.discussion-header .col-author {
  justify-self: start;
}

.discussion-header .col-stats {
  justify-self: end;
}

.table-row {
  display: grid;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
  align-items: center;
  gap: var(--spacing-md);
}

.table-row:hover {
  background-color: rgba(37, 99, 235, 0.04);
  border-color: var(--color-primary-light);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.row-icon {
  font-size: 1.125rem;
  margin-right: var(--spacing-xs);
}

.text {
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.id-badge {
  display: inline-block;
  padding: 2px 8px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.1), rgba(37, 99, 235, 0.05));
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 0.875rem;
  font-family: 'Courier New', monospace;
}

.stat-text {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.perf-stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8125rem;
  color: var(--color-text-secondary);
  margin-right: var(--spacing-md);
}

.perf-stat .el-icon {
  font-size: 0.875rem;
}

/* Contest Row */
.contest-row {
  grid-template-columns: 2fr 2fr 1fr;
}

.contest-row .col-title {
  justify-self: start;
}

.contest-row .col-time {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  justify-self: start;
}

.contest-row .col-time .el-icon {
  font-size: 1rem;
}

.contest-row .col-status {
  justify-self: center;
}

/* Problemset Row */
.problemset-row {
  grid-template-columns: 2fr 1fr 1fr;
}

.problemset-row .col-title {
  justify-self: start;
}

.problemset-row .col-count,
.problemset-row .col-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  justify-self: center;
}

.problemset-row .col-count .el-icon,
.problemset-row .col-author .el-icon {
  font-size: 1rem;
}

/* Problem Row */
.problem-row {
  grid-template-columns: 100px 2fr 120px;
}

.problem-row .col-id {
  justify-self: start;
}

.problem-row .col-title {
  justify-self: start;
}

.problem-row .col-stats {
  text-align: right;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  justify-self: end;
}

/* Record Row */
.record-row {
  grid-template-columns: 100px 2fr 120px 120px;
}

.record-row .col-id {
  justify-self: start;
}

.record-row .col-problem {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  justify-self: start;
}

.record-row .col-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  justify-self: start;
}

.record-row .col-user .el-icon {
  font-size: 1rem;
}

.record-row .col-status {
  justify-self: center;
}

/* Discussion Row */
.discussion-row {
  grid-template-columns: 2fr 100px 120px 150px;
}

.discussion-row .col-title {
  justify-self: start;
}

.discussion-row .col-category {
  justify-self: center;
}

.discussion-row .col-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  justify-self: start;
}

.discussion-row .col-author .el-icon {
  font-size: 1rem;
}

.discussion-row .col-stats {
  text-align: right;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  justify-self: end;
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
  border-bottom: 2px solid var(--color-border);
  background: linear-gradient(to right, rgba(37, 99, 235, 0.02), transparent);
}

:deep(.el-card__body) {
  padding: var(--spacing-lg) var(--spacing-xl);
}

/* Empty State */
:deep(.el-empty) {
  padding: var(--spacing-2xl) 0;
}

:deep(.el-empty__description) {
  font-size: 0.9375rem;
  color: var(--color-text-tertiary);
}

/* Responsive */
@media (max-width: 1200px) {
  .contest-row,
  .problemset-row,
  .problem-row,
  .record-row,
  .discussion-row {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
  }
  
  .table-header {
    display: none;
  }
  
  .col-id,
  .col-title,
  .col-time,
  .col-status,
  .col-count,
  .col-author,
  .col-stats,
  .col-problem,
  .col-user,
  .col-memory,
  .col-category {
    justify-self: start !important;
    text-align: left !important;
  }
}

@media (max-width: 768px) {
  .home {
    padding: 0;
  }
  
  :deep(.el-card__body) {
    padding: var(--spacing-md);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
    font-size: 1.25rem;
  }
  
  .section-title {
    font-size: 1.125rem;
  }
  
  .section-subtitle {
    font-size: 0.75rem;
  }
}
</style>
