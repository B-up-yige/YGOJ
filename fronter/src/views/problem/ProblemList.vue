<template>
  <div class="problem-list">
    <div class="header">
      <h2>题目列表</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建题目
      </el-button>
    </div>

    <el-table :data="problems" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="authorId" label="作者 ID" width="120" />
      <el-table-column prop="timeLimit" label="时间限制 (ms)" width="120" />
      <el-table-column prop="memoryLimit" label="内存限制 (MB)" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row.id)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(scope.row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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
import { getProblemList, delProblem } from '@/api/problem'

const router = useRouter()
const loading = ref(false)
const problems = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载题目列表
const loadProblems = async () => {
  loading.value = true
  try {
    const res = await getProblemList(currentPage.value, pageSize.value)
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
</style>
