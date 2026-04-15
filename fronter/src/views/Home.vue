<template>
  <div class="home">
    <el-row :gutter="20">
      <!-- 快捷操作 -->
      <el-col :span="8">
        <el-card shadow="always" class="action-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Operation /></el-icon> 快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="goToProblems" class="action-btn">
              <el-icon><Document /></el-icon>
              <span>浏览题目</span>
            </el-button>
            <el-button type="success" @click="goToRecords" class="action-btn">
              <el-icon><List /></el-icon>
              <span>查看提交</span>
            </el-button>
            <el-button type="warning" @click="createProblem" v-if="isAdmin" class="action-btn">
              <el-icon><Plus /></el-icon>
              <span>创建题目</span>
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 最新提交 -->
      <el-col :span="8">
        <el-card shadow="always" class="record-card">
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
              <el-tag :type="getStatusType(record.status)" size="small" effect="dark">
                {{ getStatusText(record.status) }}
              </el-tag>
            </div>
            <el-empty v-if="recentRecords.length === 0" description="暂无提交记录" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 系统公告 -->
      <el-col :span="8">
        <el-card shadow="always" class="notice-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 系统公告</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="2026-03-20" placement="top" color="#409EFF">
              <el-card class="notice-item">
                <h4>🎉 系统上线</h4>
                <p>YGOJ 在线判题系统正式上线，欢迎使用！</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item timestamp="即将发布" placement="top" color="#E6A23C">
              <el-card class="notice-item">
                <h4>📢 更多功能</h4>
                <p>更多实用功能即将上线，敬请期待...</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
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

const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => {
  // 这里可以根据用户角色判断，暂时返回 false
  return false
})

const recentRecords = ref([])

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

const goToRecords = () => {
  router.push('/records')
}

const createProblem = () => {
  router.push('/problem/create')
}

const viewRecord = (id) => {
  router.push(`/record/${id}`)
}

const loadRecentRecords = async () => {
  try {
    const res = await getRecordList(1, 10) // 获取第一页，10 条记录
    recentRecords.value = res.data?.records || res.data?.list || res.data || []
  } catch (error) {
    console.error('加载最新记录失败:', error)
  }
}

onMounted(() => {
  loadRecentRecords()
})
</script>

<style scoped>
.home {
  max-width: 1280px;
  margin: 0 auto;
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
  gap: var(--spacing-md);
}

.action-btn {
  height: 56px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: var(--spacing-sm);
  padding: 0 var(--spacing-lg);
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
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
  font-size: 1.25rem;
}

/* Record List */
.record-card .record-list {
  max-height: 320px;
  overflow-y: auto;
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
