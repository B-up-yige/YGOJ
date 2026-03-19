<template>
  <div class="user-info">
    <el-card v-loading="loading">
      <template #header>
        <h2>用户信息</h2>
      </template>

      <el-descriptions :column="1" border size="large">
        <el-descriptions-item label="用户 ID">{{ user.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions" style="margin-top: 30px; text-align: center;">
        <el-button @click="goBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserinfo } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const user = ref({
  id: route.params.id,
  username: '',
  nickname: '',
  email: ''
})

const loadUser = async () => {
  loading.value = true
  try {
    const res = await getUserinfo(route.params.id)
    user.value = res.data
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
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
}

.user-info h2 {
  margin: 0;
}
</style>
