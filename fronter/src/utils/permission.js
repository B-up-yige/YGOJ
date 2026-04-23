import { useUserStore, PERMISSIONS } from '@/stores/user'

/**
 * 权限检查工具函数
 */

/**
 * 检查是否有指定位运算权限
 * @param {number} permissionBit - 权限位索引
 * @returns {boolean}
 */
export function hasPermission(permissionBit) {
  const userStore = useUserStore()
  return userStore.hasPermission(permissionBit)
}

/**
 * 检查是否有指定角色
 * @param {string} role - 角色名称
 * @returns {boolean}
 */
export function hasRole(role) {
  const userStore = useUserStore()
  return userStore.hasRole(role)
}

/**
 * 检查是否是超级管理员
 * @returns {boolean}
 */
export function isSuperAdmin() {
  const userStore = useUserStore()
  return userStore.isSuperAdmin
}

/**
 * 检查是否是管理员（包括超级管理员）
 * @returns {boolean}
 */
export function isAdmin() {
  const userStore = useUserStore()
  return userStore.isAdmin
}

/**
 * 检查是否可以提交代码
 * @returns {boolean}
 */
export function canSubmitCode() {
  const userStore = useUserStore()
  return userStore.canSubmitCode
}

/**
 * 检查是否可以参加比赛
 * @returns {boolean}
 */
export function canJoinContest() {
  const userStore = useUserStore()
  return userStore.canJoinContest
}

/**
 * 检查是否可以创建题目
 * @returns {boolean}
 */
export function canCreateProblem() {
  const userStore = useUserStore()
  return userStore.canCreateProblem
}

/**
 * 检查是否可以管理题目
 * @returns {boolean}
 */
export function canManageProblem() {
  const userStore = useUserStore()
  return userStore.canManageProblem
}

/**
 * 检查是否可以创建比赛
 * @returns {boolean}
 */
export function canCreateContest() {
  const userStore = useUserStore()
  return userStore.canCreateContest
}

/**
 * 检查是否可以管理比赛
 * @returns {boolean}
 */
export function canManageContest() {
  const userStore = useUserStore()
  return userStore.canManageContest
}

/**
 * 检查是否可以创建题集
 * @returns {boolean}
 */
export function canCreateProblemset() {
  const userStore = useUserStore()
  return userStore.canCreateProblemset
}

/**
 * 检查是否可以管理题集
 * @returns {boolean}
 */
export function canManageProblemset() {
  const userStore = useUserStore()
  return userStore.canManageProblemset
}

/**
 * 检查是否可以创建帖子
 * @returns {boolean}
 */
export function canCreatePost() {
  const userStore = useUserStore()
  return userStore.canCreatePost
}

/**
 * 检查是否可以管理帖子
 * @returns {boolean}
 */
export function canManagePost() {
  const userStore = useUserStore()
  return userStore.canManagePost
}

/**
 * 检查是否可以发表评论
 * @returns {boolean}
 */
export function canCreateComment() {
  const userStore = useUserStore()
  return userStore.canCreateComment
}

/**
 * 检查是否可以管理用户权限
 * @returns {boolean}
 */
export function canManageUser() {
  const userStore = useUserStore()
  return userStore.canManageUser
}

/**
 * 检查是否有任一权限（OR逻辑）
 * @param {number[]} permissionBits - 权限位数组
 * @returns {boolean}
 */
export function hasAnyPermission(permissionBits) {
  const userStore = useUserStore()
  return permissionBits.some(bit => userStore.hasPermission(bit))
}

/**
 * 检查是否拥有所有权限（AND逻辑）
 * @param {number[]} permissionBits - 权限位数组
 * @returns {boolean}
 */
export function hasAllPermissions(permissionBits) {
  const userStore = useUserStore()
  return permissionBits.every(bit => userStore.hasPermission(bit))
}
