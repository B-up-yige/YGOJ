<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">
        <router-link to="/">YGOJ</router-link>
      </div>
      <el-menu
        mode="horizontal"
        :ellipsis="false"
        :default-active="activeMenu"
        background-color="#fff"
        text-color="#303133"
        active-text-color="#409EFF"
        style="flex: 1; min-width: 0; border-bottom: none;"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/home">
          <el-icon><Home /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/problems">
          <el-icon><Document /></el-icon>
          <span>题目列表</span>
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
          <span>提交记录</span>
        </el-menu-item>
      </el-menu>
      <div class="user-actions">
        <el-dropdown v-if="isLoggedIn">
          <span class="user-info" style="color: white; cursor: pointer;">
            {{ currentUser?.nickname || '用户' }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="viewProfile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div v-else>
          <el-button type="text" @click="goToLogin" style="color: white;">登录</el-button>
          <el-button type="primary" @click="goToRegister" size="small">注册</el-button>
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
  if (path.startsWith('/problems')) return '/problems'
  if (path.startsWith('/contests')) return '/contests'
  if (path.startsWith('/problemsets')) return '/problemsets'
  if (path.startsWith('/records')) return '/records'
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
}

.header {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 0 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid #e4e7ed;
  height: 60px;
  line-height: 60px;
}

.logo {
  margin-right: 40px;
  flex-shrink: 0;
}

.logo a {
  color: #409EFF;
  font-size: 24px;
  font-weight: bold;
  text-decoration: none;
  transition: all 0.3s ease;
}

.logo a:hover {
  color: #66b1ff;
  transform: scale(1.02);
}

.user-actions {
  margin-left: auto;
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  background-color: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

/* 菜单项样式优化 */
:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background-color: #ecf5ff;
}

:deep(.el-menu-item.is-active) {
  border-bottom: 2px solid #409EFF;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  padding: 10px 16px;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #ecf5ff;
  color: #409EFF;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }
  
  .logo {
    margin-right: 20px;
  }
  
  .logo a {
    font-size: 20px;
  }
}
</style>
