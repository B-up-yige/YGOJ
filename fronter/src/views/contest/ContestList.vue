<template>
  <div class="contest-list">
    <div class="header">
      <h2>比赛列表</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建比赛
      </el-button>
    </div>

    <el-table :data="contests" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="比赛标题" />
      <el-table-column label="作者" width="120">
        <template #default="scope">
          {{ scope.row.author?.nickname || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="时间" width="350">
        <template #default="scope">
          <div>{{ formatDateTime(scope.row.startTime) }} - {{ formatDateTime(scope.row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="我的进度" width="150">
        <template #default="scope">
          <div v-if="userId && contestProgress[scope.row.id]" class="progress-info">
            <el-progress 
              :percentage="calculateProgress(contestProgress[scope.row.id])" 
              :color="getProgressColor(calculateProgress(contestProgress[scope.row.id]))"
              :stroke-width="18"
              :show-text="true"
            >
              <template #default="{ percentage }">
                <span class="progress-text">{{ getProgressText(contestProgress[scope.row.id]) }}</span>
              </template>
            </el-progress>
          </div>
          <span v-else-if="!userId" class="no-data">-</span>
          <span v-else class="no-data">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <div class="action-buttons">
            <el-button size="small" @click="handleView(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </div>
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
import { getContestList, delContest, getUserContestProgress, getContestProblems } from '@/api/contest'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const contests = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userId = ref(null)
const contestProgress = ref({}) // 存储每个比赛的过题情况: { contestId: { problemId: status } }

// 加载比赛列表
const loadContests = async () => {
  loading.value = true
  try {
    const res = await getContestList(currentPage.value, pageSize.value)
    if (res.data) {
      if (Array.isArray(res.data)) {
        contests.value = res.data
        total.value = res.data.length
      } else if (res.data.records || res.data.list) {
        contests.value = res.data.records || res.data.list
        total.value = res.data.total || 0
      }
      
      // 加载用户过题情况
      if (userId.value) {
        await loadAllContestProgress()
      }
    }
  } catch (error) {
    console.error('加载比赛列表失败:', error)
    ElMessage.error('加载比赛列表失败')
  } finally {
    loading.value = false
  }
}

// 加载所有比赛的过题情况
const loadAllContestProgress = async () => {
  try {
    const progressMap = {}
    for (const contest of contests.value) {
      try {
        // 获取比赛题目列表
        const problemsRes = await getContestProblems(contest.id)
        const problems = problemsRes.data || []
        const totalProblems = problems.length
        
        // 获取用户过题情况
        const progressRes = await getUserContestProgress(userId.value, contest.id)
        const progress = progressRes.data || {}
        
        // 计算AC的题目数量
        let acCount = 0
        for (const problem of problems) {
          if (progress[problem.problemId] === 'AC') {
            acCount++
          }
        }
        
        progressMap[contest.id] = {
          total: totalProblems,
          ac: acCount,
          details: progress
        }
      } catch (error) {
        console.error(`加载比赛 ${contest.id} 的过题情况失败:`, error)
        progressMap[contest.id] = { total: 0, ac: 0, details: {} }
      }
    }
    contestProgress.value = progressMap
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

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadContests()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadContests()
}

// 格式化日期时间
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

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    UPCOMING: 'info',
    RUNNING: 'success',
    ENDED: 'warning'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    UPCOMING: '未开始',
    RUNNING: '进行中',
    ENDED: '已结束'
  }
  return texts[status] || status
}

const handleCreate = () => {
  router.push('/contest/create')
}

const handleView = (id) => {
  router.push(`/contest/${id}`)
}

const handleEdit = (id) => {
  router.push(`/contest/edit/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个比赛吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delContest(id)
    ElMessage.success('删除成功')
    loadContests()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除比赛失败:', error)
      ElMessage.error('删除比赛失败')
    }
  }
}

onMounted(() => {
  // 获取当前用户ID
  const storedUserId = localStorage.getItem('userId')
  if (storedUserId) {
    userId.value = parseInt(storedUserId)
  }
  loadContests()
})
</script>

<style scoped>
.contest-list {
  padding: 24px;
  background: var(--color-surface);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
  border-top: 1px solid #e4e7ed;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.no-data {
  color: #909399;
  font-size: 14px;
}

/* Element Plus 表格样式优化 */
:deep(.el-table) {
  --el-table-border-color: var(--color-border);
  --el-table-text-color: var(--color-text-secondary);
  --el-table-header-text-color: var(--color-text-primary);
  --el-table-row-hover-bg-color: rgba(37, 99, 235, 0.04);
  border-radius: var(--radius-md);
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: rgba(0, 0, 0, 0.02);
  color: var(--color-text-primary);
  font-weight: 600;
  padding: 14px 0;
}

:deep(.el-table td) {
  padding: 14px 0;
  transition: all 0.3s ease;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: rgba(37, 99, 235, 0.04) !important;
}

/* 进度条样式 */
:deep(.el-progress-bar__outer) {
  background-color: #ebeef5;
  border-radius: 10px;
}

:deep(.el-progress-bar__inner) {
  border-radius: 10px;
  transition: all 0.6s ease;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
  align-items: center;
}

:deep(.el-button--small) {
  padding: 8px 16px;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

:deep(.el-button--small:not(.el-button--primary):not(.el-button--danger)) {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text-primary);
}

:deep(.el-button--small:not(.el-button--primary):not(.el-button--danger):hover) {
  background-color: rgba(37, 99, 235, 0.04);
  border-color: var(--color-primary-light);
  color: var(--color-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

:deep(.el-button--primary.el-button--small) {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;
  color: white;
  font-weight: 600;
}

:deep(.el-button--primary.el-button--small:hover) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

:deep(.el-button--danger.el-button--small) {
  background: linear-gradient(135deg, var(--color-danger) 0%, #dc2626 100%);
  border: none;
  color: white;
  font-weight: 600;
}

:deep(.el-button--danger.el-button--small:hover) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* 分页样式 */
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .el-pager li) {
  background-color: #f4f4f5;
  color: #606266;
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background-color: #409EFF;
  color: #fff;
}

:deep(.el-pagination.is-background .btn-next):not([disabled]):hover,
:deep(.el-pagination.is-background .btn-prev):not([disabled]):hover,
:deep(.el-pagination.is-background .el-pager li:not(.disabled):hover) {
  color: #409EFF;
  background-color: #ecf5ff;
}

/* 按钮样式 */
:deep(.el-button--primary) {
  background-color: #409EFF;
  border-color: #409EFF;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
</style>
