<template>
  <div class="problem-list">
    <div class="header">
      <h2>题目列表</h2>
      <el-button type="primary" @click="handleCreate" v-permission="{ type: 'or', values: [PERMISSIONS.PERM_PROBLEM_CREATE, PERMISSIONS.PERM_PROBLEM_MANAGE_ALL] }">
        <el-icon><Plus /></el-icon>
        创建题目
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input
        v-model="searchTitle"
        placeholder="搜索题目标题"
        clearable
        style="width: 300px; margin-right: 10px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-input
        v-model="searchTag"
        placeholder="按标签筛选"
        clearable
        style="width: 200px; margin-right: 10px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Filter /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="problems" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="作者" width="120">
        <template #default="scope">
          {{ scope.row.author?.nickname || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="timeLimit" label="时间限制 (ms)" width="120" />
      <el-table-column prop="memoryLimit" label="内存限制 (MB)" width="120" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <div class="action-buttons">
            <el-button size="small" @click="handleView(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(scope.row.id)" v-permission="{ type: 'or', values: [PERMISSIONS.PERM_PROBLEM_MANAGE_OWN, PERMISSIONS.PERM_PROBLEM_MANAGE_ALL] }">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)" v-permission="{ type: 'or', values: [PERMISSIONS.PERM_PROBLEM_MANAGE_OWN, PERMISSIONS.PERM_PROBLEM_MANAGE_ALL] }">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination" style="margin-top: 20px; text-align: right;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
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
import { Search, Filter, Plus } from '@element-plus/icons-vue'
import { getProblemList, delProblem } from '@/api/problem'
import { PERMISSIONS } from '@/stores/user'

const router = useRouter()
const loading = ref(false)
const problems = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')
const searchTag = ref('')

// 加载题目列表
const loadProblems = async () => {
  loading.value = true
  try {
    const res = await getProblemList(currentPage.value, pageSize.value, searchTitle.value, searchTag.value)
    // 后端返回的数据格式：{ data: [...] }
    if (res.data) {
      // 如果返回的是数组
      if (Array.isArray(res.data)) {
        problems.value = res.data
        total.value = res.data.length
      } else if (res.data.records || res.data.list) {
        // 如果是分页对象
        problems.value = res.data.records || res.data.list
        total.value = res.data.total || 0
      } else {
        // 其他情况
        problems.value = []
        total.value = 0
      }
    }
  } catch (error) {
    console.error('加载题目列表失败:', error)
    ElMessage.error('加载题目列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadProblems()
}

// 重置搜索
const handleReset = () => {
  searchTitle.value = ''
  searchTag.value = ''
  currentPage.value = 1
  loadProblems()
}

// 分页大小改变时触发
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProblems()
}

// 页码改变时触发
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadProblems()
}

const handleCreate = () => {
  router.push('/problem/create')
}

const handleView = (id) => {
  router.push(`/problem/${id}`)
}

const handleEdit = (id) => {
  router.push(`/problem/edit/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这道题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delProblem(id)
    ElMessage.success('删除成功')
    // 刷新列表
    loadProblems()
  } catch {
    // 取消删除
  }
}

onMounted(() => {
  loadProblems()
})
</script>

<style scoped>
.problem-list {
  max-width: 1280px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  padding-bottom: var(--spacing-lg);
  border-bottom: 2px solid var(--color-border);
}

.header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

/* Search Bar */
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-xl);
  padding: var(--spacing-lg);
  background-color: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

/* Table Container */
:deep(.el-table) {
  --el-table-border-color: var(--color-border);
  --el-table-text-color: var(--color-text-secondary);
  --el-table-header-text-color: var(--color-text-primary);
  --el-table-row-hover-bg-color: rgba(37, 99, 235, 0.04);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

:deep(.el-table th) {
  background-color: rgba(0, 0, 0, 0.02);
  color: var(--color-text-primary);
  font-weight: 600;
  padding: 16px 0;
  font-size: 0.9375rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

:deep(.el-table td) {
  padding: 16px 0;
  transition: all var(--transition-fast);
  font-size: 0.9375rem;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: rgba(37, 99, 235, 0.04) !important;
}

/* Action Buttons */
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

/* Pagination */
.pagination {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
  padding: var(--spacing-lg) 0;
  border-top: 1px solid var(--color-border);
}

:deep(.el-pagination) {
  --el-pagination-button-color: var(--color-text-secondary);
  --el-pagination-hover-color: var(--color-primary);
}

:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .el-pager li) {
  background-color: var(--color-surface);
  color: var(--color-text-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  transition: all var(--transition-fast);
  font-weight: 500;
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  color: white;
  border-color: transparent;
  box-shadow: var(--shadow-md);
}

:deep(.el-pagination.is-background .btn-next):not([disabled]):hover,
:deep(.el-pagination.is-background .btn-prev):not([disabled]):hover,
:deep(.el-pagination.is-background .el-pager li:not(.disabled):hover) {
  color: var(--color-primary);
  background-color: rgba(37, 99, 235, 0.04);
  border-color: var(--color-primary-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* Primary Button */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;
  border-radius: var(--radius-md);
  padding: 12px 24px;
  font-weight: 600;
  font-size: 0.9375rem;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

:deep(.el-button--primary::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.el-button--primary:hover::before) {
  width: 300px;
  height: 300px;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* Loading */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
}

:deep(.el-loading-spinner .circular) {
  width: 50px;
  height: 50px;
}

:deep(.el-loading-spinner .path) {
  stroke: var(--color-primary);
  stroke-width: 3;
}
</style>
