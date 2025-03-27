import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrderManagement',
    component: () => import('../views/admin/OrderManagement.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/staff',
    name: 'StaffDashboard',
    component: () => import('../views/staff/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'STAFF' }
  },
  {
    path: '/user',
    name: 'UserDashboard',
    component: () => import('../views/user/Dashboard.vue'),
    meta: { requiresAuth: true, role: 'USER' }
  },
  {
    path: '/user/add-order',
    name: 'AddOrder',
    component: () => import('../views/user/AddOrder.vue'),
    meta: { requiresAuth: true, role: 'USER' }
  },
  {
    path: '/user/query-order',
    name: 'QueryOrder',
    component: () => import('../views/user/QueryOrder.vue'),
    meta: { requiresAuth: true, role: 'USER' }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('../views/user/Profile.vue'),
    meta: { requiresAuth: true, role: 'USER' }
  },
  {
    path: '/admin/profile',
    name: 'AdminProfile',
    component: () => import('../views/admin/Profile.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/admin/logistics',
    name: 'AdminLogisticsManagement',
    component: () => import('../views/admin/LogisticsManagement.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' }
  },
  {
    path: '/staff/profile',
    name: 'StaffProfile',
    component: () => import('../views/staff/Profile.vue'),
    meta: { requiresAuth: true, role: 'STAFF' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  // 将ROLE_XXX格式转换为XXX格式，以便与路由meta中的role匹配
  let normalizedRole = userRole

  // 处理字符串格式的角色
  if (typeof userRole === 'string') {
    normalizedRole = userRole.replace('ROLE_', '')
  }

  // 处理数字格式的角色
  if (userRole === 1 || userRole === '1') {
    normalizedRole = 'ADMIN'
  } else if (userRole === 2 || userRole === '2') {
    normalizedRole = 'STAFF'
  } else if (userRole === 3 || userRole === '3') {
    normalizedRole = 'USER'
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAuth && to.meta.role && normalizedRole) {
    // 不区分大小写比较角色
    const routeRole = to.meta.role.toUpperCase()
    const userRoleUpper = normalizedRole.toUpperCase()

    if (routeRole !== userRoleUpper) {
      // 如果角色不匹配，重定向到登录页
      console.log('角色不匹配:', routeRole, '!=', userRoleUpper)
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router