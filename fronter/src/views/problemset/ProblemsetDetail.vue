<template>
  <div class="problemset-detail" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ problemset.title }}</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="创建者ID">{{ problemset.authorId }}</el-descriptions-item>
        <el-descriptions-item label="公开状态">
          <el-tag :type="problemset.isPublic ? 'success' : 'info'">
            {{ problemset.isPublic ? '公开' : '私有' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="题集描述" :span="2">
          <div style="white-space: pre-wrap;">{{ problemset.description || '暂无描述' }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <div class="problems-section" style="margin-top: 30px;">
        <h3>题集题目</h3>
        <el-table :data="problems" style="width: 100%">
          <el-table-column prop="problemId" label="题目ID" width="120" />
          <el-table-column label="添加时间">
            <template #default="scope">
              {{ formatDateTime(scope.row.addedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button link type="primary" @click="viewProblem(scope.row.problemId)">
                查看题目
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProblemsetInfo, getProblemsetProblems } from '@/api/problemset'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const problemset = ref({})
const problems = ref([])

const loadProblemset = async () => {
  loading.value = true
  try {
    const res = await getProblemsetInfo(route.params.id)
    problemset.value = res.data
    
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    console.error('加载题集详情失败:', error)
    ElMessage.error('加载题集详情失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

const viewProblem = (problemId) => {
  router.push(`/problem/${problemId}`)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadProblemset()
})
</script>

<style scoped>
.problemset-detail {
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

.problems-section h3 {
  margin-bottom: 15px;
}
</style>
