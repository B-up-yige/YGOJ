<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">
        <router-link to="/">
          <span class="logo-icon">⚡</span>
          <span class="logo-text">YGOJ</span>
        </router-link>
      </div>
      <el-menu
        mode="horizontal"
        :ellipsis="false"
        :default-active="activeMenu"
        class="nav-menu"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/home">
          <el-icon><Home /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/problems">
          <el-icon><Document /></el-icon>
          <span>题目</span>
        </el-menu-item>
        <el-menu-item index="/contests">
          <el-icon><Trophy /></el-icon>
          <span>比赛</span>
        </el-menu-item>
        <el-menu-item index="/problemsets">
          <el-icon><Collection /></el-icon>
          <span>题集</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><List /></el-icon>
          <span>提交</span>
        </el-menu-item>
      </el-menu>
      <div class="user-actions">
        <el-dropdown v-if="isLoggedIn" trigger="click">
          <div class="user-profile">
            <div class="avatar">{{ currentUser?.nickname?.charAt(0) || 'U' }}</div>
            <span class="username">{{ currentUser?.nickname || '用户' }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="custom-dropdown">
              <el-dropdown-item @click="viewProfile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div v-else class="auth-buttons">
          <el-button class="btn-login" @click="goToLogin">登录</el-button>
          <el-button type="primary" class="btn-register" @click="goToRegister">注册</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserIdByToken, getUserinfo } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => !!userStore.token)
const currentUser = computed(() => userStore.userInfo)

console.log('当前是否登录:', isLoggedIn.value)
console.log('当前用户信息:', currentUser.value)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/home')) return '/home'
  // 注意：problemsets 必须在 problems 之前判断
  if (path.startsWith('/problemsets') || path.startsWith('/problemset/')) return '/problemsets'
  if (path.startsWith('/problems') || path.startsWith('/problem/')) return '/problems'
  if (path.startsWith('/contests') || path.startsWith('/contest/')) return '/contests'
  if (path.startsWith('/records') || path.startsWith('/record/')) return '/records'
  return ''
})

// 加载当前用户信息
const loadUserInfo = async () => {
  console.log('开始加载用户信息，token:', userStore.token)
  
  if (!userStore.token) {
    console.log('没有 token，跳过加载')
    return
  }
  
  try {
    // 第一步：通过 token 获取用户 ID
    const idRes = await getUserIdByToken(userStore.token)
    console.log('获取到用户 ID:', idRes.data)
    
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
      console.log('获取到用户信息:', userInfoRes.data)
      console.log('用户昵称:', userInfoRes.data.nickname)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const viewProfile = () => {
  if (currentUser.value?.id) {
    router.push(`/user/${currentUser.value.id}`)
  }
}

const handleLogout = async () => {
  try {
    // await logout()
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  console.log('菜单被点击:', index)
  router.push(index).catch(err => {
    console.error('路由跳转失败:', err)
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

/* Header */
.header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
  background: var(--color-surface);
  padding: 0 var(--spacing-2xl);
  height: 64px;
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
}

/* Logo */
.logo a {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  text-decoration: none;
  transition: transform var(--transition-fast);
}

.logo a:hover {
  transform: scale(1.02);
}

.logo-icon {
  font-size: 1.5rem;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

/* Navigation Menu */
.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
}

:deep(.el-menu-item) {
  height: 64px;
  line-height: 64px;
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  border-bottom: 2px solid transparent !important;
  transition: all var(--transition-fast);
  padding: 0 var(--spacing-lg);
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 6px;
  font-size: 1.125rem;
  display: flex;
  align-items: center;
}

:deep(.el-menu-item span) {
  display: inline-flex;
  align-items: center;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(37, 99, 235, 0.04);
  color: var(--color-primary);
}

:deep(.el-menu-item.is-active) {
  color: var(--color-primary);
  border-bottom: 2px solid var(--color-primary) !important;
  background-color: rgba(37, 99, 235, 0.04);
}

/* User Actions */
.user-actions {
  margin-left: auto;
}

/* User Profile */
.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.user-profile:hover {
  background-color: rgba(37, 99, 235, 0.04);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.username {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--color-text-primary);
}

.dropdown-icon {
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
  transition: transform var(--transition-fast);
}

.user-profile:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* Auth Buttons */
.auth-buttons {
  display: flex;
  gap: var(--spacing-sm);
}

.btn-login {
  color: var(--color-text-secondary);
  font-weight: 500;
  transition: all var(--transition-fast);
}

.btn-login:hover {
  color: var(--color-primary);
  background-color: rgba(37, 99, 235, 0.04);
}

.btn-register {
  background: var(--color-primary);
  border-color: var(--color-primary);
  font-weight: 500;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.btn-register:hover {
  background: var(--color-primary-dark);
  border-color: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* Custom Dropdown */
:deep(.custom-dropdown) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  padding: var(--spacing-xs);
}

:deep(.el-dropdown-menu__item) {
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  font-size: 0.9375rem;
  transition: all var(--transition-fast);
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: rgba(37, 99, 235, 0.04);
  color: var(--color-primary);
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
}

/* Main Content */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-2xl);
}

/* Responsive */
@media (max-width: 768px) {
  .header {
    padding: 0 var(--spacing-md);
    gap: var(--spacing-md);
  }
  
  .logo-text {
    font-size: 1.25rem;
  }
  
  :deep(.el-menu-item span) {
    display: none;
  }
  
  .username {
    display: none;
  }
}
</style>
