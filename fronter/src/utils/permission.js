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
 * 检查是否是管理员
 * @returns {boolean}
 */
export function isAdmin() {
  const userStore = useUserStore()
  return userStore.isAdmin
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
