<template>
  <div class="user-info">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 用户基本信息 -->
      <div class="user-profile">
        <el-avatar :size="100" :icon="UserFilled" />
        <h3 class="nickname">{{ user.nickname || '暂无昵称' }}</h3>
        <p class="username">@{{ user.username }}</p>
        <div class="edit-btn-wrapper" v-if="isCurrentUser">
          <el-button type="primary" @click="showEditDialog" size="small">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>

      <!-- 详细信息 -->
      <el-descriptions title="基本信息" :column="1" border size="large">
        <el-descriptions-item label="用户 ID">
          <el-tag>{{ user.id }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">
          <span>{{ user.email || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 统计信息 -->
      <div class="statistics" style="margin-top: 30px;">
        <h3>个人统计</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="提交次数" :value="stats.totalSubmissions" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="通过题目" :value="stats.acceptedCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="通过率" :value="stats.acceptanceRate" suffix="%" :precision="2" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="排名" :value="stats.rank" />
          </el-col>
        </el-row>
        
        <!-- 详细统计 -->
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <el-tag type="danger" size="large">答案错误: {{ stats.wrongAnswerCount }}</el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag type="warning" size="large">超时: {{ stats.timeLimitExceededCount }}</el-tag>
          </el-col>
          <el-col :span="8">
            <el-tag type="warning" size="large">超内存: {{ stats.memoryLimitExceededCount }}</el-tag>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="12">
            <el-tag type="info" size="large">运行错误: {{ stats.runtimeErrorCount }}</el-tag>
          </el-col>
          <el-col :span="12">
            <el-tag type="info" size="large">编译错误: {{ stats.compilationErrorCount }}</el-tag>
          </el-col>
        </el-row>
      </div>
      
      <!-- 学习曲线图表 -->
      <div class="learning-curve" style="margin-top: 30px;">
        <h3>学习曲线（近{{ learningCurveDays }}天）</h3>
        <div ref="chartRef" style="width: 100%; height: 400px;"></div>
        <div style="text-align: center; margin-top: 15px;">
          <el-radio-group v-model="learningCurveDays" @change="loadLearningCurve">
            <el-radio-button :label="7">近7天</el-radio-button>
            <el-radio-button :label="30">近30天</el-radio-button>
            <el-radio-button :label="90">近90天</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 标签统计图表 -->
      <div class="tag-statistics" style="margin-top: 30px;">
        <h3>题目标签分析</h3>
        <div ref="tagChartRef" style="width: 100%; height: 500px;"></div>
      </div>
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
import { UserFilled, Edit } from '@element-plus/icons-vue'
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
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.user-profile {
  text-align: center;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  margin-bottom: 30px;
  color: white;
  position: relative;
}

.edit-btn-wrapper {
  margin-top: 15px;
}

.user-profile .nickname {
  font-size: 24px;
  font-weight: bold;
  margin: 15px 0 5px 0;
}

.user-profile .username {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.statistics h3 {
  margin-bottom: 20px;
  color: #333;
}

.el-statistic {
  text-align: center;
}
</style>
