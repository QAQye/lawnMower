<template>
  <div class="menu-container">
    
    <div 
      class="toggle-box" 
      :class="{ 'collapsed': isCollapse }" 
      @click="toggleCollapse"
    >
      <el-icon v-if="isCollapse" size="20"><Expand /></el-icon>
      <el-icon v-else size="20"><Fold /></el-icon>
    </div>

    <el-menu
      :default-active="activeIndex"
      class="el-menu-vertical-demo"
      :collapse="isCollapse"
      @select="handleSelect"
      background-color="#3f3d3d" 
      text-color="#fff"
      active-text-color="#ffd04b"
    >
      <el-menu-item index="1">
        <el-icon><icon-menu /></el-icon>
        <template #title>首页</template>
      </el-menu-item>

      <el-menu-item index="2">
        <el-icon><img src="../assets/weed.png" class="custom-icon" /></el-icon>
        <template #title>检测杂草</template>
      </el-menu-item>

      <el-menu-item index="3">
        <el-icon><img src="../assets/control.png" class="custom-icon" /></el-icon>
        <template #title>远程驾驶</template>
      </el-menu-item>

      <!-- <el-menu-item index="4">
        <el-icon><Location /></el-icon>
        <template #title>自动除草</template>
      </el-menu-item> -->

      <el-menu-item index="5">
        <el-icon><img src="../assets/machine.png" class="custom-icon" /></el-icon>
        <template #title>除草机列表</template>
      </el-menu-item>
      
      <el-menu-item index="6">
        <el-icon><img src="../assets/tasklist.png" class="custom-icon" /></el-icon>
        <template #title>任务列表</template>
      </el-menu-item>
      <!-- <el-menu-item index="7">
        <el-icon><QuestionFilled /></el-icon>
        <template #title>AI问答</template>
      </el-menu-item> -->
    </el-menu>
  </div>
</template>

<script lang="ts" setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, computed } from 'vue'
import {
  Document,
  Menu as IconMenu,
  Location,
  Platform,
  Expand,
  Fold,
  QuestionFilled
} from '@element-plus/icons-vue'

const isCollapse = ref(false)
const router = useRouter()
const route = useRoute()

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 这里的 key 必须和上面 template 里的 index 一一对应
const menuMap: Record<string, string> = {
  '1':   '/main/index',
  '2':  '/main/detection',
  '3':   '/main/remote',
  '4':   '/main/auto',
  '5':   '/main/mowerlist',
  '6':   '/main/tasklist',
  '7':   '/main/ai'
}

// 统一处理点击事件
const handleSelect = (key: string) => {
  const path = menuMap[key]
  if (path) {
    router.push(path)
  }
}

// 计算当前高亮项
const activeIndex = computed(() => {
  const currentPath = route.path
  const entry = Object.entries(menuMap).find(([key, val]) => val === currentPath)
  return entry ? entry[0] : '1'
})
</script>

<style scoped>
/* 样式保持不变 */
.menu-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #3f3d3d; 
}

.toggle-box {
  height: 56px;
  display: flex;
  align-items: center;
  color: #fff;
  cursor: pointer;
  justify-content: flex-start;
  padding-left: 20px;
  transition: all 0.3s;
  box-sizing: border-box; 
}

.toggle-box.collapsed {
  justify-content: center;
  padding-left: 0;
}

.toggle-box:hover {
  background-color: #302e2e;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}

.el-menu {
  border-right: none;
}

:deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) {
  background-color: #302e2e;
}
.custom-icon {
  width: 18px;  /* 根据你需要的大小进行微调，通常 18-20px 比较合适 */
  height: 18px;
  object-fit: contain; /* 确保图片完整显示不被拉伸变形 */
}
</style>