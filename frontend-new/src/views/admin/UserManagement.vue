<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">用户管理</h1>
      <div class="flex space-x-2">
        <button 
          @click="openAddUserDialog" 
          class="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md flex items-center"
        >
          <span class="material-icons mr-1">add</span>
          添加用户
        </button>
        <button 
          @click="handleBatchDelete" 
          :disabled="selectedUsers.length === 0" 
          class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span class="material-icons mr-1">delete</span>
          批量删除
        </button>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="bg-white p-4 rounded-md shadow mb-6">
      <div class="flex items-center">
        <input 
          v-model="searchKeyword" 
          @keyup.enter="fetchUsers"
          type="text" 
          placeholder="搜索用户名/姓名/手机号/邮箱" 
          class="border border-gray-300 rounded-md px-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-orange-500"
        >
        <button 
          @click="fetchUsers" 
          class="ml-2 bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md"
        >
          搜索
        </button>
        <button 
          @click="resetSearch" 
          class="ml-2 bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-md"
        >
          重置
        </button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="bg-white rounded-md shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              <input 
                type="checkbox" 
                :checked="isAllSelected"
                @change="toggleSelectAll"
                class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
              >
            </th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户名</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">角色</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <input 
                type="checkbox" 
                v-model="selectedUsers"
                :value="user.username"
                class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
              >
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.id }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ user.username }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <span 
                class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                :class="{
                  'bg-blue-100 text-blue-800': user.role === 0,
                  'bg-green-100 text-green-800': user.role === 1,
                  'bg-purple-100 text-purple-800': user.role === 2
                }"
              >
                {{ formatRole(user.role) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <el-switch
                v-model="user.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(user)"
                :loading="user.statusLoading"
              />
              <span 
                class="ml-2 px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                :class="{
                  'bg-green-100 text-green-800': user.status === 1,
                  'bg-red-100 text-red-800': user.status === 0
                }"
              >
                {{ user.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(user.createTime) }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button 
                @click="openEditUserDialog(user)" 
                class="text-indigo-600 hover:text-indigo-900 mr-3"
              >
                编辑
              </button>
              <button 
                @click="openDeleteDialog(user)" 
                class="text-red-600 hover:text-red-900"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="7" class="px-6 py-4 text-center text-sm text-gray-500">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="px-6 py-4 flex justify-between items-center border-t border-gray-200">
        <div class="text-sm text-gray-700">
          共 <span class="font-medium">{{ total }}</span> 条记录
        </div>
        <div class="flex space-x-2">
          <button 
            @click="changePage(currentPage - 1)" 
            :disabled="currentPage === 1"
            class="px-3 py-1 rounded-md bg-gray-200 text-gray-700 disabled:opacity-50"
          >
            上一页
          </button>
          <button 
            @click="changePage(currentPage + 1)" 
            :disabled="currentPage * pageSize >= total"
            class="px-3 py-1 rounded-md bg-gray-200 text-gray-700 disabled:opacity-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑用户' : '添加用户'" 
      width="500px"
    >
      <el-form :model="userForm" label-width="80px" :rules="rules" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="普通用户" :value="0"></el-option>
            <el-option label="员工" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog 
      v-model="deleteDialogVisible" 
      title="删除确认" 
      width="400px"
    >
      <p>确定要删除{{ isBatchDelete ? '选中的' + selectedUsers.length + '个' : '该' }}用户吗？此操作不可恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { getUserList, createUser, updateUser, deleteUser, batchDeleteUsers } from '@/api/users';

// 数据列表
const users = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref('');
const selectedUsers = ref([]);

// 对话框控制
const dialogVisible = ref(false);
const deleteDialogVisible = ref(false);
const isEdit = ref(false);
const isBatchDelete = ref(false);
const userToDelete = ref(null);

// 表单相关
const userFormRef = ref(null);
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  role: 'ROLE_USER',
  status: 1
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 计算属性：是否全选
const isAllSelected = computed(() => {
  return users.value.length > 0 && selectedUsers.value.length === users.value.length;
});

// 生命周期钩子
onMounted(() => {
  fetchUsers();
});

// 获取用户列表
const fetchUsers = async () => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '加载中...'
    });
    
    const res = await getUserList({
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    });
    
    loading.close();
    
    if (res.code === 200 && res.data) {
      users.value = res.data.records || [];
      total.value = res.data.total || 0;
      // 清空选中
      selectedUsers.value = [];
    } else {
      ElMessage.error(res.message || '获取用户列表失败');
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  }
};

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  currentPage.value = 1;
  fetchUsers();
};

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > Math.ceil(total.value / pageSize.value)) return;
  currentPage.value = page;
  fetchUsers();
};

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedUsers.value = [];
  } else {
    selectedUsers.value = users.value.map(user => user.id);
  }
};

// 打开添加用户对话框
const openAddUserDialog = () => {
  isEdit.value = false;
  userForm.id = null;
  userForm.username = '';
  userForm.password = '';
  userForm.role = 0; // 修改为数字类型
  userForm.status = 1;
  dialogVisible.value = true;
};

// 打开编辑用户对话框
const openEditUserDialog = (user) => {
  isEdit.value = true;
  // 保持ID的原始值，避免精度丢失
  userForm.id = user.id;
  userForm.username = user.username;
  userForm.password = ''; // 编辑时不修改密码
  // 将字符串类型的角色值转换为整数类型
  if (user.role === 'ROLE_USER') {
    userForm.role = 0;
  } else if (user.role === 'ROLE_STAFF') {
    userForm.role = 1;
  } else if (user.role === 'ROLE_ADMIN') {
    userForm.role = 2;
  } else {
    // 如果是数字类型，直接使用
    userForm.role = typeof user.role === 'number' ? user.role : 0;
  }
  userForm.status = user.status;
  dialogVisible.value = true;
};

// 提交用户表单
const submitUserForm = async () => {
  if (!userFormRef.value) return;
  
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const loading = ElLoading.service({
          lock: true,
          text: isEdit.value ? '更新中...' : '创建中...'
        });
        
        const userData = { ...userForm };
        
        // 不再依赖ID进行更新，而是使用用户名作为主要标识符
        // 这样可以避免JavaScript处理大整数时精度丢失的问题
        // 后端已修改为通过用户名查询用户
        
        let res;
        if (isEdit.value) {
          res = await updateUser(userData);
        } else {
          res = await createUser(userData);
        }
        
        loading.close();
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
          dialogVisible.value = false;
          fetchUsers();
        } else {
          ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'));
        }
      } catch (error) {
        console.error('用户操作失败:', error);
        ElMessage.error(error.message || '操作失败，请重试');
      }
    }
  });
};

// 打开删除对话框
const openDeleteDialog = (user) => {
  isBatchDelete.value = false;
  userToDelete.value = user;
  deleteDialogVisible.value = true;
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请至少选择一个用户');
    return;
  }
  
  isBatchDelete.value = true;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '删除中...'
    });
    
    let res;
    
    if (isBatchDelete.value) {
      res = await batchDeleteUsers(selectedUsers.value);
    } else {
      res = await deleteUser(userToDelete.value.username);
    }
    
    loading.close();
    
    if (res.code === 200) {
      ElMessage.success('删除成功');
      deleteDialogVisible.value = false;
      fetchUsers();
    } else {
      ElMessage.error(res.message || '删除失败');
      deleteDialogVisible.value = false;
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试');
  }
};

// 格式化角色显示
const formatRole = (role) => {
  if (role === 2) return '管理员';
  if (role === 1) return '员工';
  return '普通用户';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 在script setup部分添加
// 处理用户状态切换
const handleStatusChange = async (user) => {
  try {
    user.statusLoading = true;
    const res = await updateUser({
      id: user.id,
      username: user.username,
      role: user.role,
      status: user.status
    });
    
    if (res.code === 200) {
      ElMessage.success(`用户状态${user.status === 1 ? '启用' : '禁用'}成功`);
    } else {
      user.status = user.status === 1 ? 0 : 1; // 切换失败时恢复状态
      ElMessage.error(res.message || '状态更新失败');
    }
  } catch (error) {
    user.status = user.status === 1 ? 0 : 1; // 切换失败时恢复状态
    ElMessage.error('状态更新失败');
  } finally {
    user.statusLoading = false;
  }
};
</script>