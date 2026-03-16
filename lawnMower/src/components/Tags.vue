<template>
  <div class="tags-container">
    <el-scrollbar>
      <div class="tags-wrapper">
        <el-tag
          v-for="(tag, index) in tagsList"
          :key="tag.path"
          :class="{ 'active': isActive(tag.path) }"
          :closable="tag.path !== '/main/index'" 
          :effect="isActive(tag.path) ? 'dark' : 'plain'"
          @click="clickTag(tag)"
          @close="closeTag(index)"
          class="mx-1 tag-item"
        >
          {{ tag.title }}
        </el-tag>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 定义标签的数据结构
interface TagItem {
  title: string;
  path: string;
}

const tagsList = ref<TagItem[]>([
  { title: '首页', path: '/main/index' } // 默认初始只有首页
])

// 判断是否是当前激活的标签
const isActive = (path: string) => {
  return route.path === path
}

// 核心逻辑：监听路由变化，自动添加标签
watch(() => route.path, (newPath) => {
  const isExist = tagsList.value.some(item => item.path === newPath)
  if (!isExist) {
    tagsList.value.push({
      title: (route.meta.title as string) || '未命名', // 获取路由里的 meta.title
      path: newPath
    })
  }
}, { immediate: true })

// 点击标签跳转
const clickTag = (tag: TagItem) => {
  router.push(tag.path)
}

// 关闭标签逻辑
const closeTag = (index: number) => {
  const delItem = tagsList.value[index]
  tagsList.value.splice(index, 1) // 删除

  // 如果关闭的是当前正在看的页面，需要跳到别的地方
  if (isActive(delItem.path)) {
    // 如果还有标签，就跳到最后一个
    const lastTag = tagsList.value[tagsList.value.length - 1]
    router.push(lastTag ? lastTag.path : '/main/index')
  }
}
</script>

<style scoped>
.tags-container {
  height: 40px; /* 固定高度 */
  width: 100%;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  padding: 0 10px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.tags-wrapper {
  display: flex;
  align-items: center;
  height: 40px;
}

.tag-item {
  margin-right: 6px;
  cursor: pointer;
  user-select: none;
}

/* 覆盖 Element Plus 默认样式，让标签更好看 */
.tag-item:not(.active):hover {
  color: #409eff;
  background-color: #ecf5ff;
}

/* 激活状态的样式微调 */
.active {
  border-color: #409eff;
}
:deep(.el-scrollbar__bar.is-vertical) {
  display: none !important;
}
/* ✅ 建议新增：防止内容换行导致的高度溢出 */
/* 确保 el-scrollbar 的包裹层在垂直方向上隐藏溢出 */
:deep(.el-scrollbar__wrap) {
  overflow-y: hidden !important;
}
</style>