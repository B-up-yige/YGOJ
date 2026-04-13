import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/home',
      children: [
        {
          path: '/home',
          name: 'Home',
          component: () => import('@/views/Home.vue')
        },
        {
          path: '/problems',
          name: 'ProblemList',
          component: () => import('@/views/problem/ProblemList.vue')
        },
        {
          path: '/problem/:id',
          name: 'ProblemDetail',
          component: () => import('@/views/problem/ProblemDetail.vue')
        },
        {
          path: '/problem/edit/:id',
          name: 'ProblemEdit',
          component: () => import('@/views/problem/ProblemEdit.vue')
        },
        {
          path: '/problem/testcase/:id',
          name: 'TestCaseEdit',
          component: () => import('@/views/problem/TestCaseEdit.vue')
        },
        {
          path: '/problem/create',
          name: 'ProblemCreate',
          component: () => import('@/views/problem/ProblemEdit.vue')
        },
        {
          path: '/records',
          name: 'RecordList',
          component: () => import('@/views/record/RecordList.vue')
        },
        {
          path: '/record/:id',
          name: 'RecordDetail',
          component: () => import('@/views/record/RecordDetail.vue')
        },
        {
          path: '/user/:id',
          name: 'UserInfo',
          component: () => import('@/views/user/UserInfo.vue')
        },
        // 比赛相关路由
        {
          path: '/contests',
          name: 'ContestList',
          component: () => import('@/views/contest/ContestList.vue')
        },
        {
          path: '/contest/:id',
          name: 'ContestDetail',
          component: () => import('@/views/contest/ContestDetail.vue')
        },
        {
          path: '/contest/edit/:id',
          name: 'ContestEdit',
          component: () => import('@/views/contest/ContestEdit.vue')
        },
        {
          path: '/contest/create',
          name: 'ContestCreate',
          component: () => import('@/views/contest/ContestEdit.vue')
        },
        // 题集相关路由
        {
          path: '/problemsets',
          name: 'ProblemsetList',
          component: () => import('@/views/problemset/ProblemsetList.vue')
        },
        {
          path: '/problemset/:id',
          name: 'ProblemsetDetail',
          component: () => import('@/views/problemset/ProblemsetDetail.vue')
        },
        {
          path: '/problemset/edit/:id',
          name: 'ProblemsetEdit',
          component: () => import('@/views/problemset/ProblemsetEdit.vue')
        },
        {
          path: '/problemset/create',
          name: 'ProblemsetCreate',
          component: () => import('@/views/problemset/ProblemsetEdit.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 如果访问登录页或注册页，直接放行
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }
  
  // 如果没有 token，跳转到登录页
  if (!token) {
    next('/login')
    return
  }
  
  next()
})

export default router
