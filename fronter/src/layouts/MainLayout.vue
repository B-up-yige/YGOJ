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
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
        style="flex: 1; min-width: 0;"
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
    
    if (idRes.data) {
      const userId = idRes.data
      
      // 第二步：通过用户 ID 获取完整用户信息
      const userInfoRes = await getUserinfo(userId)
      if (userInfoRes.data) {
        userStore.setUserInfo(userInfoRes.data)
        console.log('获取到用户信息:', userInfoRes.data)
        console.log('用户昵称:', userInfoRes.data.nickname)
      }
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
}

.header {
  display: flex;
  align-items: center;
  background-color: #545c64;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.logo {
  margin-right: 40px;
}

.logo a {
  color: #fff;
  font-size: 24px;
  font-weight: bold;
  text-decoration: none;
}

.logo a:hover {
  color: #ffd04b;
}

.user-actions {
  margin-left: auto;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
