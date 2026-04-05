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
        <el-button type="primary" @click="showSubmitDialog">提交代码</el-button>
        <el-button type="success" @click="viewRecords">查看记录</el-button>
      </div>
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
import { submitCode } from '@/api/record'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const submitDialogVisible = ref(false)

const problem = ref({
  id: route.params.id,
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

const loadProblem = async () => {
  loading.value = true
  try {
    const res = await getProblemInfo(route.params.id)
    problem.value = res.data
    
    // 加载标签
    await loadTags()
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const res = await getProblemTags(route.params.id)
    if (res.data) {
      // 后端返回的是 Tag 对象数组，需要提取 tag 字段
      tags.value = res.data.map(item => item.tag)
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const goBack = () => {
  router.back()
}

const showSubmitDialog = () => {
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
      problemId: route.params.id,
      userId: userStore.userInfo?.id || localStorage.getItem('userId'),
      language: submitForm.value.language,
      code: submitForm.value.code
    }
    await submitCode(data)
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    // 跳转到记录页面查看结果
    router.push('/records')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
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
