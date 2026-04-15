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
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Element Plus 卡片样式 */
:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

:deep(.el-card:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 快捷操作样式 */
.action-card .quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  height: 48px;
  font-size: 15px;
  border-radius: 6px;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.action-btn span {
  margin-left: 8px;
}

/* 最新提交样式 */
.record-card .record-list {
  max-height: 300px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  margin-bottom: 8px;
  background-color: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.record-item:hover {
  background-color: #ecf5ff;
  transform: translateX(5px);
}

.record-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-id {
  font-weight: 600;
  color: #409EFF;
  font-size: 14px;
}

.record-problem {
  color: #606266;
  font-size: 14px;
}

/* 系统公告样式 */
.notice-card :deep(.el-timeline) {
  padding-top: 10px;
}

.notice-card :deep(.el-timeline-item__timestamp) {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.notice-card :deep(.el-timeline-item__content) {
  margin-top: 8px;
}

.notice-item {
  border: none;
  box-shadow: none;
  background-color: transparent;
}

.notice-item h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #303133;
  font-weight: 600;
}

.notice-item p {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .el-col {
    margin-bottom: 20px;
  }
}
</style>
