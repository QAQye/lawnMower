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

      <el-sub-menu index="2">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>功能介绍</span>
        </template>
        <el-menu-item-group>
          <template #title><span>主要功能</span></template>
          <el-menu-item index="2-1">SLAM 定位</el-menu-item>
          <el-menu-item index="2-2">A*避障</el-menu-item>
          <el-menu-item index="2-3">自主导航</el-menu-item>
          <el-menu-item index="2-4">语义分割</el-menu-item>
          <el-menu-item index="2-5">检测杂草</el-menu-item>
        </el-menu-item-group>
      </el-sub-menu>

      <el-menu-item index="3">
        <el-icon><Platform /></el-icon>
        <template #title>远程驾驶</template>
      </el-menu-item>

      <el-menu-item index="4">
        <el-icon><Location /></el-icon>
        <template #title>自动除草</template>
      </el-menu-item>
            <el-menu-item index="5">
        <el-icon><QuestionFilled /></el-icon>
        <template #title>AI问答</template>
      </el-menu-item>
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
  '2-1': '/main/slam',
  '2-2': '/main/astar',
  '2-3': '/main/navigation',
  '2-4': '/main/segmentation',
  '2-5': '/main/detection',
  '3':   '/main/remote',
  '4':   '/main/auto',
  '5':   '/main/ai'
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
</style>