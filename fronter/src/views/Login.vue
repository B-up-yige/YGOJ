<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">用户登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号/邮箱" prop="loginStr">
          <el-input v-model="loginForm.loginStr" placeholder="请输入用户名或邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="link-text">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, getUserIdByToken, getUserinfo } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  loginStr: '',
  password: ''
})

const rules = {
  loginStr: [
    { required: true, message: '请输入账号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm.loginStr, loginForm.password)
        
        // 保存 token
        const token = res.data
        userStore.setToken(token)
        
        ElMessage.success('登录成功')
        
        // 延迟一下，确保 token 已经保存
        setTimeout(async () => {
          try {
            // 第一步：通过 token 获取用户 ID
            const idRes = await getUserIdByToken(token)
            if (idRes.data) {
              const userId = idRes.data
              
              // 第二步：通过用户 ID 获取完整用户信息
              const userInfoRes = await getUserinfo(userId)
              if (userInfoRes.data) {
                userStore.setUserInfo(userInfoRes.data)
                console.log('登录成功后获取到用户信息:', userInfoRes.data)
              }
            }
          } catch (err) {
            console.error('获取用户信息失败:', err)
          }
        }, 100)
        
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
  padding: 20px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.link-text {
  text-align: center;
  width: 100%;
}

.link-text a {
  color: #667eea;
  text-decoration: none;
}

.link-text a:hover {
  text-decoration: underline;
}
</style>
