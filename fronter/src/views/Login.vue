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
        
        // 等待获取用户信息后再跳转
        try {
          // 第一步：通过 token 获取用户 ID
          const idRes = await getUserIdByToken(token)
          
          // 如果 token 无效 (返回 null)，清除本地存储并跳转到登录页
          if (!idRes.data) {
            console.log('token 已失效，清除本地存储')
            userStore.logout()
            router.push('/login')
            return
          }
          
          const userId = idRes.data
          
          // 第二步：通过用户 ID 获取完整用户信息
          const userInfoRes = await getUserinfo(userId)
          if (userInfoRes.data) {
            userStore.setUserInfo(userInfoRes.data)
            // 保存用户权限信息
            const role = userInfoRes.data.role || 'USER'
            const permission = userInfoRes.data.permission || 3
            userStore.setUserAuth(role, permission)
            console.log('登录成功后获取到用户信息:', userInfoRes.data)
            console.log('用户角色:', role)
            console.log('用户权限:', permission)
          }
        } catch (err) {
          console.error('获取用户信息失败:', err)
        }
        
        // 获取完用户信息后再跳转
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
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-card {
  width: 450px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.15);
  position: relative;
  z-index: 1;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  text-shadow: 0 0 20px rgba(102, 126, 234, 0.6);
  letter-spacing: 2px;
}

.link-text {
  text-align: center;
  width: 100%;
  color: #d0d0d0;
}

.link-text a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.link-text a:hover {
  color: #764ba2;
  text-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
}

/* Element Plus 表单科技风格 */
:deep(.el-form-item__label) {
  color: #e0e0e0;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.3) inset;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.5) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset, 0 0 15px rgba(102, 126, 234, 0.3);
}

:deep(.el-input__inner) {
  color: #e0e0e0;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  font-weight: 600;
  letter-spacing: 1px;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
}
</style>
