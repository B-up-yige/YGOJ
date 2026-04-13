<template>
  <div class="problemset-list">
    <div class="header">
      <h2>题集列表</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建题集
      </el-button>
    </div>

    <el-table :data="problemsets" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="题集标题" />
      <el-table-column prop="authorId" label="作者ID" width="120" />
      <el-table-column label="公开状态" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.isPublic ? 'success' : 'info'">
            {{ scope.row.isPublic ? '公开' : '私有' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="我的进度" width="150">
        <template #default="scope">
          <div v-if="userId && problemsetProgress[scope.row.id]" class="progress-info">
            <el-progress 
              :percentage="calculateProgress(problemsetProgress[scope.row.id])" 
              :color="getProgressColor(calculateProgress(problemsetProgress[scope.row.id]))"
              :stroke-width="18"
              :show-text="true"
            >
              <template #default="{ percentage }">
                <span class="progress-text">{{ getProgressText(problemsetProgress[scope.row.id]) }}</span>
              </template>
            </el-progress>
          </div>
          <span v-else-if="!userId" class="no-data">-</span>
          <span v-else class="no-data">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row.id)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(scope.row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProblemsetList, delProblemset, getUserProblemsetProgress, getProblemsetProblems } from '@/api/problemset'

const router = useRouter()
const loading = ref(false)
const problemsets = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userId = ref(null)
const problemsetProgress = ref({}) // 存储每个题集的过题情况: { problemsetId: { problemId: status } }

const loadProblemsets = async () => {
  loading.value = true
  try {
    const res = await getProblemsetList(currentPage.value, pageSize.value)
    if (res.data) {
      if (Array.isArray(res.data)) {
        problemsets.value = res.data
        total.value = res.data.length
      } else if (res.data.records || res.data.list) {
        problemsets.value = res.data.records || res.data.list
        total.value = res.data.total || 0
      }
      
      // 加载用户过题情况
      if (userId.value) {
        await loadAllProblemsetProgress()
      }
    }
  } catch (error) {
    console.error('加载题集列表失败:', error)
    ElMessage.error('加载题集列表失败')
  } finally {
    loading.value = false
  }
}

// 加载所有题集的过题情况
const loadAllProblemsetProgress = async () => {
  try {
    const progressMap = {}
    for (const problemset of problemsets.value) {
      try {
        // 获取题集题目列表
        const problemsRes = await getProblemsetProblems(problemset.id)
        const problems = problemsRes.data || []
        const totalProblems = problems.length
        
        // 获取用户过题情况（目前后端返回空，这里先模拟）
        // TODO: 当后端实现完整的题集过题查询后，替换为实际调用
        const progressRes = await getUserProblemsetProgress(userId.value, problemset.id)
        const progress = progressRes.data || {}
        
        // 计算AC的题目数量
        let acCount = 0
        for (const problem of problems) {
          if (progress[problem.problemId] === 'AC') {
            acCount++
          }
        }
        
        progressMap[problemset.id] = {
          total: totalProblems,
          ac: acCount,
          details: progress
        }
      } catch (error) {
        console.error(`加载题集 ${problemset.id} 的过题情况失败:`, error)
        progressMap[problemset.id] = { total: 0, ac: 0, details: {} }
      }
    }
    problemsetProgress.value = progressMap
  } catch (error) {
    console.error('加载过题情况失败:', error)
  }
}

// 计算进度百分比
const calculateProgress = (progressData) => {
  if (!progressData || progressData.total === 0) return 0
  return Math.round((progressData.ac / progressData.total) * 100)
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage === 100) return '#67c23a'
  if (percentage >= 50) return '#e6a23c'
  return '#409eff'
}

// 获取进度文本
const getProgressText = (progressData) => {
  if (!progressData) return '0/0'
  return `${progressData.ac}/${progressData.total}`
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProblemsets()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadProblemsets()
}

const handleCreate = () => {
  router.push('/problemset/create')
}

const handleView = (id) => {
  router.push(`/problemset/${id}`)
}

const handleEdit = (id) => {
  router.push(`/problemset/edit/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个题集吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delProblemset(id)
    ElMessage.success('删除成功')
    loadProblemsets()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题集失败:', error)
      ElMessage.error('删除题集失败')
    }
  }
}

onMounted(() => {
  // 获取当前用户ID
  const storedUserId = localStorage.getItem('userId')
  if (storedUserId) {
    userId.value = parseInt(storedUserId)
  }
  loadProblemsets()
})
</script>

<style scoped>
.problemset-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.progress-info {
  display: flex;
  align-items: center;
}

.progress-text {
  font-size: 12px;
  font-weight: bold;
}

.no-data {
  color: #909399;
  font-size: 14px;
}
</style>
