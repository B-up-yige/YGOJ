<template>
  <div class="record-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>提交记录详情</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录 ID">{{ record.id }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ record.userId }}</el-descriptions-item>
        <el-descriptions-item label="题目 ID">{{ record.problemId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(record.status)">
            {{ getStatusText(record.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="编程语言" v-if="record.language">{{ record.language }}</el-descriptions-item>
        <el-descriptions-item label="提交时间" v-if="record.submitTime">{{ formatTime(record.submitTime) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 编译错误信息 -->
      <div class="compile-section" v-if="record.compileStderr">
        <el-divider />
        <h3>
          <el-icon color="#f56c6c"><CircleClose /></el-icon>
          编译错误
        </h3>
        <pre class="compile-error"><code>{{ record.compileStderr }}</code></pre>
      </div>

      <!-- 编译警告信息 -->
      <div class="compile-section" v-else-if="record.compileStdout">
        <el-divider />
        <h3>
          <el-icon color="#e6a23c"><Warning /></el-icon>
          编译警告
        </h3>
        <pre class="compile-warning"><code>{{ record.compileStdout }}</code></pre>
      </div>

      <div class="code-section" v-if="record.code">
        <el-divider />
        <h3>提交的代码</h3>
        <pre><code>{{ record.code }}</code></pre>
      </div>

      <!-- 测试点详情 -->
      <div class="testcase-section" v-if="details.length > 0">
        <el-divider />
        <h3>测试点详情</h3>
        <el-table :data="details" style="width: 100%" border>
          <el-table-column prop="recordId" label="记录ID" width="100" />
          <el-table-column label="状态" width="150">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="耗时(ms)" width="120" />
          <el-table-column prop="memory" label="内存(KB)" width="120" />
        </el-table>
      </div>

      <div class="actions" style="margin-top: 30px; text-align: center;">
        <el-button type="primary" @click="viewProblem">查看题目</el-button>
        <el-button type="warning" @click="handleRejudge" v-permission="PERMISSIONS.PERM_SYSTEM_CONFIG">重测</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecordInfo, getRecordDetails, rejudge } from '@/api/record'
import { CircleClose, Warning } from '@element-plus/icons-vue'
import { PERMISSIONS } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const record = ref({
  id: route.params.id,
  userId: '',
  problemId: '',
  status: '',
  language: '',
  code: '',
  submitTime: ''
})
const details = ref([])

const loadRecord = async () => {
  loading.value = true
  try {
    const res = await getRecordInfo(route.params.id)
    record.value = res.data
    
    // 加载测试点详情
    const detailRes = await getRecordDetails(route.params.id)
    details.value = detailRes.data || []
  } catch (error) {
    console.error('加载记录详情失败:', error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'AC': 'success',
    'WA': 'danger',
    'TLE': 'warning',
    'MLE': 'warning',
    'RE': 'danger',
    'CE': 'info',
    'waiting': '',
    'Accepted': 'success',
    'Wrong Answer': 'danger',
    'Time Limit Exceeded': 'warning',
    'Memory Limit Exceeded': 'warning',
    'Runtime Error': 'danger',
    'Compilation Error': 'info'
  }
  return statusMap[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  // 处理 LocalDateTime 格式: 2024-01-01T12:00:00
  const dateStr = time.replace('T', ' ')
  return dateStr.substring(0, 19)
}

const getStatusText = (status) => {
  const textMap = {
    'AC': '通过',
    'WA': '答案错误',
    'TLE': '超时',
    'MLE': '超内存',
    'RE': '运行错误',
    'CE': '编译错误',
    'waiting': '等待中',
    'Accepted': '通过',
    'Wrong Answer': '答案错误',
    'Time Limit Exceeded': '超时',
    'Memory Limit Exceeded': '超内存',
    'Runtime Error': '运行错误',
    'Compilation Error': '编译错误'
  }
  return textMap[status] || status
}

const goBack = () => {
  router.back()
}

const viewProblem = () => {
  if (record.value.problemId) {
    router.push(`/problem/${record.value.problemId}`)
  }
}

const handleRejudge = async () => {
  try {
    await ElMessageBox.confirm('确定要重新判题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await rejudge(record.value.id)
    ElMessage.success('重测任务已提交')
    // 重新加载记录详情
    loadRecord()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重测失败:', error)
      ElMessage.error('重测失败')
    }
  }
}

onMounted(() => {
  loadRecord()
})
</script>

<style scoped>
.record-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.code-section {
  margin-top: 20px;
}

.code-section h3 {
  margin-bottom: 10px;
  color: #333;
}

.code-section pre {
  background-color: #f6f8fa;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.5;
}

.code-section code {
  color: #24292e;
}

.compile-section {
  margin-top: 20px;
}

.compile-section h3 {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.compile-error,
.compile-warning {
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.5;
}

.compile-error {
  background-color: #fef0f0;
  border-left: 4px solid #f56c6c;
}

.compile-error code {
  color: #f56c6c;
}

.compile-warning {
  background-color: #fdf6ec;
  border-left: 4px solid #e6a23c;
}

.compile-warning code {
  color: #e6a23c;
}

.testcase-section {
  margin-top: 20px;
}

.testcase-section h3 {
  margin-bottom: 10px;
  color: #333;
}
</style>
