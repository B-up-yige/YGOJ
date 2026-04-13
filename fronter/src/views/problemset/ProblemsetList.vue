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
import { getProblemsetList, delProblemset } from '@/api/problemset'

const router = useRouter()
const loading = ref(false)
const problemsets = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
    }
  } catch (error) {
    console.error('加载题集列表失败:', error)
    ElMessage.error('加载题集列表失败')
  } finally {
    loading.value = false
  }
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
</style>
