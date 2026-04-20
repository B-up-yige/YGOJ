<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="contest-problem-detail" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <h2>{{ problemLabel }}. {{ problem.title }}</h2>
            <p class="contest-info">比赛：{{ contestTitle }}</p>
          </div>
          <el-button @click="goBack">返回比赛</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目 ID">{{ problem.id }}</el-descriptions-item>
        <el-descriptions-item label="作者 ID">{{ problem.authorId }}</el-descriptions-item>
        <el-descriptions-item label="时间限制">{{ problem.timeLimit }} ms</el-descriptions-item>
        <el-descriptions-item label="内存限制">{{ problem.memoryLimit }} MB</el-descriptions-item>
      </el-descriptions>

      <!-- 标签区域 -->
      <div class="tags-section" v-if="tags.length > 0">
        <h3>标签</h3>
        <div class="tags-container">
          <el-tag
            v-for="tag in tags"
            :key="tag"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <el-divider />

      <div class="section">
        <h3>题目描述</h3>
        <p>{{ problem.description || '暂无描述' }}</p>
      </div>

      <div class="actions">
        <el-button 
          type="primary" 
          @click="showSubmitDialog"
          :disabled="!canSubmit"
        >
          {{ submitButtonText }}
        </el-button>
        <el-button type="success" @click="viewRecords">查看提交记录</el-button>
      </div>
      
      <!-- 比赛时间提示 -->
      <el-alert
        v-if="!canSubmit"
        :title="submitButtonText"
        :type="alertType"
        show-icon
        style="margin-top: 20px;"
      />
    </el-card>

    <!-- 提交代码对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交代码" width="60%">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="编程语言">
          <el-select v-model="submitForm.language" placeholder="请选择编程语言" style="width: 100%">
            <el-option label="Java" value="Java" />
            <el-option label="C++" value="C++" />
            <el-option label="C" value="C" />
            <el-option label="Python 3" value="Python 3" />
          </el-select>
        </el-form-item>
        <el-form-item label="代码">
          <el-input
            v-model="submitForm.code"
            type="textarea"
            :rows="15"
            placeholder="请输入代码"
            style="font-family: monospace;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCode" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProblemInfo, getProblemTags } from '@/api/problem'
import { getContestInfo } from '@/api/contest'
import { submitCode } from '@/api/record'
import { useUserStore } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const submitDialogVisible = ref(false)
const notFound = ref(false)

const contestId = route.params.contestId
const problemLabel = route.params.problemLabel
const contestTitle = ref('')
const problemId = ref(null)
const contestInfo = ref(null)
const canSubmit = ref(false)
const submitButtonText = ref('提交代码')
const alertType = ref('info')

const problem = ref({
  id: null,
  title: '',
  description: '',
  authorId: null,
  timeLimit: 0,
  memoryLimit: 0
})

const tags = ref([])

const submitForm = ref({
  language: 'Java',
  code: ''
})

// 加载比赛信息和题目映射
const loadContest = async () => {
  try {
    const res = await getContestInfo(contestId)
    contestTitle.value = res.data.title
    contestInfo.value = res.data
    
    // 检查比赛时间
    checkContestTime()
    
    // 获取比赛题目列表，找到对应problemLabel的problemId
    const problemsRes = await import('@/api/contest').then(m => m.getContestProblems(contestId))
    const problems = problemsRes.data || []
    const targetProblem = problems.find(p => p.problemLabel === problemLabel)
    
    if (!targetProblem) {
      notFound.value = true
      return
    }
    
    problemId.value = targetProblem.problemId
    await loadProblem()
  } catch (error) {
    console.error('加载比赛信息失败:', error)
    if (error.response && (error.response.status === 404 || error.response.status === 403)) {
      notFound.value = true
    } else {
      ElMessage.error('加载比赛信息失败')
    }
  }
}

// 检查比赛时间
const checkContestTime = () => {
  if (!contestInfo.value) return
  
  const now = new Date()
  const startTime = new Date(contestInfo.value.startTime)
  const endTime = new Date(contestInfo.value.endTime)
  
  if (now < startTime) {
    canSubmit.value = false
    submitButtonText.value = '比赛尚未开始'
    alertType.value = 'warning'
  } else if (now > endTime) {
    canSubmit.value = false
    submitButtonText.value = '比赛已结束'
    alertType.value = 'error'
  } else {
    canSubmit.value = true
    submitButtonText.value = '提交代码'
    alertType.value = 'success'
  }
}

const loadProblem = async () => {
  loading.value = true
  try {
    // 先检查比赛时间
    if (!canSubmit.value) {
      ElMessage.warning(submitButtonText.value)
      router.push(`/contest/${contestId}`)
      return
    }
    
    const res = await getProblemInfo(problemId.value)
    problem.value = res.data
    
    // 加载标签
    await loadTags()
  } catch (error) {
    console.error('加载题目失败:', error)
    if (error.response && (error.response.status === 404 || error.response.status === 403)) {
      notFound.value = true
    } else {
      ElMessage.error('加载题目失败')
    }
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const res = await getProblemTags(problemId.value)
    if (res.data) {
      tags.value = res.data.map(item => item.tag)
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const goBack = () => {
  router.push(`/contest/${contestId}`)
}

const showSubmitDialog = () => {
  if (!canSubmit.value) {
    ElMessage.warning(submitButtonText.value)
    return
  }
  
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  submitDialogVisible.value = true
}

const handleSubmitCode = async () => {
  if (!submitForm.value.code.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  submitting.value = true
  try {
    const data = {
      problemId: problemId.value,
      userId: userStore.userInfo?.id || localStorage.getItem('userId'),
      contestId: parseInt(contestId), // 关键：携带比赛ID
      language: submitForm.value.language,
      code: submitForm.value.code
    }
    await submitCode(data)
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    // 跳转到比赛提交记录页面
    router.push(`/contest/${contestId}/records`)
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

const viewRecords = () => {
  // 跳转到比赛的提交记录页面
  router.push(`/contest/${contestId}/records?problemId=${problemId.value}`)
}

onMounted(() => {
  loadContest()
})
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.contest-problem-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0 0 8px 0;
}

.contest-info {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.section {
  margin: 20px 0;
}

.section h3 {
  margin-bottom: 10px;
  color: #333;
}

.tags-section {
  margin: 20px 0;
}

.tags-section h3 {
  margin-bottom: 10px;
  color: #333;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.actions {
  margin-top: 30px;
  text-align: center;
}
</style>
