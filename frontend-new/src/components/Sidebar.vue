<template>
  <div class="sidebar-container h-screen bg-white shadow-lg flex flex-col">
    <!-- 系统标题 -->
    <div class="p-4 border-b border-gray-200">
      <h1 class="text-xl font-bold text-orange-500">快递管理系统</h1>
    </div>
    
    <!-- 导航菜单 - 管理员和员工 -->
    <nav v-if="userRole !== 'USER'" class="flex-grow overflow-y-auto py-4">
      <ul class="space-y-1">
        <li v-for="item in filteredMenuItems" :key="item.path">
          <router-link 
            :to="item.path" 
            class="flex items-center px-4 py-3 text-gray-700 hover:bg-orange-50 hover:text-orange-500 transition-colors"
            :class="{ 'bg-orange-50 text-orange-500 border-r-4 border-orange-500': isActive(item.path) }"
          >
            <span class="material-icons mr-3">{{ item.icon }}</span>
            <span>{{ item.title }}</span>
          </router-link>
        </li>
      </ul>
    </nav>
    
    <!-- 普通用户界面 -->
    <div v-if="userRole === 'USER'" class="flex-grow overflow-y-auto py-4">
      <div class="px-4 space-y-4">
        <!-- 首页导航 -->
        <router-link 
          to="/user"
          class="flex items-center px-4 py-3 text-gray-700 hover:bg-orange-50 hover:text-orange-500 transition-colors"
          :class="{ 'bg-orange-50 text-orange-500 border-r-4 border-orange-500': isActive('/user') }"
        >
          <span class="material-icons mr-3">home</span>
          <span>首页</span>
        </router-link>
        
        <!-- 用户操作按钮 -->
        <button 
          @click="navigateTo('/user/add-order')"
          class="flex items-center w-full px-4 py-3 text-gray-700 hover:bg-orange-50 hover:text-orange-500 rounded transition-colors"
        >
          <span class="material-icons mr-3">add_circle</span>
          <span>添加订单</span>
        </button>
        
        <button 
          @click="navigateTo('/user/query-order')"
          class="flex items-center w-full px-4 py-3 text-gray-700 hover:bg-orange-50 hover:text-orange-500 rounded transition-colors"
        >
          <span class="material-icons mr-3">search</span>
          <span>查询订单</span>
        </button>
        
        <!-- 个人信息管理 -->
        <router-link 
          to="/user/profile"
          class="flex items-center px-4 py-3 text-gray-700 hover:bg-orange-50 hover:text-orange-500 transition-colors"
          :class="{ 'bg-orange-50 text-orange-500 border-r-4 border-orange-500': isActive('/user/profile') }"
        >
          <span class="material-icons mr-3">account_circle</span>
          <span>个人信息</span>
        </router-link>
      </div>
    </div>
    
    <!-- 退出登录按钮 -->
    <div class="p-4 border-t border-gray-200">
      <button 
        @click="logout" 
        class="flex items-center w-full px-4 py-2 text-gray-700 hover:bg-orange-50 hover:text-orange-500 rounded transition-colors"
      >
        <span class="material-icons mr-3">logout</span>
        <span>退出登录</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const userRole = ref('');

// 在组件挂载时获取用户角色
onMounted(() => {
  const storedRole = localStorage.getItem('userRole');
  
  // 处理字符串格式的角色
  if (typeof storedRole === 'string') {
    if (storedRole.includes('ADMIN') || storedRole === '1') {
      userRole.value = 'ADMIN';
    } else if (storedRole.includes('STAFF') || storedRole === '2') {
      userRole.value = 'STAFF';
    } else {
      userRole.value = 'USER';
    }
  } else if (storedRole === 1) {
    userRole.value = 'ADMIN';
  } else if (storedRole === 2) {
    userRole.value = 'STAFF';
  } else {
    userRole.value = 'USER';
  }
});

// 管理员菜单项配置
const adminMenuItems = [
  { title: '数据统计', path: '/admin', icon: 'dashboard' },
  { title: '订单管理', path: '/admin/orders', icon: 'receipt_long' },
  { title: '物流事务', path: '/admin/logistics', icon: 'local_shipping' },
  { title: '用户管理', path: '/admin/users', icon: 'people' },
  { title: '员工管理', path: '/admin/staff', icon: 'badge' },
  { title: '门店管理', path: '/admin/stores', icon: 'store' },
  { title: '工作日志', path: '/admin/logs', icon: 'description' },
  { title: '审核', path: '/admin/approvals', icon: 'fact_check' },
  { title: '个人信息', path: '/admin/profile', icon: 'account_circle' }
];

// 员工菜单项配置
const staffMenuItems = [
  { title: '数据统计', path: '/staff', icon: 'dashboard' },
  { title: '订单管理', path: '/staff/orders', icon: 'receipt_long' },
  { title: '物流事务', path: '/staff/logistics', icon: 'local_shipping' },
  { title: '工作日志', path: '/staff/logs', icon: 'description' },
  { title: '个人信息', path: '/staff/profile', icon: 'account_circle' }
];

// 根据用户角色过滤菜单项
const filteredMenuItems = computed(() => {
  if (userRole.value === 'ADMIN') {
    return adminMenuItems;
  } else if (userRole.value === 'STAFF') {
    return staffMenuItems;
  }
  return [];
});

// 导航到指定路径
const navigateTo = (path) => {
  router.push(path);
};

// 判断当前路由是否激活
const isActive = (path) => {
  // 对于首页路径（如/admin），只有完全匹配才算激活
  if (path === '/admin' || path === '/staff' || path === '/user') {
    return route.path === path;
  }
  // 对于其他路径，使用前缀匹配
  return route.path === path || route.path.startsWith(`${path}/`);
};

// 退出登录
const logout = () => {
  // 清除本地存储的token和用户信息
  localStorage.removeItem('token');
  localStorage.removeItem('userRole');
  localStorage.removeItem('username');
  
  // 跳转到登录页
  router.push('/login');
};
</script>

<style scoped>
.sidebar-container {
  width: 250px;
  transition: all 0.3s;
}
</style>