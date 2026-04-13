<template>
  <div class="contest-detail" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ contest.title }}</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="比赛状态">
          <el-tag :type="getStatusType(contest.status)">
            {{ getStatusText(contest.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建者ID">{{ contest.authorId }}</el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">
          {{ formatDateTime(contest.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">
          {{ formatDateTime(contest.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="比赛描述" :span="2">
          <div style="white-space: pre-wrap;">{{ contest.description || '暂无描述' }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <div class="problems-section" style="margin-top: 30px;">
        <h3>比赛题目</h3>
        <el-table :data="problems" style="width: 100%">
          <el-table-column prop="problemLabel" label="题号" width="100" />
          <el-table-column prop="problemId" label="题目ID" width="120" />
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
import { getContestInfo, getContestProblems } from '@/api/contest'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const contest = ref({})
const problems = ref([])

const loadContest = async () => {
  loading.value = true
  try {
    const res = await getContestInfo(route.params.id)
    contest.value = res.data
    
    // 加载比赛题目
    const problemsRes = await getContestProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    console.error('加载比赛详情失败:', error)
    ElMessage.error('加载比赛详情失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusType = (status) => {
  const types = {
    UPCOMING: 'info',
    RUNNING: 'success',
    ENDED: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    UPCOMING: '未开始',
    RUNNING: '进行中',
    ENDED: '已结束'
  }
  return texts[status] || status
}

const viewProblem = (problemId) => {
  router.push(`/problem/${problemId}`)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadContest()
})
</script>

<style scoped>
.contest-detail {
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
