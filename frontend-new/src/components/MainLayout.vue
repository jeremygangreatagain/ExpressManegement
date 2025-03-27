<template>
  <div class="flex h-screen overflow-hidden">
    <!-- 侧边栏 -->
    <Sidebar />
    
    <!-- 主内容区域 -->
    <div class="flex-1 overflow-auto">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './Sidebar.vue';
</script>