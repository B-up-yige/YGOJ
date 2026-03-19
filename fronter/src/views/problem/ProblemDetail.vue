<template>
  <div class="problem-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>{{ problem.title }}</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目 ID">{{ problem.id }}</el-descriptions-item>
        <el-descriptions-item label="作者 ID">{{ problem.authorId }}</el-descriptions-item>
        <el-descriptions-item label="时间限制">{{ problem.timeLimit }} ms</el-descriptions-item>
        <el-descriptions-item label="内存限制">{{ problem.memoryLimit }} MB</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <div class="section">
        <h3>题目描述</h3>
        <p>{{ problem.description || '暂无描述' }}</p>
      </div>

      <div class="actions">
        <el-button type="primary" @click="handleSubmit">提交代码</el-button>
        <el-button type="success" @click="viewRecords">查看记录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProblemInfo } from '@/api/problem'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const problem = ref({
  id: route.params.id,
  title: '',
  description: '',
  authorId: null,
  timeLimit: 0,
  memoryLimit: 0
})

const loadProblem = async () => {
  loading.value = true
  try {
    const res = await getProblemInfo(route.params.id)
    problem.value = res.data
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleSubmit = () => {
  // TODO: 实现提交代码功能
  console.log('提交代码')
}

const viewRecords = () => {
  router.push('/records')
}

onMounted(() => {
  loadProblem()
})
</script>

<style scoped>
.problem-detail {
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

.section {
  margin: 20px 0;
}

.section h3 {
  margin-bottom: 10px;
  color: #333;
}

.actions {
  margin-top: 30px;
  text-align: center;
}
</style>
