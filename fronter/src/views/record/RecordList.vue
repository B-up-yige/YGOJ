<template>
  <div class="record-list">
    <h2>{{ contestId ? '比赛提交记录' : '提交记录' }}</h2>

    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-input
        v-model="searchProblemId"
        placeholder="题目ID"
        clearable
        style="width: 150px; margin-right: 10px"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Document /></el-icon>
        </template>
      </el-input>
      <el-select
        v-model="searchStatus"
        placeholder="选择状态"
        clearable
        style="width: 180px; margin-right: 10px"
        @change="handleSearch"
        @clear="handleSearch"
      >
        <el-option label="通过" value="AC" />
        <el-option label="答案错误" value="WA" />
        <el-option label="超时" value="TLE" />
        <el-option label="超内存" value="MLE" />
        <el-option label="运行错误" value="RE" />
        <el-option label="编译错误" value="CE" />
        <el-option label="等待中" value="waiting" />
      </el-select>
      <el-input
        v-model="searchUserId"
        placeholder="用户ID"
        clearable
        style="width: 150px; margin-right: 10px"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><User /></el-icon>
        </template>
      </el-input>
      <el-checkbox v-model="mySubmissions" @change="handleSearch">我的提交</el-checkbox>
      <el-button type="primary" @click="handleSearch" style="margin-left: 10px">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="records" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="记录 ID" width="100" />
      <el-table-column prop="userName" label="用户" width="150">
        <template #default="scope">
          <span>{{ scope.row.userName || '未知用户' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="problemTitle" label="题目" min-width="200">
        <template #default="scope">
          <span>{{ scope.row.problemTitle || '未知题目' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="150">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.submitTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row.id)">详情</el-button>
          <el-button link type="warning" @click="handleRejudge(scope.row.id)">重测</el-button>
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

    <!-- 修改状态对话框 -->
    <el-dialog v-model="dialogVisible" title="修改状态" width="30%">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="Accepted" value="Accepted" />
            <el-option label="Wrong Answer" value="Wrong Answer" />
            <el-option label="Time Limit Exceeded" value="Time Limit Exceeded" />
            <el-option label="Memory Limit Exceeded" value="Memory Limit Exceeded" />
            <el-option label="Runtime Error" value="Runtime Error" />
            <el-option label="Compilation Error" value="Compilation Error" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, User } from '@element-plus/icons-vue'
import { getRecordList, getContestRecordList, rejudge } from '@/api/record'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const dialogVisible = ref(false)
const currentRecord = ref(null)

// 接收路由参数
const contestId = ref(route.params.contestId || null)
const problemId = ref(route.query.problemId || null)

const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索条件
const searchProblemId = ref('')
const searchStatus = ref('')
const searchUserId = ref('')
const mySubmissions = ref(false)

const statusForm = ref({
  status: ''
})

// 加载记录列表
const loadRecords = async () => {
  loading.value = true
  try {
    let res
    
    // 构建搜索参数
    const problemIdParam = searchProblemId.value ? parseInt(searchProblemId.value) : null
    const userIdParam = mySubmissions.value ? parseInt(localStorage.getItem('userId')) : (searchUserId.value ? parseInt(searchUserId.value) : null)
    
    if (contestId.value) {
      // 比赛提交记录
      res = await getContestRecordList(
        contestId.value, 
        currentPage.value, 
        pageSize.value,
        problemIdParam,
        searchStatus.value,
        userIdParam,
        mySubmissions.value
      )
    } else {
      // 普通提交记录
      res = await getRecordList(
        currentPage.value, 
        pageSize.value,
        problemIdParam,
        searchStatus.value,
        userIdParam,
        mySubmissions.value
      )
    }
    
    // 后端返回的数据格式：{ data: [...] }
    if (res.data) {
      if (Array.isArray(res.data)) {
        // 如果直接是数组
        records.value = res.data
        total.value = res.data.length
      } else if (res.data.records || res.data.list) {
        // 如果是分页对象
        records.value = res.data.records || res.data.list
        total.value = res.data.total || 0
      } else {
        records.value = []
        total.value = 0
      }
    }
  } catch (error) {
    console.error('加载记录列表失败:', error)
    ElMessage.error('加载记录列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadRecords()
}

// 重置搜索
const handleReset = () => {
  searchProblemId.value = ''
  searchStatus.value = ''
  searchUserId.value = ''
  mySubmissions.value = false
  currentPage.value = 1
  loadRecords()
}

// 分页大小改变时触发
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

// 页码改变时触发
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRecords()
}

const getStatusType = (status) => {
  const statusMap = {
    'AC': 'success',
    'WA': 'danger',
    'TLE': 'warning',
    'MLE': 'warning',
    'RE': 'danger',
    'CE': 'info',
    'waiting': '',
    'Accepted': 'success',
    'Wrong Answer': 'danger',
    'Time Limit Exceeded': 'warning',
    'Memory Limit Exceeded': 'warning',
    'Runtime Error': 'danger',
    'Compilation Error': 'info'
  }
  return statusMap[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  // 处理 LocalDateTime 格式: 2024-01-01T12:00:00
  const dateStr = time.replace('T', ' ')
  return dateStr.substring(0, 19)
}

const getStatusText = (status) => {
  const textMap = {
    'AC': '通过',
    'WA': '答案错误',
    'TLE': '超时',
    'MLE': '超内存',
    'RE': '运行错误',
    'CE': '编译错误',
    'waiting': '等待中',
    'Accepted': '通过',
    'Wrong Answer': '答案错误',
    'Time Limit Exceeded': '超时',
    'Memory Limit Exceeded': '超内存',
    'Runtime Error': '运行错误',
    'Compilation Error': '编译错误'
  }
  return textMap[status] || status
}

const handleView = (id) => {
  router.push(`/record/${id}`)
}

const handleRejudge = async (id) => {
  try {
    await ElMessageBox.confirm('确定要重新判题吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await rejudge(id)
    ElMessage.success('重测任务已提交')
    // 刷新列表
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重测失败:', error)
      ElMessage.error('重测失败')
    }
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.record-list {
  padding: 24px;
  background: var(--color-surface);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
}

.record-list h2 {
  margin-bottom: 24px;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 600;
}

/* Search Bar */
.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* Element Plus 表格样式优化 */
:deep(.el-table) {
  --el-table-border-color: var(--color-border);
  --el-table-text-color: var(--color-text-secondary);
  --el-table-header-text-color: var(--color-text-primary);
  --el-table-row-hover-bg-color: rgba(37, 99, 235, 0.04);
}

:deep(.el-table th) {
  background-color: rgba(0, 0, 0, 0.02);
  color: var(--color-text-primary);
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

/* 分页样式 */
:deep(.el-pagination) {
  margin-top: 20px;
  justify-content: flex-end;
}

/* 对话框样式 */
:deep(.el-dialog__header) {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 16px;
}

:deep(.el-dialog__title) {
  color: #303133;
  font-weight: 600;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}
</style>
