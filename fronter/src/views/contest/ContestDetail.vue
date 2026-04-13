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

      <!-- 比赛时间提示 -->
      <el-alert
        v-if="!canSubmit"
        :title="timeAlertMessage"
        :type="alertType"
        show-icon
        style="margin-top: 20px;"
      />

      <div class="problems-section" style="margin-top: 30px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
          <h3 style="margin: 0;">比赛题目</h3>
          <el-button type="primary" @click="showAddProblemDialog">添加题目</el-button>
        </div>
        <el-table :data="problems" style="width: 100%">
          <el-table-column prop="problemLabel" label="题号" width="100" />
          <el-table-column prop="problemId" label="题目ID" width="120" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button 
                link 
                type="primary" 
                @click="viewProblem(scope.row.problemId, scope.row.problemLabel)"
                :disabled="!canSubmit"
              >
                {{ canSubmit ? '做题' : (now < new Date(contest.startTime) ? '未开始' : '已结束') }}
              </el-button>
              <el-button link type="danger" @click="handleDeleteProblem(scope.row.problemId)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 添加题目对话框 -->
    <el-dialog v-model="addProblemDialogVisible" title="添加题目" width="500px">
      <el-form :model="addProblemForm" label-width="100px">
        <el-form-item label="题号" required>
          <el-input v-model="addProblemForm.problemLabel" placeholder="请输入题号（如：A、B、C）" />
        </el-form-item>
        <el-form-item label="题目ID" required>
          <el-input-number v-model="addProblemForm.problemId" :min="1" placeholder="请输入题目ID" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addProblemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddProblem" :loading="adding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContestInfo, getContestProblems, addContestProblem, delContestProblem } from '@/api/contest'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const contest = ref({})
const problems = ref([])
const addProblemDialogVisible = ref(false)
const addProblemForm = ref({
  contestId: null,
  problemId: null,
  problemLabel: ''
})
const adding = ref(false)
const canSubmit = ref(true)
const timeAlertMessage = ref('')
const alertType = ref('info')
const now = ref(new Date())

const loadContest = async () => {
  loading.value = true
  try {
    const res = await getContestInfo(route.params.id)
    contest.value = res.data
    
    // 检查比赛时间
    checkContestTime()
    
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

// 检查比赛时间
const checkContestTime = () => {
  if (!contest.value || !contest.value.startTime || !contest.value.endTime) return
  
  const currentTime = new Date()
  now.value = currentTime
  const startTime = new Date(contest.value.startTime)
  const endTime = new Date(contest.value.endTime)
  
  if (currentTime < startTime) {
    canSubmit.value = false
    timeAlertMessage.value = `比赛尚未开始，开始时间：${formatDateTime(contest.value.startTime)}`
    alertType.value = 'warning'
  } else if (currentTime > endTime) {
    canSubmit.value = false
    timeAlertMessage.value = `比赛已结束，结束时间：${formatDateTime(contest.value.endTime)}`
    alertType.value = 'error'
  } else {
    canSubmit.value = true
    timeAlertMessage.value = ''
    alertType.value = 'success'
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

const viewProblem = (problemId, problemLabel) => {
  // 跳转到比赛中的题目页面，携带比赛ID和题号
  router.push(`/contest/${route.params.id}/problem/${problemLabel}`)
}

const showAddProblemDialog = () => {
  addProblemForm.value = {
    contestId: parseInt(route.params.id),
    problemId: null,
    problemLabel: ''
  }
  addProblemDialogVisible.value = true
}

const handleAddProblem = async () => {
  if (!addProblemForm.value.problemLabel || !addProblemForm.value.problemLabel.trim()) {
    ElMessage.warning('请输入题号')
    return
  }
  if (!addProblemForm.value.problemId) {
    ElMessage.warning('请输入题目ID')
    return
  }

  adding.value = true
  try {
    await addContestProblem(addProblemForm.value)
    ElMessage.success('添加题目成功')
    addProblemDialogVisible.value = false
    // 重新加载题目列表
    const problemsRes = await getContestProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    console.error('添加题目失败:', error)
    ElMessage.error('添加题目失败')
  } finally {
    adding.value = false
  }
}

const handleDeleteProblem = async (problemId) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await delContestProblem(parseInt(route.params.id), problemId)
    ElMessage.success('删除成功')
    // 重新加载题目列表
    const problemsRes = await getContestProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题目失败:', error)
      ElMessage.error('删除题目失败')
    }
  }
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
