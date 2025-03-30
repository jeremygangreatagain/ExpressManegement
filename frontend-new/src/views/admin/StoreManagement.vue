<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">门店管理</h1>
      <div class="flex space-x-2">
        <button 
          @click="openAddStoreDialog" 
          class="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md flex items-center"
        >
          <span class="material-icons mr-1">add</span>
          添加门店
        </button>
        <button 
          @click="handleBatchDelete" 
          :disabled="selectedStores.length === 0" 
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
          @keyup.enter="fetchStores"
          type="text" 
          placeholder="搜索门店名称/地址/电话" 
          class="border border-gray-300 rounded-md px-4 py-2 w-64 focus:outline-none focus:ring-2 focus:ring-orange-500"
        >
        <button 
          @click="fetchStores" 
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

    <!-- 门店列表 -->
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
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">门店名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">地址</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">联系电话</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="store in stores" :key="store.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <input 
                type="checkbox" 
                v-model="selectedStores"
                :value="store.id"
                class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded"
              >
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ store.id }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ store.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ store.address }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ store.phone }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
              <el-switch
                v-model="store.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(store)"
                :loading="store.statusLoading"
              />
              <span 
                class="ml-2 px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                :class="{
                  'bg-green-100 text-green-800': store.status === 1,
                  'bg-red-100 text-red-800': store.status === 0
                }"
              >
                {{ store.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
              <button 
                @click="openEditStoreDialog(store)" 
                class="text-indigo-600 hover:text-indigo-900 mr-3"
              >
                编辑
              </button>
              <button 
                @click="openDeleteDialog(store)" 
                class="text-red-600 hover:text-red-900"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="stores.length === 0">
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

    <!-- 添加/编辑门店对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑门店' : '添加门店'" 
      width="500px"
    >
      <el-form :model="storeForm" label-width="80px" :rules="rules" ref="storeFormRef">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="storeForm.name"></el-input>
        </el-form-item>
        
        <!-- 省市区三级联动 -->
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="selectedRegion"
            :options="regionOptions"
            :props="{ expandTrigger: 'hover' }"
            @change="handleRegionChange"
            placeholder="请选择省/市/区"
            class="w-full"
          ></el-cascader>
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="storeForm.detailAddress" placeholder="街道、门牌号等"></el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="storeForm.phone"></el-input>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="storeForm.status" placeholder="请选择状态" class="w-full">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStoreForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog 
      v-model="deleteDialogVisible" 
      title="删除确认" 
      width="400px"
    >
      <p>确定要删除{{ isBatchDelete ? '选中的' + selectedStores.length + '个' : '该' }}门店吗？此操作不可恢复。</p>
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
import { getStoreList, createStore, updateStore, deleteStore, batchDeleteStores } from '@/api/stores';
import { regionData, codeToText } from 'element-china-area-data';

// 数据列表
const stores = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref('');
const selectedStores = ref([]);

// 对话框控制
const dialogVisible = ref(false);
const deleteDialogVisible = ref(false);
const isEdit = ref(false);
const isBatchDelete = ref(false);
const storeToDelete = ref(null);

// 地区数据
const regionOptions = ref([]);
const selectedRegion = ref([]);

// 表单相关
const storeFormRef = ref(null);
const storeForm = reactive({
  id: null,
  name: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  address: '',
  phone: '',
  status: 1,
  region: []
});

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$|^\d{3,4}-\d{7,8}$/, message: '请输入正确的电话号码格式', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 计算属性：是否全选
const isAllSelected = computed(() => {
  return stores.value.length > 0 && selectedStores.value.length === stores.value.length;
});

// 生命周期钩子
onMounted(() => {
  fetchStores();
  fetchRegionData();
});

// 获取门店列表
const fetchStores = async () => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '加载中...'
    });
    
    const res = await getStoreList({
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    });
    
    loading.close();
    
    if (res.code === 200 && res.data) {
      stores.value = res.data.records.map(store => {
        if (store.address) {
          const addressParts = store.address.split(' ');
          if (addressParts.length >= 3) {
            const [provinceCode, cityCode, districtCode] = addressParts;
            store.address = `${codeToText[provinceCode]} ${codeToText[cityCode]} ${codeToText[districtCode]} ${addressParts.slice(3).join(' ')}`;
          }
        }
        return store;
      }) || [];
      total.value = res.data.total || 0;
      // 清空选中
      selectedStores.value = [];
    } else {
      ElMessage.error(res.message || '获取门店列表失败');
    }
  } catch (error) {
    ElMessage.error('获取门店列表失败');
  }
};

// 获取省市区数据
const fetchRegionData = () => {
  regionOptions.value = regionData;
};

// 处理地区选择变化
const handleRegionChange = (value) => {
  if (value && value.length === 3) {
    const [province, city, district] = value;
    storeForm.province = province;
    storeForm.city = city;
    storeForm.district = district;
    // 设置region字段，用于表单验证
    storeForm.region = value;
  } else {
    // 清空region字段，确保表单验证能正确识别未选择地区的情况
    storeForm.region = [];
  }
};

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = '';
  currentPage.value = 1;
  fetchStores();
};

// 切换页码
const changePage = (page) => {
  if (page < 1 || page > Math.ceil(total.value / pageSize.value)) return;
  currentPage.value = page;
  fetchStores();
};

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedStores.value = [];
  } else {
    selectedStores.value = stores.value.map(store => store.id);
  }
};

// 打开添加门店对话框
const openAddStoreDialog = () => {
  isEdit.value = false;
  storeForm.id = null;
  storeForm.name = '';
  storeForm.province = '';
  storeForm.city = '';
  storeForm.district = '';
  storeForm.detailAddress = '';
  storeForm.address = '';
  storeForm.phone = '';
  storeForm.status = 1;
  storeForm.region = [];
  selectedRegion.value = [];
  dialogVisible.value = true;
};

// 打开编辑门店对话框
const openEditStoreDialog = (store) => {
  isEdit.value = true;
  storeForm.id = store.id;
  storeForm.name = store.name;
  storeForm.phone = store.phone;
  storeForm.status = store.status;
  
  // 解析地址
  const addressParts = store.address.split(' ');
  if (addressParts.length >= 4) {
    // 从地址中提取省市区编码
    const [provinceCode, cityCode, districtCode] = addressParts;
    
    // 使用codeToText转换为可读名称
    storeForm.province = provinceCode;
    storeForm.city = cityCode;
    storeForm.district = districtCode;
    storeForm.detailAddress = addressParts.slice(3).join(' ');
    
    // 设置级联选择器的值
    selectedRegion.value = [provinceCode, cityCode, districtCode];
    // 设置region字段，用于表单验证
    storeForm.region = [provinceCode, cityCode, districtCode];
  } else if (store.address) {
    // 处理没有省市区编码的地址
    storeForm.detailAddress = store.address;
    selectedRegion.value = [];
    storeForm.region = [];
  } else {
    // 处理空地址的情况
    storeForm.detailAddress = '';
    selectedRegion.value = [];
    storeForm.region = [];
  }
  
  dialogVisible.value = true;
};

// 提交门店表单
const submitStoreForm = async () => {
  if (!storeFormRef.value) return;
  
  storeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const loading = ElLoading.service({
          lock: true,
          text: isEdit.value ? '更新中...' : '创建中...'
        });
        
        // 组合完整地址
        if (storeForm.province && storeForm.city && storeForm.district) {
          storeForm.address = `${storeForm.province} ${storeForm.city} ${storeForm.district} ${storeForm.detailAddress}`;
        } else {
          storeForm.address = storeForm.detailAddress;
        }
        
        const storeData = { ...storeForm };
        
        let res;
        if (isEdit.value) {
          res = await updateStore(storeData);
        } else {
          res = await createStore(storeData);
        }
        
        loading.close();
        
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
          dialogVisible.value = false;
          fetchStores();
        } else {
          ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'));
        }
      } catch (error) {
        console.error('门店操作失败:', error);
        ElMessage.error(error.message || '操作失败，请重试');
      }
    }
  });
};

// 打开删除对话框
const openDeleteDialog = (store) => {
  isBatchDelete.value = false;
  storeToDelete.value = store;
  deleteDialogVisible.value = true;
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedStores.value.length === 0) {
    ElMessage.warning('请至少选择一个门店');
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
      res = await batchDeleteStores(selectedStores.value);
    } else {
      res = await deleteStore(storeToDelete.value.id);
    }
    
    loading.close();
    
    if (res.code === 200) {
      ElMessage.success('删除成功');
      deleteDialogVisible.value = false;
      fetchStores();
    } else {
      ElMessage.error(res.message || '删除失败');
      deleteDialogVisible.value = false;
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试');
  }
};

// 处理门店状态切换
const handleStatusChange = async (store) => {
  try {
    store.statusLoading = true;
    const res = await updateStore({
      id: store.id,
      name: store.name,
      address: store.address,
      phone: store.phone,
      status: store.status
    });
    
    if (res.code === 200) {
      ElMessage.success(`门店状态${store.status === 1 ? '启用' : '禁用'}成功`);
    } else {
      store.status = store.status === 1 ? 0 : 1; // 切换失败时恢复状态
      ElMessage.error(res.message || '状态更新失败');
    }
  } catch (error) {
    store.status = store.status === 1 ? 0 : 1; // 切换失败时恢复状态
    ElMessage.error('状态更新失败');
  } finally {
    store.statusLoading = false;
  }
};
</script>