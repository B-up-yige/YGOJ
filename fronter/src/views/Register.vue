<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（3-20 位字母、数字、下划线）" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入昵称（3-20 位）" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="link-text">
            已有账号？<router-link to="/login">立即登录</router-link>
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
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  email: ''
})

const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('用户名只能由字母、数字和下划线组成'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度必须在 3-20 位之间'))
  } else {
    callback()
  }
}

const validateNickname = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入昵称'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('昵称长度必须在 3-20 位之间'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!/^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{}|;:'",.<>/?]+$/.test(value)) {
    callback(new Error('密码只能由字母、数字和特殊符号组成'))
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

const rules = {
  username: [
    { validator: validateUsername, trigger: 'blur' }
  ],
  nickname: [
    { validator: validateNickname, trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register(registerForm)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: var(--spacing-2xl);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

[data-theme='dark'] .register-container {
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
}

.register-container::before {
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

.register-card {
  width: 450px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
}

[data-theme='dark'] .register-card {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 32px;
  font-weight: bold;
  text-shadow: none;
  letter-spacing: 2px;
}

[data-theme='dark'] .register-title {
  color: #fff;
  text-shadow: 0 0 20px rgba(102, 126, 234, 0.6);
}

.link-text {
  text-align: center;
  width: 100%;
  color: #666;
}

[data-theme='dark'] .link-text {
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
  color: #333;
  font-weight: 500;
}

[data-theme='dark'] :deep(.el-form-item__label) {
  color: #e0e0e0;
}

:deep(.el-input__wrapper) {
  background: #fff;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 8px;
  transition: all 0.3s ease;
}

[data-theme='dark'] :deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.3) inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.5) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset, 0 0 15px rgba(102, 126, 234, 0.3);
}

:deep(.el-input__inner) {
  color: #333;
}

[data-theme='dark'] :deep(.el-input__inner) {
  color: #e0e0e0;
}

/* 占位符文本颜色 */
:deep(.el-input__inner::placeholder) {
  color: #a8abb2;
}

[data-theme='dark'] :deep(.el-input__inner::placeholder) {
  color: #6b7280;
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
