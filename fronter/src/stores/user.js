import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 权限常量定义
export const PERMISSIONS = {
  // 角色
  ROLE_USER: 'USER',
  ROLE_ADMIN: 'ADMIN',
  ROLE_CONTEST_ADMIN: 'CONTEST_ADMIN',
  ROLE_PROBLEM_ADMIN: 'PROBLEM_ADMIN',
  
  // 位运算权限
  PERM_PROBLEM_VIEW: 0,
  PERM_PROBLEM_SUBMIT: 1,
  PERM_PROBLEM_CREATE: 2,
  PERM_PROBLEM_EDIT: 3,
  PERM_PROBLEM_DELETE: 4,
  PERM_RECORD_VIEW: 5,
  PERM_RANKING_VIEW: 6,
  PERM_CONTEST_CREATE: 7,
  PERM_CONTEST_MANAGE: 8,
  PERM_CONTEST_JOIN: 9,
  PERM_PROBLEMSET_CREATE: 10,
  PERM_PROBLEMSET_MANAGE: 11,
  PERM_PROBLEMSET_VIEW: 12,
  PERM_USER_MANAGE: 13,
  PERM_SYSTEM_CONFIG: 14,
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const userRole = ref(localStorage.getItem('userRole') || '')
  const userPermission = ref(Number(localStorage.getItem('userPermission')) || 0)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info && info.id) {
      localStorage.setItem('userId', info.id.toString())
    }
  }
  
  function setUserAuth(role, permission) {
    userRole.value = role
    userPermission.value = permission
    localStorage.setItem('userRole', role)
    localStorage.setItem('userPermission', permission.toString())
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    userRole.value = ''
    userPermission.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userPermission')
  }
  
  // 检查是否有指定位运算权限
  function hasPermission(permissionBit) {
    return (userPermission.value & (1 << permissionBit)) !== 0
  }
  
  // 检查是否有指定角色
  function hasRole(role) {
    return userRole.value === role
  }
  
  // 检查是否是管理员
  const isAdmin = computed(() => userRole.value === PERMISSIONS.ROLE_ADMIN)
  
  // 检查是否可以创建题目
  const canCreateProblem = computed(() => hasPermission(PERMISSIONS.PERM_PROBLEM_CREATE))
  
  // 检查是否可以管理比赛
  const canManageContest = computed(() => hasPermission(PERMISSIONS.PERM_CONTEST_MANAGE))
  
  // 检查是否可以创建题集
  const canCreateProblemset = computed(() => hasPermission(PERMISSIONS.PERM_PROBLEMSET_CREATE))

  return {
    token,
    userInfo,
    userRole,
    userPermission,
    setToken,
    setUserInfo,
    setUserAuth,
    logout,
    hasPermission,
    hasRole,
    isAdmin,
    canCreateProblem,
    canManageContest,
    canCreateProblemset,
  }
})
