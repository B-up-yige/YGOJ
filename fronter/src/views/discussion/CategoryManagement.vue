<template>
  <div class="category-management">
    <div class="header">
      <h2>板块管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        <span>新增板块</span>
      </el-button>
    </div>

    <!-- 板块列表 -->
    <el-table :data="categories" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="板块代码" width="150" />
      <el-table-column prop="name" label="板块名称" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isActive ? 'success' : 'info'">
            {{ scope.row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="showEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑板块' : '新增板块'"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="板块代码" prop="code">
          <el-input 
            v-model="form.code" 
            placeholder="请输入板块代码（英文大写）" 
            :disabled="isEdit"
            maxlength="50"
          />
          <div class="form-tip">唯一标识，如：GENERAL、PROBLEM_HELP</div>
        </el-form-item>
        
        <el-form-item label="板块名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入板块名称" maxlength="100" />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入板块描述" 
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <div class="form-tip">数字越小越靠前</div>
        </el-form-item>
        
        <el-form-item label="状态" prop="isActive">
          <el-switch v-model="form.isActive" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '@/api/discussion'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  code: '',
  name: '',
  description: '',
  sortOrder: 0,
  isActive: true
})

const rules = {
  code: [
    { required: true, message: '请输入板块代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入板块名称', trigger: 'blur' }
  ]
}

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getAllCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载板块列表失败:', error)
    ElMessage.error('加载板块列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    code: '',
    name: '',
    description: '',
    sortOrder: 0,
    isActive: true
  }
  dialogVisible.value = true
}

const showEditDialog = (category) => {
  isEdit.value = true
  form.value = {
    id: category.id,
    code: category.code,
    name: category.name,
    description: category.description,
    sortOrder: category.sortOrder,
    isActive: category.isActive
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateCategory(form.value)
        ElMessage.success('更新成功')
      } else {
        await createCategory(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await loadCategories()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个板块吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCategory(id)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  // 检查管理员权限
  if (!userStore.isAdmin) {
    ElMessage.error('无权访问此页面')
    router.push('/discussion')
    return
  }
  
  loadCategories()
})
</script>

<style scoped>
.category-management {
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
}

.header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 600;
}

.form-tip {
  margin-top: 4px;
  color: var(--color-text-tertiary);
  font-size: 12px;
}
</style>
