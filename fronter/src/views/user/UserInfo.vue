<template>
  <div class="user-info">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
          <el-button type="primary" @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 用户基本信息 -->
      <div class="user-profile">
        <el-avatar :size="100" :icon="UserFilled" />
        <h3 class="nickname">{{ user.nickname || '暂无昵称' }}</h3>
        <p class="username">@{{ user.username }}</p>
        <div class="edit-btn-wrapper" v-if="isCurrentUser">
          <el-button type="primary" @click="showEditDialog" size="small">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>

      <!-- 详细信息 -->
      <el-descriptions title="基本信息" :column="1" border size="large">
        <el-descriptions-item label="用户 ID">
          <el-tag>{{ user.id }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">
          <span>{{ user.email || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 统计信息 -->
      <div class="statistics" style="margin-top: 30px;">
        <h3>个人统计</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="提交次数" :value="stats.submissions" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="通过题目" :value="stats.accepted" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="排名" :value="stats.rank" />
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 编辑用户信息对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称（3-20 位）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserFilled, Edit } from '@element-plus/icons-vue'
import { getUserinfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const updating = ref(false)

const isCurrentUser = computed(() => {
  return userStore.userInfo && userStore.userInfo.id.toString() === route.params.id
})

const user = ref({
  id: route.params.id,
  username: '',
  nickname: '',
  email: ''
})

// 编辑表单数据
const editForm = ref({
  nickname: '',
  email: ''
})

// 表单验证规则
const validateNickname = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入昵称'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('昵称长度必须在 3-20 位之间'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const editRules = {
  nickname: [
    { validator: validateNickname, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

// 统计数据
const stats = ref({
  submissions: 0,
  accepted: 0,
  rank: 0
})

const loadUser = async () => {
  loading.value = true
  try {
    const res = await getUserinfo(route.params.id)
    user.value = res.data
    
    // TODO: 加载用户统计数据
    // 这里可以调用后端接口获取用户的提交统计
    stats.value = {
      submissions: 0,
      accepted: 0,
      rank: 0
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示编辑对话框
const showEditDialog = () => {
  ElMessage.info('编辑功能暂未实现')
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.user-info {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.user-profile {
  text-align: center;
  padding: 30px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  margin-bottom: 30px;
  color: white;
  position: relative;
}

.edit-btn-wrapper {
  margin-top: 15px;
}

.user-profile .nickname {
  font-size: 24px;
  font-weight: bold;
  margin: 15px 0 5px 0;
}

.user-profile .username {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.statistics h3 {
  margin-bottom: 20px;
  color: #333;
}

.el-statistic {
  text-align: center;
}
</style>
