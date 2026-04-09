<template>
  <div class="record-list">
    <h2>提交记录</h2>

    <el-table :data="records" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="记录 ID" width="100" />
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column prop="problemId" label="题目 ID" width="100" />
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
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row.id)">详情</el-button>
          <el-button link type="warning" @click="handleEditStatus(scope.row)">修改状态</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecordList } from '@/api/record'

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const currentRecord = ref(null)

const records = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusForm = ref({
  status: ''
})

// 加载记录列表
const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getRecordList(currentPage.value, pageSize.value)
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

const handleEditStatus = (record) => {
  ElMessage.info('修改状态功能暂未实现')
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.record-list {
  padding: 20px;
}

.record-list h2 {
  margin-bottom: 20px;
}
</style>
