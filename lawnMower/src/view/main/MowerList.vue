<template>
  <div class="mower-list-container">
    <div class="toolbar">
      <h3>除草机设备管理</h3>
      <div>
        <el-button type="success" icon="Refresh" @click="fetchData" :loading="loading">刷新状态</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增除草机</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="设备名称" min-width="150" />
      <el-table-column prop="ipAddress" label="IP 地址" width="160" align="center" />
      
      <el-table-column label="在线状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success" effect="dark">在线</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="warning" effect="dark">异常</el-tag>
          <el-tag v-else type="info" effect="dark">离线</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="tasksTime" label="总工作时长(分)" width="150" align="center" />
      <el-table-column prop="createTime" label="录入时间" width="180" align="center" />
      <el-table-column prop="updateTime" label="最后修改" width="180" align="center" />

      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="success" plain @click="goRemote(scope.row)">遥控</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
          
          <el-popconfirm title="确定要删除该除草机吗？" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button size="small" type="danger" plain>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogType === 'add' ? '新增除草机' : '修改除草机'" v-model="dialogVisible" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入除草机名称 (如: 一号草坪机)" />
        </el-form-item>
        <el-form-item label="IP 地址" prop="ipAddress">
          <el-input v-model="formData.ipAddress" placeholder="请输入局域网IP (如: 192.168.1.11)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios'; // 确保你的项目中安装了 axios
import { useRouter } from 'vue-router';
const router = useRouter();

// 假设你登录成功后，把 token 存到了 localStorage 里
const token = localStorage.getItem('token'); 

// 给 Axios 加上请求拦截器
axios.interceptors.request.use(
  config => {
    // 每次发请求前，如果发现有 token，就自动塞到请求头里
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`; 
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 基础配置
const baseUrl = 'http://localhost:8081/api/mower'; // 根据你的后端端口调整
const loading = ref(false);
const tableData = ref([]);

// 弹窗表单相关
const dialogVisible = ref(false);
const dialogType = ref('add'); // 'add' 或 'edit'
const formRef = ref(null);
const formData = ref({
  id: null,
  name: '',
  ipAddress: ''
});

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入除草机名称', trigger: 'blur' }],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/, message: 'IP地址格式不正确', trigger: 'blur' }
  ]
};

// 1. 获取列表数据 (触发后端 Ping)
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await axios.get(`${baseUrl}/list`);
    // 注意：根据你的后端全局返回值配置，这里可能需要 res.data.data
    tableData.value = res.data; 
  } catch (error) {
    ElMessage.error('获取列表失败');
  } finally {
    loading.value = false;
  }
};

// 2. 点击新增按钮
const handleAdd = () => {
  dialogType.value = 'add';
  formData.value = { id: null, name: '', ipAddress: '' };
  dialogVisible.value = true;
};

// 3. 点击编辑按钮
const handleEdit = (row) => {
  dialogType.value = 'edit';
  // 深拷贝数据，防止修改时表格跟着变
  formData.value = { ...row }; 
  dialogVisible.value = true;
};

// 4. 提交表单 (新增或修改)
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await axios.post(`${baseUrl}/add`, formData.value);
          ElMessage.success('新增成功');
        } else {
          await axios.put(`${baseUrl}/update`, formData.value);
          ElMessage.success('修改成功');
        }
        dialogVisible.value = false;
        fetchData(); // 刷新表格
      } catch (error) {
        ElMessage.error('操作失败');
      }
    }
  });
};

// 5. 删除设备
const handleDelete = async (id) => {
  try {
    await axios.delete(`${baseUrl}/delete/${id}`);
    ElMessage.success('删除成功');
    fetchData(); // 刷新表格
  } catch (error) {
    ElMessage.error('删除失败');
  }
};

// 6. 去遥控跳转
// 2. 修改 goRemote 方法
const goRemote = (row) => {
  if (row.status === 0) {
    ElMessage.error('设备已离线，无法遥控！');
    return;
  }
  if (row.status === 3) {
    ElMessage.warning('设备异常（车端程序未启动），可能无法遥控！');
    // 如果你想允许异常状态下强制跳转，可以不加 return
    return; 
  }
  
  ElMessage.success(`正在连接到 ${row.name}...`);
  // 跳转到 Remote 页面，并将 IP 附加在 URL 参数里
  router.push({ path: '/main/remote', query: { ip: row.ipAddress } });
};

// 组件挂载时自动拉取数据
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.mower-list-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h3 {
  margin: 0;
  color: #303133;
}
</style>