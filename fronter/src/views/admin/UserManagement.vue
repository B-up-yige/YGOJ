<template>
  <div class="user-management">
    <el-card class="header-card">
      <h2>用户权限管理</h2>
      <p class="subtitle">管理系统用户及其权限配置</p>
    </el-card>

    <el-card class="table-card">
      <!-- 搜索和过滤 -->
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或邮箱"
          style="width: 300px"
          clearable
          @clear="loadUsers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadUsers">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 用户列表表格 -->
      <el-table :data="users" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限值" width="120">
          <template #default="{ row }">
            <span>{{ row.permission }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" min-width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">
              编辑权限
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              :disabled="row.role === 'ADMIN'"
              @click="handleDeleteUser(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 编辑权限对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户权限"
      width="600px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="比赛管理员" value="CONTEST_ADMIN" />
            <el-option label="题目管理员" value="PROBLEM_ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="权限配置">
          <div class="permission-grid">
            <el-checkbox-group v-model="selectedPermissions">
              <div class="permission-item" v-for="perm in permissionList" :key="perm.bit">
                <el-checkbox :label="perm.bit">{{ perm.name }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </el-form-item>

        <el-form-item label="权限值">
          <el-input-number 
            v-model="editForm.permission" 
            :min="0" 
            :max="65535"
            disabled
          />
          <span class="tip">根据上方选择自动计算</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const editDialogVisible = ref(false)
const editForm = ref({
  userId: null,
  username: '',
  role: 'USER',
  permission: 0
})

const selectedPermissions = ref([])

// 权限列表定义
const permissionList = [
  { bit: 0, name: '查看题目' },
  { bit: 1, name: '提交代码' },
  { bit: 2, name: '创建题目' },
  { bit: 3, name: '编辑题目' },
  { bit: 4, name: '删除题目' },
  { bit: 5, name: '查看题解' },
  { bit: 6, name: '创建题解' },
  { bit: 7, name: '查看提交记录' },
  { bit: 8, name: '查看排行榜' },
  { bit: 9, name: '创建比赛' },
  { bit: 10, name: '管理比赛' },
  { bit: 11, name: '参加比赛' },
  { bit: 12, name: '创建题集' },
  { bit: 13, name: '管理题集' },
  { bit: 14, name: '查看题集' },
  { bit: 15, name: '管理用户' },
  { bit: 16, name: '系统配置' }
]

// 监听选中权限变化，自动计算权限值
watch(selectedPermissions, (newVal) => {
  let permissionValue = 0
  newVal.forEach(bit => {
    permissionValue |= (1 << bit)
  })
  editForm.value.permission = permissionValue
})

// 获取用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/user/admin/users', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value
      },
      headers: {
        'Authorization': token
      }
    })
    
    if (response.data.code === 200) {
      users.value = response.data.data.list
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 显示编辑对话框
const showEditDialog = (user) => {
  editForm.value = {
    userId: user.id,
    username: user.username,
    role: user.role,
    permission: user.permission
  }
  
  // 根据权限值反选checkbox
  selectedPermissions.value = []
  permissionList.forEach(perm => {
    if ((user.permission & (1 << perm.bit)) !== 0) {
      selectedPermissions.value.push(perm.bit)
    }
  })
  
  editDialogVisible.value = true
}

// 更新用户权限
const handleUpdatePermission = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.put('/api/user/admin/user/permission', {
      userId: editForm.value.userId,
      role: editForm.value.role,
      permission: editForm.value.permission
    }, {
      headers: {
        'Authorization': token
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('权限更新成功')
      editDialogVisible.value = false
      loadUsers()
    } else {
      ElMessage.error(response.data.message || '权限更新失败')
    }
  } catch (error) {
    console.error('更新权限失败:', error)
    ElMessage.error('更新权限失败')
  }
}

// 删除用户
const handleDeleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const token = localStorage.getItem('token')
    const response = await axios.delete(`/api/user/admin/user/${user.id}`, {
      headers: {
        'Authorization': token
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('用户删除成功')
      loadUsers()
    } else {
      ElMessage.error(response.data.message || '删除用户失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

// 获取角色名称
const getRoleName = (role) => {
  const roleMap = {
    'USER': '普通用户',
    'ADMIN': '管理员',
    'CONTEST_ADMIN': '比赛管理员',
    'PROBLEM_ADMIN': '题目管理员'
  }
  return roleMap[role] || role
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    'USER': '',
    'ADMIN': 'danger',
    'CONTEST_ADMIN': 'warning',
    'PROBLEM_ADMIN': 'success'
  }
  return typeMap[role] || ''
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 10px 0;
  color: var(--color-text-primary);
}

.subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.table-card {
  min-height: 500px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.permission-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.permission-item {
  padding: 5px;
}

.tip {
  margin-left: 10px;
  color: var(--color-text-tertiary);
  font-size: 12px;
}
</style>
