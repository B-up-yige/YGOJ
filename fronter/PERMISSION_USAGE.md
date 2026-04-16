# 前端权限控制使用指南

## 概述

前端权限控制系统基于 Vuex Store 中的用户权限信息，提供多种方式使用权限控制。

## 核心概念

### 1. 权限存储 (user store)

在 `stores/user.js` 中定义了：
- `userRole`: 用户角色（USER, ADMIN, CONTEST_ADMIN等）
- `userPermission`: 位运算权限值
- 权限检查方法：`hasPermission()`, `hasRole()`, `isAdmin()` 等

### 2. 权限常量

```javascript
import { PERMISSIONS } from '@/stores/user'

// 角色常量
PERMISSIONS.ROLE_USER          // 普通用户
PERMISSIONS.ROLE_ADMIN         // 管理员
PERMISSIONS.ROLE_CONTEST_ADMIN // 比赛管理员
PERMISSIONS.ROLE_PROBLEM_ADMIN // 题目管理员

// 位运算权限常量
PERMISSIONS.PERM_PROBLEM_VIEW    // 0 - 查看题目
PERMISSIONS.PERM_PROBLEM_SUBMIT  // 1 - 提交代码
PERMISSIONS.PERM_PROBLEM_CREATE  // 2 - 创建题目
PERMISSIONS.PERM_PROBLEM_EDIT    // 3 - 编辑题目
PERMISSIONS.PERM_PROBLEM_DELETE  // 4 - 删除题目
PERMISSIONS.PERM_CONTEST_CREATE  // 9 - 创建比赛
PERMISSIONS.PERM_CONTEST_MANAGE  // 10 - 管理比赛
PERMISSIONS.PERM_PROBLEMSET_CREATE // 12 - 创建题集
PERMISSIONS.PERM_PROBLEMSET_MANAGE // 13 - 管理题集
```

## 使用方式

### 方式1: 在模板中使用 v-permission 指令

```vue
<template>
  <!-- 检查单个权限 -->
  <el-button v-permission="PERMISSIONS.PERM_PROBLEM_CREATE">
    创建题目
  </el-button>
  
  <!-- 检查角色 -->
  <el-button v-permission="'ADMIN'">
    管理员功能
  </el-button>
  
  <!-- OR逻辑：满足任一权限 -->
  <el-button v-permission="{ type: 'or', values: [2, 3] }">
    创建或编辑题目
  </el-button>
  
  <!-- AND逻辑：需要所有权限 -->
  <el-button v-permission="{ type: 'and', values: [2, 3] }">
    创建和编辑题目
  </el-button>
</template>

<script setup>
import { PERMISSIONS } from '@/stores/user'
</script>
```

### 方式2: 在 script 中使用工具函数

```vue
<template>
  <el-button v-if="canCreateProblem" @click="createProblem">
    创建题目
  </el-button>
  
  <el-button v-if="isAdmin" @click="adminAction">
    管理员操作
  </el-button>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore, PERMISSIONS } from '@/stores/user'

const userStore = useUserStore()

// 使用计算属性
const canCreateProblem = computed(() => 
  userStore.hasPermission(PERMISSIONS.PERM_PROBLEM_CREATE)
)

const isAdmin = computed(() => userStore.isAdmin)

// 或者直接使用 store 的计算属性
const canManageContest = computed(() => userStore.canManageContest)
</script>
```

### 方式3: 使用权限工具函数

```vue
<script setup>
import { hasPermission, hasRole, isAdmin } from '@/utils/permission'
import { PERMISSIONS } from '@/stores/user'

// 检查权限
if (hasPermission(PERMISSIONS.PERM_PROBLEM_CREATE)) {
  // 有创建题目权限
}

// 检查角色
if (hasRole('ADMIN')) {
  // 是管理员
}

// 检查是否是管理员
if (isAdmin()) {
  // 管理员操作
}
</script>
```

## 实际应用场景

### 场景1: 题目列表页面 - 显示创建按钮

```vue
<template>
  <div class="problem-list">
    <el-button 
      v-permission="PERMISSIONS.PERM_PROBLEM_CREATE"
      type="primary" 
      @click="showCreateDialog"
    >
      创建题目
    </el-button>
    
    <el-table :data="problems">
      <!-- 表格内容 -->
    </el-table>
  </div>
</template>

<script setup>
import { PERMISSIONS } from '@/stores/user'
</script>
```

### 场景2: 比赛管理 - 仅管理员可见

```vue
<template>
  <div class="contest-management">
    <!-- 只有比赛管理员或系统管理员可见 -->
    <template v-if="canManageContest || isAdmin">
      <el-button @click="createContest">创建比赛</el-button>
      <el-button @click="editContest">编辑比赛</el-button>
      <el-button @click="deleteContest">删除比赛</el-button>
    </template>
    
    <!-- 所有登录用户可见 -->
    <el-button @click="joinContest">参加比赛</el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore, PERMISSIONS } from '@/stores/user'

const userStore = useUserStore()
const canManageContest = computed(() => userStore.canManageContest)
const isAdmin = computed(() => userStore.isAdmin)
</script>
```

### 场景3: 导航菜单 - 根据权限显示

```vue
<template>
  <el-menu>
    <el-menu-item index="/problems">题目</el-menu-item>
    <el-menu-item index="/contests">比赛</el-menu-item>
    
    <!-- 仅管理员可见的管理菜单 -->
    <el-sub-menu v-if="isAdmin" index="admin">
      <template #title>系统管理</template>
      <el-menu-item index="/admin/users">用户管理</el-menu-item>
      <el-menu-item index="/admin/settings">系统设置</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
</script>
```

### 场景4: 路由守卫 - 权限验证

```javascript
// router/index.js
import { useUserStore } from '@/stores/user'

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 需要管理员权限的路由
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/home')
    return
  }
  
  // 需要创建题目权限的路由
  if (to.meta.requiresPermission) {
    if (!userStore.hasPermission(to.meta.requiresPermission)) {
      next('/home')
      return
    }
  }
  
  next()
})

// 路由配置
{
  path: '/admin/users',
  component: AdminUsers,
  meta: { requiresAdmin: true }
},
{
  path: '/problem/create',
  component: CreateProblem,
  meta: { requiresPermission: PERMISSIONS.PERM_PROBLEM_CREATE }
}
```

## 注意事项

1. **权限数据加载**: 确保在用户登录后正确加载并保存权限信息
2. **前端权限仅是UI控制**: 后端接口仍然需要进行权限验证
3. **权限变更**: 用户权限变更后需要重新登录或刷新页面
4. **默认权限**: 新用户注册后应设置合理的默认权限

## 最佳实践

1. ✅ 始终在后端进行权限验证，前端权限控制仅用于UI展示
2. ✅ 使用常量而非硬编码数字，提高代码可读性
3. ✅ 对于复杂的权限逻辑，抽取为计算属性
4. ✅ 在关键操作前再次确认权限，避免误操作
5. ❌ 不要仅依赖前端权限控制来保护敏感数据

## 示例：完整的权限控制组件

```vue
<template>
  <div class="problem-management">
    <!-- 创建按钮 - 需要创建题目权限 -->
    <el-button 
      v-permission="PERMISSIONS.PERM_PROBLEM_CREATE"
      type="primary"
      @click="handleCreate"
    >
      创建题目
    </el-button>
    
    <!-- 题目列表 -->
    <el-table :data="problems">
      <el-table-column prop="title" label="标题" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <!-- 编辑按钮 - 需要编辑权限或是作者 -->
          <el-button 
            v-if="canEditProblem(row)"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          
          <!-- 删除按钮 - 需要删除权限或是管理员 -->
          <el-button 
            v-if="canDeleteProblem(row)"
            size="small"
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore, PERMISSIONS } from '@/stores/user'

const userStore = useUserStore()

// 检查是否可以编辑题目
const canEditProblem = (problem) => {
  // 有编辑权限或者是作者
  return userStore.hasPermission(PERMISSIONS.PERM_PROBLEM_EDIT) || 
         problem.authorId === userStore.userInfo?.id
}

// 检查是否可以删除题目
const canDeleteProblem = (problem) => {
  // 有删除权限或是管理员或是作者
  return userStore.hasPermission(PERMISSIONS.PERM_PROBLEM_DELETE) ||
         userStore.isAdmin ||
         problem.authorId === userStore.userInfo?.id
}

const handleCreate = () => {
  // 创建题目逻辑
}

const handleEdit = (problem) => {
  // 编辑题目逻辑
}

const handleDelete = (problem) => {
  // 删除题目逻辑
}
</script>
```
