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
        <el-descriptions-item label="提交时间" v-if="record.submitTime">{{ record.submitTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="code-section" v-if="record.code">
        <el-divider />
        <h3>提交的代码</h3>
        <pre><code>{{ record.code }}</code></pre>
      </div>

      <div class="actions" style="margin-top: 30px; text-align: center;">
        <el-button type="primary" @click="viewProblem">查看题目</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRecordInfo } from '@/api/record'

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

const loadRecord = async () => {
  loading.value = true
  try {
    const res = await getRecordInfo(route.params.id)
    record.value = res.data
  } catch (error) {
    console.error('加载记录详情失败:', error)
  } finally {
    loading.value = false
  }
}

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

const goBack = () => {
  router.back()
}

const viewProblem = () => {
  if (record.value.problemId) {
    router.push(`/problem/${record.value.problemId}`)
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
</style>
