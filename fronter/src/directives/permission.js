import { useUserStore } from '@/stores/user'

/**
 * 权限控制自定义指令
 * 用法:
 * v-permission="PERMISSIONS.PERM_PROBLEM_CREATE"  // 检查单个权限
 * v-permission="{ type: 'role', value: 'ADMIN' }"  // 检查角色
 * v-permission="{ type: 'bit', value: 2 }"         // 检查位运算权限
 */
export default {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  }
}

function checkPermission(el, binding) {
  const userStore = useUserStore()
  const value = binding.value
  
  if (!value) {
    el.style.display = 'none'
    return
  }
  
  let hasPermission = false
  
  // 支持多种格式
  if (typeof value === 'number') {
    // 直接传入权限位索引
    hasPermission = userStore.hasPermission(value)
  } else if (typeof value === 'string') {
    // 传入角色名称
    hasPermission = userStore.hasRole(value)
  } else if (typeof value === 'object') {
    // 对象格式
    if (value.type === 'role') {
      hasPermission = userStore.hasRole(value.value)
    } else if (value.type === 'bit') {
      hasPermission = userStore.hasPermission(value.value)
    } else if (value.type === 'or') {
      // OR逻辑：满足任一权限
      hasPermission = value.values?.some(bit => userStore.hasPermission(bit))
    } else if (value.type === 'and') {
      // AND逻辑：需要所有权限
      hasPermission = value.values?.every(bit => userStore.hasPermission(bit))
    }
  }
  
  if (!hasPermission) {
    el.style.display = 'none'
  } else {
    el.style.display = ''
  }
}
