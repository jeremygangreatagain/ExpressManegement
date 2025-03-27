<script setup>
// 导入组件
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import ToastManager from './components/ToastManager.vue';
import MainLayout from './components/MainLayout.vue';

// 获取当前路由
const route = useRoute();

// 判断是否需要显示侧边栏布局
const needLayout = computed(() => {
  // 登录页不需要布局
  if (route.path === '/login') return false;
  
  // 检查是否有token
  const hasToken = !!localStorage.getItem('token');
  
  // 有token且不是登录页则显示布局
  return hasToken;
});
</script>

<template>
  <!-- 根据条件决定是否使用布局组件 -->
  <MainLayout v-if="needLayout">
    <router-view />
  </MainLayout>
  
  <!-- 不需要布局时直接显示路由视图 -->
  <router-view v-else />
  
  <!-- 全局Toast管理器 -->
  <ToastManager/>
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
}
</style>
