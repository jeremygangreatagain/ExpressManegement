<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">员工管理</h1>
      <div class="flex space-x-2">
        <button 
          @click="openAddStaffDialog" 
          class="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md flex items-center"
        >
          <span class="material-icons mr-1">add</span>
          添加员工
        </button>
        <button 
          @click="handleBatchDelete" 
          :disabled="selectedStaffs.length === 0" 
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
          @keyup.enter="fetchStaffs"
          type="text" 
          placeholder="按姓名搜索 (支持用户名/手机号)" 
          class="border border-gray-300 rounded-md px-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-orange-500"
        >
        <button 
          @click="fetchStaffs" 
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

    <!-- 单一员工列表表格 -->
    <div class="bg-white rounded-md shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 table-fixed">
        <thead class="bg-gray-50">
          <tr>
            <!-- Define column widths once in the single header -->
            <th style="width: 5%;" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              <!-- Global Checkbox (Optional - can be added later if needed for all staffs) -->
            </th>
            <th style="width: 10%;" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
            <th style="width: 30%;" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">姓名</th>
            <th style="width: 25%;" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
            <th style="width: 30%;" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <!-- eslint-disable-next-line vue/no-v-for-template-key -->
          <template v-for="store in groupedStaffs" :key="store.id + '-group'"> 
            <!-- Store Group Header Row -->
            <tr class="bg-gray-100"> 
              <td colspan="5" class="px-6 py-3 text-left text-sm font-semibold text-gray-700">
                 <input 
                  type="checkbox" 
                  :checked="isAllSelectedInStore(store.id)"
                  @change="toggleSelectAllInStore(store.id)"
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded mr-2 align-middle" 
                > 
                <span class="align-middle">{{ store.name }}</span> <!-- Wrap text in span for alignment -->
              </td>
            </tr>
            
            <!-- Staff Rows for the current store - Apply key here -->
            <tr v-for="staff in store.staffs" :key="staff.id" class="hover:bg-gray-50"> 
              <td class="px-6 py-4 whitespace-nowrap">
                <input 
                  type="checkbox" 
                  v-model="selectedStaffs"
                  :value="staff.id"
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
                >
              </td>
              <td class="px-6 py-4 whitespace-nowrap overflow-hidden text-sm text-gray-500 text-center">{{ staff.id }}</td> <!-- Apply text-center and px-6 directly to td -->
              <td class="px-6 py-4 whitespace-nowrap overflow-hidden text-ellipsis text-sm font-medium text-gray-900 text-center">{{ staff.name }}</td> <!-- Apply text-center and px-6 directly to td -->
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <div class="flex items-center">
                  <el-switch
                    v-model="staff.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleStatusChange(staff)"
                    :loading="staff.statusLoading"
                    class="flex-shrink-0"
                  />
                  <span 
                    class="ml-2 px-2 inline-flex text-xs leading-5 font-semibold rounded-full flex-shrink-0"
                    :class="{
                      'bg-green-100 text-green-800': staff.status === 1,
                      'bg-red-100 text-red-800': staff.status === 0
                    }"
                  >
                    {{ staff.status === 1 ? '启用' : '禁用' }}
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                 <div class="flex items-center space-x-2">
                    <button 
                      @click="openEditStaffDialog(staff)" 
                      class="text-indigo-600 hover:text-indigo-900 flex-shrink-0"
                    >
                      编辑
                    </button>
                    <button 
                      @click="openDeleteDialog(staff)" 
                      class="text-red-600 hover:text-red-900 flex-shrink-0"
                    >
                      删除
                    </button>
                 </div>
              </td>
            </tr>
             <!-- Row shown if store has no staff -->
            <tr v-if="store.staffs.length === 0">
              <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 italic">该门店暂无员工</td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

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

    <!-- 添加/编辑员工对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑员工' : '添加员工'" 
      width="500px"
    >
      <el-form :model="staffForm" label-width="80px" :rules="rules" ref="staffFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="staffForm.username" :disabled="isEdit"></el-input> <!-- Disable username editing -->
        </el-form-item>
         <el-form-item label="密码" prop="password" v-if="!isEdit"> <!-- Show password only when adding -->
          <el-input v-model="staffForm.password" type="password" show-password></el-input>
        </el-form-item>
         <el-form-item label="姓名" prop="name">
          <el-input v-model="staffForm.name"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="staffForm.phone"></el-input>
        </el-form-item>
        <!-- 移除position字段 -->
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="staffForm.storeId" placeholder="请选择门店">
            <el-option 
              v-for="store in stores" 
              :key="store.id"  
              :label="store.name" 
              :value="store.id"
            ></el-option> <!-- Ensured key is present -->
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="staffForm.status" placeholder="请选择状态">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStaffForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog 
      v-model="deleteDialogVisible" 
      title="删除确认" 
      width="400px"
    >
      <p>确定要删除{{ isBatchDelete ? '选中的' + selectedStaffs.length + '个' : '该' }}员工吗？此操作不可恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// import { formatDate } from '@/utils/date'; // Commented out: File 'date.js' missing
import { getStaffs, addStaff, updateStaff, deleteStaff, batchDeleteStaffs } from '@/api/staff'; // Uncommented: Use real API functions
import { getStoreList } from '@/api/stores'; // Corrected function name to getStoreList

// Removed Placeholder API functions


export default {
  name: 'StaffManagement',
  setup() {
    const staffs = ref([])
    const stores = ref([])
    const selectedStaffs = ref([])
    const searchKeyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const dialogVisible = ref(false)
    const deleteDialogVisible = ref(false)
    const isEdit = ref(false)
    const isBatchDelete = ref(false)
    const staffFormRef = ref(null)
    
    const staffForm = ref({
      id: '',
      username: '', // Added username
      password: '', // Added password
      name: '',
      phone: '',
      // 移除position字段
      storeId: '',
      status: 1
    })
    
    const currentDeleteStaff = ref(null)

    // Custom phone validator
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('请输入手机号'))
      }
      // Basic Chinese mobile number pattern (adjust if needed for international numbers)
      const phoneRegex = /^(?:(?:\+|00)86)?1[3-9]\d{9}$/
      if (!phoneRegex.test(value)) {
        callback(new Error('请输入有效的手机号'))
      } else {
        callback()
      }
    }
    
    const rules = {
      name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { validator: validatePhone, trigger: 'blur' } // Use custom validator
      ],
      username: [ // Added username rules
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [ // Added password rules (only required for add)
         // Conditional rule applied in submitStaffForm or template logic if needed
         // For simplicity, let's make it always required for now, can refine later
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为 6 位', trigger: 'blur' }
      ],
      // 移除position验证规则
      storeId: [{ required: true, message: '请选择门店', trigger: 'change' }]
    }
    
    // 按门店分组员工
    const groupedStaffs = computed(() => {
      console.log('Recalculating groupedStaffs...'); // Log recalculation
      // Use JSON.stringify/parse for deep copy logging to avoid Proxy issues in console
      console.log('Current stores.value:', JSON.parse(JSON.stringify(stores.value))); 
      console.log('Current staffs.value:', JSON.parse(JSON.stringify(staffs.value))); 

      // Add safety check: Ensure stores.value is an array
      if (!Array.isArray(stores.value)) {
        console.warn('stores.value is not an array in groupedStaffs:', stores.value);
        return []; // Return empty array or handle appropriately
      }
      
      const groups = {}
      
      // 初始化所有门店
      stores.value.forEach(store => {
        // Ensure store has an id and name
        if (store && store.id && store.name) {
          groups[store.id] = {
            ...store,
            staffs: []
          };
        } else {
          console.warn('Invalid store object found:', store);
        }
      });
      
      // 分配员工到对应门店
      staffs.value.forEach(staff => {
        // Ensure staff has storeId 
        if (staff && staff.storeId) {
          const storeName = stores.value.find(s => s.id === staff.storeId)?.name || '未知门店';
          const staffWithStore = { ...staff, storeName };

          if (groups[staffWithStore.storeId]) {
            groups[staffWithStore.storeId].staffs.push(staffWithStore);
          } else {
            // If store doesn't exist in initial list (edge case), create a group
            console.warn(`Store with ID ${staffWithStore.storeId} not found in fetched stores. Creating temporary group.`);
            groups[staffWithStore.storeId] = {
              id: staffWithStore.storeId,
              name: storeName, // Use the name we found or '未知门店'
              staffs: [staffWithStore]
            };
          }
        } else {
           console.warn('Invalid staff object or missing storeId:', staff);
        }
      });
      
      const result = Object.values(groups);
      console.log('Grouped staffs result:', JSON.parse(JSON.stringify(result))); // Log grouped result
      return result; //.filter(group => group.staffs.length > 0);
    })
    
    // 检查门店中是否所有员工都被选中
    const isAllSelectedInStore = (storeId) => {
      const store = groupedStaffs.value.find(s => s.id === storeId)
      if (!store || !store.staffs) return false // Add check for staffs array
      
      return store.staffs.length > 0 && 
             store.staffs.every(staff => selectedStaffs.value.includes(staff.id))
    }
    
    // 切换门店中所有员工的选中状态
    const toggleSelectAllInStore = (storeId) => {
      const store = groupedStaffs.value.find(s => s.id === storeId)
      if (!store || !store.staffs) return // Add check for staffs array
      
      const allSelected = isAllSelectedInStore(storeId)
      const staffIdsInStore = store.staffs.map(staff => staff.id);
      
      if (allSelected) {
        // 取消选中该门店所有员工
        selectedStaffs.value = selectedStaffs.value.filter(
          id => !staffIdsInStore.includes(id)
        )
      } else {
        // 选中该门店所有员工 (avoid duplicates)
        const newSelected = staffIdsInStore
          .filter(id => !selectedStaffs.value.includes(id))
        selectedStaffs.value = [...selectedStaffs.value, ...newSelected]
      }
    }
    
    // 获取员工列表
    const fetchStaffs = async () => {
      try {
        const params = {
          keyword: searchKeyword.value,
          page: currentPage.value,
          size: pageSize.value
        }
        
        const res = await getStaffs(params);

        // Assuming response structure is { code: 200, data: { records: [...], total: ... } } based on request interceptor
        const staffList = res.data?.records || []; // Access data nested within res.data
        const totalCount = res.data?.total ?? staffList.length; // Access total nested within res.data

        // Add statusLoading property for UI feedback
        staffs.value = staffList.map(staff => ({ ...staff, statusLoading: false }));
        total.value = totalCount;
        
        console.log('Fetched staffs:', JSON.parse(JSON.stringify(staffs.value))); // Log fetched staffs
        console.log('Fetched total:', total.value); // Log fetched total

      } catch (error) {
        ElMessage.error('获取员工列表失败');
        console.error(error)
        staffs.value = [] // Reset on error
        total.value = 0
      }
    }
    
    // 获取门店列表
    const fetchStores = async () => {
      try {
        // API response structure is { code: 200, data: { records: [...], total: ... } } based on request interceptor
        const res = await getStoreList(); 
        // Correctly assign the array from the 'records' property within res.data
        stores.value = res.data?.records || []; // Access data nested within res.data
        console.log('Fetched stores:', JSON.parse(JSON.stringify(stores.value))); // Log fetched stores
      } catch (error) {
        ElMessage.error('获取门店列表失败');
        console.error(error)
        stores.value = [] // Reset on error
      }
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchKeyword.value = ''
      currentPage.value = 1
      fetchStaffs()
    }
    
    // 切换页码
    const changePage = (page) => {
      // Basic validation
      const maxPage = Math.ceil(total.value / pageSize.value);
      if (page < 1 || page > maxPage) return
      currentPage.value = page
      fetchStaffs()
    }
    
    // 打开添加员工对话框
    const openAddStaffDialog = () => {
      isEdit.value = false
      // Reset form
      staffForm.value = {
        id: '',
        username: '', // Reset username
        password: '', // Reset password
        name: '',
        phone: '',
        // 移除position字段
        storeId: '',
        status: 1 // Default status
      }
      // Clear previous validation messages if any
      staffFormRef.value?.resetFields(); 
      dialogVisible.value = true
    }
    
    // 打开编辑员工对话框
    const openEditStaffDialog = (staff) => {
      isEdit.value = true
      // Populate form with staff data
      staffForm.value = { ...staff } // Use spread to avoid modifying original object directly
      // Clear previous validation messages if any
      staffFormRef.value?.resetFields();
      dialogVisible.value = true
    }
    
    // 打开删除对话框
    const openDeleteDialog = (staff) => {
      currentDeleteStaff.value = staff
      isBatchDelete.value = false
      deleteDialogVisible.value = true
    }
    
    // 处理批量删除
    const handleBatchDelete = () => {
      if (selectedStaffs.value.length === 0) {
         ElMessage.warning('请先选择要删除的员工');
         return;
      }
      isBatchDelete.value = true
      deleteDialogVisible.value = true
    }
    
    // 确认删除
    const confirmDelete = async () => {
      try {
        if (isBatchDelete.value) {
          // 批量删除 (by IDs)
          await batchDeleteStaffs(selectedStaffs.value)
          ElMessage.success('批量删除成功')
          selectedStaffs.value = [] // Clear selection after delete
        } else {
          // 单个删除 (by username - keep this as it was likely correct before)
          // Ensure currentDeleteStaff.value and its username property exist
          if (currentDeleteStaff.value && currentDeleteStaff.value.username) {
             await deleteStaff(currentDeleteStaff.value.username) // Assuming deleteStaff still uses username
             ElMessage.success('删除成功')
          } else {
             console.error('Cannot delete staff: username is missing.', currentDeleteStaff.value);
             ElMessage.error('删除失败：缺少用户名'); // Keep error message consistent
          }
        }
        deleteDialogVisible.value = false
        // Refresh list after delete
        // Consider if staying on the same page or going to page 1 is better
        fetchStaffs() 
      } catch (error) {
        ElMessage.error('删除失败')
        console.error(error)
      }
    }
    
    // 处理状态变更
    const handleStatusChange = async (staff) => {
      // Prevent multiple clicks while processing
      if (staff.statusLoading) return; 
      
      staff.statusLoading = true;
      try {
        // Pass only id and status for update (assuming updateStaff API needs ID)
        // Need to adjust updateStaff API call if it expects username
        await updateStaff({
          id: staff.id,
          username: staff.username, // Keep username if API still needs it for URL
          status: staff.status // Send the new status
        });
        ElMessage.success(`状态更新成功`);
        // Refresh the list to ensure UI consistency after successful update
        fetchStaffs()
      } catch (error) {
        // Revert status on failure
        staff.status = staff.status === 1 ? 0 : 1 
        ElMessage.error(`状态更新失败`)
        console.error(error)
      } finally {
        staff.statusLoading = false
      }
    }
    
    // 提交员工表单
    const submitStaffForm = async () => {
      if (!staffFormRef.value) return
      
      // Validate the form
      await staffFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              // 编辑员工
              await updateStaff(staffForm.value)
              ElMessage.success('更新成功')
            } else {
              // 添加员工
              await addStaff(staffForm.value)
              ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            // Refresh list after add/edit
            fetchStaffs() 
          } catch (error) {
            ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
            console.error(error)
          }
        } else {
          // Form validation failed
          ElMessage.error('请检查表单输入');
          return false;
        }
      })
    }
    
    // 初始化
    onMounted(() => {
      fetchStores() // Fetch stores first for grouping
      fetchStaffs()
    })
    
    return {
      staffs,
      stores,
      selectedStaffs,
      searchKeyword,
      currentPage,
      pageSize,
      total,
      dialogVisible,
      deleteDialogVisible,
      isEdit,
      isBatchDelete,
      staffForm,
      staffFormRef,
      rules,
      groupedStaffs,
      isAllSelectedInStore,
      toggleSelectAllInStore,
      fetchStaffs,
      resetSearch,
      changePage,
      openAddStaffDialog,
      openEditStaffDialog,
      openDeleteDialog,
      changePage,
      openAddStaffDialog,
      openEditStaffDialog,
      openDeleteDialog,
      handleBatchDelete,
      confirmDelete,
      handleStatusChange,
      submitStaffForm,
      // formatDate // Commented out: Function not available yet
    }; // Added semicolon
  }
}
</script>
