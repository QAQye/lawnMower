<template>
  <el-page-header :icon="ArrowLeft">
    <template #content>
      <span class="text-large"> 除草机管理系统 </span>
    </template>
    <template #extra>
      <div class="right-box">
        <el-avatar 
          :size="32" 
          class="mr-2" 
          src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            {{ username }}
            <!-- <el-icon class="el-icon--right"><arrow-down /></el-icon> -->
          </span>
          
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" >退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </template>
  </el-page-header>
  
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowDown } from '@element-plus/icons-vue' // ✅ 记得引入 ArrowDown
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = ref('User') // 默认名字

// 1. 初始化时，从 localStorage 获取登录时存的用户名（如果有的话）
// 你之前的登录逻辑里还没存 username，通常是 login 接口返回时存一下
onMounted(() => {
  const storedName = localStorage.getItem('username')
  if (storedName) {
    username.value = storedName
  } else {
    username.value = 'Admin' // 假数据演示
  }
})

// 2. 处理下拉菜单的点击事件
const handleCommand = (command: string) => {
  // 退出逻辑：清除 token，跳回登录页
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  ElMessage.success('已退出登录')
  router.push('/login')
  
}
</script>
<style scoped>
/* 调整 Header 整体样式 */
.my-header {
  /* 如果你的字是白色的，背景最好深一点，或者是透明的依靠父级背景 */
  /* background-color: #545c64; */ 
  padding: 0; 
}

/* 标题样式 */
.text-large {
  font-size: 16px;
  font-weight: bold;
  /* color: #333; */ /* 建议先用深色字，除非你的顶栏背景是深色的 */
  color: #fff;
}

/* ✅ 右侧区域布局：让头像和名字横向排列 */
.right-box {
  display: flex;
  align-items: center;
  color: #fff;
  margin-right: 16px;
}

/* 头像右边的间距 */
.mr-2 {
  margin-right: 16px;
}

/* 下拉菜单文字样式 */
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  font-weight: bold;
  color: #fff;
}

/* 鼠标悬停变色 */
.el-dropdown-link:hover {
  color: #409eff;
}

</style>