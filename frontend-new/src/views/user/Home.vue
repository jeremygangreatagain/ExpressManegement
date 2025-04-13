<template>
  <div class="h-screen bg-gray-100 p-8 flex flex-col"> 
    <div class="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-6xl mx-auto flex-grow"> 
      <!-- Card 1: 发布订单 -->
      <router-link 
        to="/user/add-order" 
        class="bg-[#FF6700] hover:bg-[#E65A00] text-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center transition duration-300 cursor-pointer transform hover:scale-105" 
      >
        <div class="w-32 h-32 bg-[#FFA54F] rounded-full mb-8 flex items-center justify-center"> 
          <!-- Placeholder for icon if needed -->
        </div>
        <h2 class="text-3xl font-bold mb-2">发布订单</h2>
        <p class="text-center text-white">快速发布新的快递订单</p>
      </router-link>

      <!-- Card 2: 查看订单 -->
      <router-link 
        to="/user/query-order" 
        class="bg-[#FF6700] hover:bg-[#E65A00] text-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center transition duration-300 cursor-pointer transform hover:scale-105" 
      >
        <div class="w-32 h-32 bg-[#FFA54F] rounded-full mb-8 flex items-center justify-center"> 
           <!-- Placeholder for icon if needed -->
        </div>
        <h2 class="text-3xl font-bold mb-2">查看订单</h2>
        <p class="text-center text-white">管理和追踪您的订单</p>
      </router-link>
    </div>
  </div>
</template>

<script setup>
// No specific script logic needed for this static dashboard layout
</script>

<style scoped>
/* Add any component-specific styles here if needed */
</style>