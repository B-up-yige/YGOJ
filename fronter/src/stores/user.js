import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 权限常量定义
export const PERMISSIONS = {
  // 角色
  ROLE_SUPER_ADMIN: 'SUPER_ADMIN',
  ROLE_ADMIN: 'ADMIN',
  ROLE_USER: 'USER',
  
  // 位运算权限
  PERM_PROBLEM_SUBMIT: 0,           // 代码提交权限
  PERM_CONTEST_JOIN: 1,             // 参加比赛权限
  PERM_PROBLEM_CREATE: 2,           // 创建题目
  PERM_PROBLEM_MANAGE_OWN: 3,       // 管理自己的题目（编辑+删除）
  PERM_PROBLEM_MANAGE_ALL: 4,       // 管理所有题目（编辑+删除）
  PERM_CONTEST_CREATE: 5,           // 创建比赛
  PERM_CONTEST_MANAGE_OWN: 6,       // 管理自己的比赛（编辑+删除）
  PERM_CONTEST_MANAGE_ALL: 7,       // 管理所有比赛（编辑+删除）
  PERM_PROBLEMSET_CREATE: 8,        // 创建题集
  PERM_PROBLEMSET_MANAGE_OWN: 9,    // 管理自己的题集（编辑+删除+管理题目）
  PERM_PROBLEMSET_MANAGE_ALL: 10,   // 管理所有题集（编辑+删除+管理题目）
  PERM_POST_CREATE: 11,             // 创建帖子
  PERM_POST_MANAGE_OWN: 12,         // 管理自己的帖子（编辑+删除）
  PERM_POST_MANAGE_ALL: 13,         // 管理所有帖子（编辑+删除+板块管理）
  PERM_COMMENT_CREATE: 14,          // 发表评论
  PERM_COMMENT_DELETE_OWN: 15,      // 删除自己的评论
  PERM_COMMENT_DELETE_ALL: 16,      // 删除所有评论
  PERM_USER_MANAGE: 17,             // 用户管理（修改他人权限和角色）
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
  
  // 检查是否是超级管理员
  const isSuperAdmin = computed(() => userRole.value === PERMISSIONS.ROLE_SUPER_ADMIN)
  
  // 检查是否是管理员（包括超级管理员）
  const isAdmin = computed(() => 
    userRole.value === PERMISSIONS.ROLE_ADMIN || 
    userRole.value === PERMISSIONS.ROLE_SUPER_ADMIN
  )
  
  // 检查是否可以提交代码
  const canSubmitCode = computed(() => hasPermission(PERMISSIONS.PERM_PROBLEM_SUBMIT))
  
  // 检查是否可以参加比赛
  const canJoinContest = computed(() => hasPermission(PERMISSIONS.PERM_CONTEST_JOIN))
  
  // 检查是否可以创建题目
  const canCreateProblem = computed(() => hasPermission(PERMISSIONS.PERM_PROBLEM_CREATE))
  
  // 检查是否可以管理题目（自己的或全部）
  const canManageProblem = computed(() => 
    hasPermission(PERMISSIONS.PERM_PROBLEM_MANAGE_OWN) || 
    hasPermission(PERMISSIONS.PERM_PROBLEM_MANAGE_ALL)
  )
  
  // 检查是否可以创建比赛
  const canCreateContest = computed(() => hasPermission(PERMISSIONS.PERM_CONTEST_CREATE))
  
  // 检查是否可以管理比赛（自己的或全部）
  const canManageContest = computed(() => 
    hasPermission(PERMISSIONS.PERM_CONTEST_MANAGE_OWN) || 
    hasPermission(PERMISSIONS.PERM_CONTEST_MANAGE_ALL)
  )
  
  // 检查是否可以创建题集
  const canCreateProblemset = computed(() => hasPermission(PERMISSIONS.PERM_PROBLEMSET_CREATE))
  
  // 检查是否可以管理题集（自己的或全部）
  const canManageProblemset = computed(() => 
    hasPermission(PERMISSIONS.PERM_PROBLEMSET_MANAGE_OWN) || 
    hasPermission(PERMISSIONS.PERM_PROBLEMSET_MANAGE_ALL)
  )
  
  // 检查是否可以创建帖子
  const canCreatePost = computed(() => hasPermission(PERMISSIONS.PERM_POST_CREATE))
  
  // 检查是否可以管理帖子（自己的或全部）
  const canManagePost = computed(() => 
    hasPermission(PERMISSIONS.PERM_POST_MANAGE_OWN) || 
    hasPermission(PERMISSIONS.PERM_POST_MANAGE_ALL)
  )
  
  // 检查是否可以发表评论
  const canCreateComment = computed(() => hasPermission(PERMISSIONS.PERM_COMMENT_CREATE))
  
  // 检查是否可以管理用户权限
  const canManageUser = computed(() => hasPermission(PERMISSIONS.PERM_USER_MANAGE))

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
    isSuperAdmin,
    isAdmin,
    canSubmitCode,
    canJoinContest,
    canCreateProblem,
    canManageProblem,
    canCreateContest,
    canManageContest,
    canCreateProblemset,
    canManageProblemset,
    canCreatePost,
    canManagePost,
    canCreateComment,
    canManageUser,
  }
})
