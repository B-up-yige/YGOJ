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
import { getContestList, delContest } from '@/api/contest'

const router = useRouter()
const loading = ref(false)
const contests = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
    }
  } catch (error) {
    console.error('加载比赛列表失败:', error)
    ElMessage.error('加载比赛列表失败')
  } finally {
    loading.value = false
  }
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
  loadContests()
})
</script>

<style scoped>
.contest-list {
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
</style>
