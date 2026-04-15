<template>
  <div class="user-info">
    <!-- 顶部个人信息卡片 -->
    <el-card class="profile-card" v-loading="loading" shadow="hover">
      <div class="profile-header">
        <div class="profile-left">
          <el-avatar :size="120" :icon="UserFilled" class="avatar" />
          <div class="profile-info">
            <h2 class="nickname">{{ user.nickname || '暂无昵称' }}</h2>
            <p class="username">@{{ user.username }}</p>
            <div class="profile-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                ID: {{ user.id }}
              </span>
              <span class="meta-item" v-if="user.email">
                <el-icon><Message /></el-icon>
                {{ user.email }}
              </span>
            </div>
          </div>
        </div>
        <div class="profile-right" v-if="isCurrentUser">
          <el-button type="primary" @click="showEditDialog" size="large">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon submissions">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalSubmissions }}</div>
              <div class="stat-label">总提交</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon accepted">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.acceptedCount }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon rate">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.acceptanceRate.toFixed(2) }}%</div>
              <div class="stat-label">通过率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon rank">
              <el-icon><Medal /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">#{{ stats.rank }}</div>
              <div class="stat-label">排名</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计和图表 -->
    <el-row :gutter="20">
      <!-- 左侧：错误分布 -->
      <el-col :xs="24" :lg="10">
        <el-card class="detail-card" shadow="hover">
          <template #header>
            <div class="card-title">
              <el-icon><PieChart /></el-icon>
              <span>提交结果分布</span>
            </div>
          </template>
          <div class="error-stats">
            <div class="error-item">
              <div class="error-label">答案错误</div>
              <el-progress 
                :percentage="getErrorPercentage(stats.wrongAnswerCount)" 
                :color="#f56c6c"
                :stroke-width="20"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ stats.wrongAnswerCount }}</span>
                </template>
              </el-progress>
            </div>
            <div class="error-item">
              <div class="error-label">超时</div>
              <el-progress 
                :percentage="getErrorPercentage(stats.timeLimitExceededCount)" 
                :color="#e6a23c"
                :stroke-width="20"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ stats.timeLimitExceededCount }}</span>
                </template>
              </el-progress>
            </div>
            <div class="error-item">
              <div class="error-label">超内存</div>
              <el-progress 
                :percentage="getErrorPercentage(stats.memoryLimitExceededCount)" 
                :color="#e6a23c"
                :stroke-width="20"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ stats.memoryLimitExceededCount }}</span>
                </template>
              </el-progress>
            </div>
            <div class="error-item">
              <div class="error-label">运行错误</div>
              <el-progress 
                :percentage="getErrorPercentage(stats.runtimeErrorCount)" 
                :color="#909399"
                :stroke-width="20"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ stats.runtimeErrorCount }}</span>
                </template>
              </el-progress>
            </div>
            <div class="error-item">
              <div class="error-label">编译错误</div>
              <el-progress 
                :percentage="getErrorPercentage(stats.compilationErrorCount)" 
                :color="#909399"
                :stroke-width="20"
              >
                <template #default="{ percentage }">
                  <span class="progress-text">{{ stats.compilationErrorCount }}</span>
                </template>
              </el-progress>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：学习曲线 -->
      <el-col :xs="24" :lg="14">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-title">
              <el-icon><TrendCharts /></el-icon>
              <span>学习曲线</span>
              <div class="chart-controls">
                <el-radio-group v-model="learningCurveDays" @change="loadLearningCurve" size="small">
                  <el-radio-button :label="7">7天</el-radio-button>
                  <el-radio-button :label="30">30天</el-radio-button>
                  <el-radio-button :label="90">90天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 标签分析 -->
    <el-card class="tag-card" shadow="hover">
      <template #header>
        <div class="card-title">
          <el-icon><CollectionTag /></el-icon>
          <span>题目标签分析</span>
        </div>
      </template>
      <div ref="tagChartRef" class="tag-chart-container"></div>
    </el-card>

    <!-- 编辑用户信息对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称（3-20 位）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserFilled, Edit, User, Message, Document, CircleCheck, TrendCharts, Medal, PieChart, CollectionTag } from '@element-plus/icons-vue'
import { getUserinfo } from '@/api/user'
import { getUserStatistics, getUserLearningCurve, getUserStatsByTag } from '@/api/record'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const updating = ref(false)
const chartRef = ref(null)
const tagChartRef = ref(null)
const learningCurveDays = ref(30)
let chartInstance = null
let tagChartInstance = null

const isCurrentUser = computed(() => {
  return userStore.userInfo && userStore.userInfo.id.toString() === route.params.id
})

const user = ref({
  id: route.params.id,
  username: '',
  nickname: '',
  email: ''
})

// 编辑表单数据
const editForm = ref({
  nickname: '',
  email: ''
})

// 表单验证规则
const validateNickname = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入昵称'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('昵称长度必须在 3-20 位之间'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const editRules = {
  nickname: [
    { validator: validateNickname, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

// 统计数据
const stats = ref({
  totalSubmissions: 0,
  acceptedCount: 0,
  wrongAnswerCount: 0,
  timeLimitExceededCount: 0,
  memoryLimitExceededCount: 0,
  runtimeErrorCount: 0,
  compilationErrorCount: 0,
  acceptanceRate: 0,
  rank: 0
})

const loadUser = async () => {
  loading.value = true
  try {
    const res = await getUserinfo(route.params.id)
    user.value = res.data
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await getUserStatistics(route.params.id)
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadLearningCurve = async () => {
  try {
    const res = await getUserLearningCurve(route.params.id, learningCurveDays.value)
    if (res.data && res.data.length > 0) {
      renderChart(res.data)
    } else {
      // 没有数据时显示空图表
      renderEmptyChart()
    }
  } catch (error) {
    console.error('加载学习曲线数据失败:', error)
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  // 处理数据
  const dates = data.map(item => item.stat_date)
  const submissions = data.map(item => item.submissions)
  const accepted = data.map(item => item.accepted)
  
  const option = {
    title: {
      text: '提交趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['提交次数', '通过次数'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '次数'
    },
    series: [
      {
        name: '提交次数',
        type: 'line',
        smooth: true,
        data: submissions,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      },
      {
        name: '通过次数',
        type: 'line',
        smooth: true,
        data: accepted,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const renderEmptyChart = () => {
  if (!chartRef.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const option = {
    title: {
      text: '暂无数据',
      left: 'center',
      top: 'middle',
      textStyle: {
        color: '#999',
        fontSize: 16
      }
    }
  }
  
  chartInstance.setOption(option)
}

const loadTagStatistics = async () => {
  try {
    const res = await getUserStatsByTag(route.params.id)
    if (res.data && res.data.length > 0) {
      renderTagChart(res.data)
    } else {
      renderEmptyTagChart()
    }
  } catch (error) {
    console.error('加载标签统计数据失败:', error)
  }
}

const renderTagChart = (data) => {
  if (!tagChartRef.value) return
  
  if (!tagChartInstance) {
    tagChartInstance = echarts.init(tagChartRef.value)
  }
  
  // 处理数据
  const tags = data.map(item => item.tag)
  const submissions = data.map(item => item.total_submissions)
  const accepted = data.map(item => item.accepted_count)
  const rates = data.map(item => item.acceptance_rate)
  
  const option = {
    title: {
      text: '各标签提交情况',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += param.marker + param.seriesName + ': ' + param.value + '<br/>'
        })
        // 添加通过率
        const index = params[0].dataIndex
        result += '通过率: ' + rates[index] + '%'
        return result
      }
    },
    legend: {
      data: ['提交次数', '通过次数'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '次数'
    },
    yAxis: {
      type: 'category',
      data: tags,
      axisLabel: {
        interval: 0,
        formatter: function(value) {
          return value.length > 8 ? value.substring(0, 8) + '...' : value
        }
      }
    },
    series: [
      {
        name: '提交次数',
        type: 'bar',
        data: submissions,
        itemStyle: {
          color: '#409EFF'
        },
        label: {
          show: true,
          position: 'right'
        }
      },
      {
        name: '通过次数',
        type: 'bar',
        data: accepted,
        itemStyle: {
          color: '#67C23A'
        },
        label: {
          show: true,
          position: 'right'
        }
      }
    ]
  }
  
  tagChartInstance.setOption(option)
}

const renderEmptyTagChart = () => {
  if (!tagChartRef.value) return
  
  if (!tagChartInstance) {
    tagChartInstance = echarts.init(tagChartRef.value)
  }
  
  const option = {
    title: {
      text: '暂无标签数据',
      left: 'center',
      top: 'middle',
      textStyle: {
        color: '#999',
        fontSize: 16
      }
    }
  }
  
  tagChartInstance.setOption(option)
}

// 计算错误百分比
const getErrorPercentage = (count) => {
  const total = stats.value.totalSubmissions
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

// 显示编辑对话框
const showEditDialog = () => {
  ElMessage.info('编辑功能暂未实现')
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await loadUser()
  await loadStatistics()
  await loadLearningCurve()
  await loadTagStatistics()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
    if (tagChartInstance) {
      tagChartInstance.resize()
    }
  })
})
</script>

<style scoped>
.user-info {
  padding: var(--spacing-2xl);
  max-width: 1400px;
  margin: 0 auto;
}

/* 顶部个人信息卡片 */
.profile-card {
  margin-bottom: var(--spacing-xl);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.avatar {
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.nickname {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.02em;
}

.username {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
}

.profile-meta {
  display: flex;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-sm);
  font-size: 0.875rem;
  opacity: 0.85;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.profile-right .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
  transition: all var(--transition-fast);
}

.profile-right .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 统计概览卡片 */
.stats-overview {
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-md);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  color: white;
}

.stat-icon.submissions {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.accepted {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.rate {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.rank {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1;
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  font-weight: 500;
}

/* 详细卡片 */
.detail-card,
.chart-card,
.tag-card {
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-xl);
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.card-title .el-icon {
  font-size: 1.25rem;
  color: var(--color-primary);
}

.chart-controls {
  margin-left: auto;
}

/* 错误统计 */
.error-stats {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  padding: var(--spacing-md);
}

.error-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.error-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
}

.progress-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* 图表容器 */
.chart-container {
  width: 100%;
  height: 350px;
}

.tag-chart-container {
  width: 100%;
  height: 450px;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-info {
    padding: var(--spacing-md);
  }
  
  .profile-header {
    flex-direction: column;
    gap: var(--spacing-lg);
    text-align: center;
  }
  
  .profile-left {
    flex-direction: column;
  }
  
  .nickname {
    font-size: 1.5rem;
  }
  
  .profile-meta {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .tag-chart-container {
    height: 350px;
  }
}
</style>
