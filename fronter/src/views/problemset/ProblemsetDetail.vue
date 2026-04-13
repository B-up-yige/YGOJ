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
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
          <h3 style="margin: 0;">题集题目</h3>
          <el-button type="primary" @click="showAddProblemDialog">添加题目</el-button>
        </div>
        <el-table :data="problems" style="width: 100%">
          <el-table-column prop="problemId" label="题目ID" width="120" />
          <el-table-column label="添加时间">
            <template #default="scope">
              {{ formatDateTime(scope.row.addedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button link type="primary" @click="viewProblem(scope.row.problemId)">
                查看题目
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
import { getProblemsetInfo, getProblemsetProblems, addProblemsetProblem, delProblemsetProblem } from '@/api/problemset'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const problemset = ref({})
const problems = ref([])
const addProblemDialogVisible = ref(false)
const addProblemForm = ref({
  problemsetId: null,
  problemId: null
})
const adding = ref(false)

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

const showAddProblemDialog = () => {
  addProblemForm.value = {
    problemsetId: parseInt(route.params.id),
    problemId: null
  }
  addProblemDialogVisible.value = true
}

const handleAddProblem = async () => {
  if (!addProblemForm.value.problemId) {
    ElMessage.warning('请输入题目ID')
    return
  }

  adding.value = true
  try {
    await addProblemsetProblem(addProblemForm.value)
    ElMessage.success('添加题目成功')
    addProblemDialogVisible.value = false
    // 重新加载题目列表
    const problemsRes = await getProblemsetProblems(route.params.id)
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

    await delProblemsetProblem(parseInt(route.params.id), problemId)
    ElMessage.success('删除成功')
    // 重新加载题目列表
    const problemsRes = await getProblemsetProblems(route.params.id)
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
