<template>
  <div v-if="notFound" class="not-found-container">
    <NotFound />
  </div>
  <div v-else class="problemset-detail" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ problemset.title }}</h2>
          <div>
            <el-button 
              v-if="canEditOrDelete()"
              @click="editProblemset"
            >
              编辑题集
            </el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
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
          <el-button 
            v-if="canEditOrDelete()"
            type="primary" 
            @click="showAddProblemDialog"
          >
            添加题目
          </el-button>
        </div>
        <el-table :data="problems" style="width: 100%">
          <el-table-column prop="problemId" label="题目ID" width="120" />
          <el-table-column label="我的状态" width="150">
            <template #default="scope">
              <el-tag v-if="userProgress[scope.row.problemId]" :type="getStatusTagType(userProgress[scope.row.problemId])">
                {{ getStatusText(userProgress[scope.row.problemId]) }}
              </el-tag>
              <span v-else style="color: var(--color-text-tertiary);">未提交</span>
            </template>
          </el-table-column>
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
              <el-button 
                v-if="canEditOrDelete()"
                link 
                type="danger" 
                @click="handleDeleteProblem(scope.row.problemId)"
              >
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
import { getProblemsetInfo, getProblemsetProblems, addProblemsetProblem, delProblemsetProblem, getUserProblemsetProgress } from '@/api/problemset'
import { useUserStore, PERMISSIONS } from '@/stores/user'
import NotFound from '@/views/NotFound.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const notFound = ref(false)
const problemset = ref({})
const problems = ref([])
const userProgress = ref({}) // 用户过题情况
const addProblemDialogVisible = ref(false)
const addProblemForm = ref({
  problemsetId: null,
  problemId: null
})
const adding = ref(false)

const loadProblemset = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    const res = await getProblemsetInfo(route.params.id, userId)
    problemset.value = res.data
    
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
    
    // 加载用户过题情况
    await loadUserProgress()
  } catch (error) {
    console.error('加载题集详情失败:', error)
    // 检查是否是资源不存在或无权限的错误
    const errorMsg = error.message || ''
    if (errorMsg.includes('不存在') || errorMsg.includes('404') || errorMsg.includes('无权') ||
        (error.response && (error.response.status === 403 || error.response.status === 404))) {
      notFound.value = true
    } else {
      ElMessage.error('加载题集详情失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载用户过题情况
const loadUserProgress = async () => {
  const userId = userStore.userInfo?.id || localStorage.getItem('userId')
  if (!userId) return
  
  try {
    const res = await getUserProblemsetProgress(userId, route.params.id)
    if (res.data) {
      userProgress.value = res.data
    }
  } catch (error) {
    console.error('加载用户过题情况失败:', error)
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  const texts = {
    AC: '通过',
    WA: '答案错误',
    TLE: '超时',
    MLE: '超内存',
    RE: '运行错误',
    CE: '编译错误',
    waiting: '判题中'
  }
  return texts[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    AC: 'success',
    WA: 'danger',
    TLE: 'warning',
    MLE: 'warning',
    RE: 'danger',
    CE: 'info',
    waiting: ''
  }
  return types[status] || 'info'
}

const viewProblem = (problemId) => {
  router.push(`/problem/${problemId}`)
}

// 检查是否可以编辑或删除题集（创建者或管理员）
const canEditOrDelete = () => {
  const currentUserId = userStore.userInfo?.id || localStorage.getItem('userId')
  if (!currentUserId) return false
  
  // 如果是创建者，可以编辑删除
  if (problemset.value.authorId === parseInt(currentUserId)) {
    return true
  }
  
  // 如果有管理权限，也可以编辑删除
  return userStore.hasPermission(PERMISSIONS.PERM_PROBLEMSET_MANAGE)
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
    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    await addProblemsetProblem(addProblemForm.value, userId)
    ElMessage.success('添加题目成功')
    addProblemDialogVisible.value = false
    // 重新加载题目列表
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    console.error('添加题目失败:', error)
    if (error.response && error.response.status === 403) {
      ElMessage.error('无权向该题集添加题目')
    } else {
      const errorMsg = error.response?.data?.message || '添加题目失败'
      ElMessage.error(errorMsg)
    }
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

    const userId = userStore.userInfo?.id || localStorage.getItem('userId')
    await delProblemsetProblem(parseInt(route.params.id), problemId, userId)
    ElMessage.success('删除成功')
    // 重新加载题目列表
    const problemsRes = await getProblemsetProblems(route.params.id)
    if (problemsRes.data) {
      problems.value = problemsRes.data
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题目失败:', error)
      if (error.response && error.response.status === 403) {
        ElMessage.error('无权从该题集删除题目')
      } else {
        ElMessage.error('删除题目失败')
      }
    }
  }
}

const goBack = () => {
  router.back()
}

const editProblemset = () => {
  router.push(`/problemset/edit/${route.params.id}`)
}

onMounted(() => {
  loadProblemset()
})
</script>

<style scoped>
.not-found-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

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
