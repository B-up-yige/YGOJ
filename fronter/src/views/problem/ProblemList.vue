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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
// TODO: 需要添加获取所有题目的 API
// import { getProblemList } from '@/api/problem'

const router = useRouter()
const loading = ref(false)
const problems = ref([
  // 临时测试数据
  { id: 1, title: 'A+B Problem', authorId: 1, timeLimit: 1000, memoryLimit: 256 },
  { id: 2, title: 'Hello World', authorId: 1, timeLimit: 500, memoryLimit: 128 }
])

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
    
    // TODO: 调用删除 API
    // await delProblem(id)
    ElMessage.success('删除成功')
    // 刷新列表
  } catch {
    // 取消删除
  }
}

onMounted(() => {
  // TODO: 加载题目列表
  // loadProblems()
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
