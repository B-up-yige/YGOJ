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
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
  color: #fff;
  text-shadow: 0 0 8px rgba(102, 126, 234, 0.4);
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Element Plus 卡片科技风格 */
:deep(.el-card) {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.4);
  border-color: rgba(102, 126, 234, 0.3);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 快捷操作样式 */
.action-card .quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  height: 50px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
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
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.record-item:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.4);
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.record-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-id {
  font-weight: bold;
  color: #667eea;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(102, 126, 234, 0.5);
}

.record-problem {
  color: #e0e0e0;
  font-size: 14px;
}

/* 系统公告样式 */
.notice-card :deep(.el-timeline) {
  padding-top: 10px;
}

.notice-card :deep(.el-timeline-item__timestamp) {
  font-size: 13px;
  color: #b0b0b0;
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
  color: #fff;
  font-weight: 600;
  text-shadow: 0 0 5px rgba(102, 126, 234, 0.3);
}

.notice-item p {
  margin: 0;
  font-size: 13px;
  color: #d0d0d0;
  line-height: 1.6;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .el-col {
    margin-bottom: 20px;
  }
}
</style>
