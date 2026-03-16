<template>
  <div class="common-layout">
    <div class="header-box">
      <NavHeader />
    </div>
    <div class="main-body">
      <div class="left-nav">
        <LeftNav />
      </div>
      <div class="right-box">
        <Tags />
        <div class="content-box">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import NavHeader from "../../components/NavHeader.vue"; 
import LeftNav from "../../components/LeftNav.vue"; 
import Tags from "../../components/Tags.vue";
</script>

<style scoped>
/* ✅ 关键样式 1：布局容器 */
.common-layout {
  display: flex;
  flex-direction: column; /* 让子元素从上到下排列 */
  height: 100vh;          /* 占满整个屏幕高度 */
  width: 100%; /* ✅ 改成这个 */
  background-color: #f5f7fa; /* 给背景加个灰色，方便看清白色的Header */
  margin: 0;
}

/* 头部区域 */
.header-box {
  background-color: #3f3d3d; /* 头部背景设为白色 */
  border-bottom: 1px solid #dcdfe6; /* 加一条底部分割线 */
  padding: 10px 20px; /* 稍微加点内边距，不要贴着字 */
  color: #fff;

}

.main-body {
  display: flex;       /* 开启 Flex 布局 */
  flex-direction: row; /* ✅ 关键：让内部元素左右横向排列 */
  flex: 1;             /* 占满除去头部之外的所有剩余高度 */
  overflow: hidden;    /* 内部滚动，不让外部滚动 */
}
/* 左侧导航栏区域 */
.left-nav {
  height: 100%;
  background-color: #3f3d3d; /* 给侧边栏加个背景色 */
  border-right: 1px solid #dcdfe6;
  overflow-y: auto; /* 如果菜单太长，允许侧边栏单独滚动 */
}
.content-box {
  flex: 1;          /* ✅ 关键：占满剩余宽度 */
  padding: 20px;
  overflow-y: auto; /* ✅ 关键：如果内容太多，只让内容区域滚动，而不是整个页面滚 */
  background-color: #f5f7fa; 
}

/* ✅ 新增：右侧容器 (用来竖着排 Tags 和 Content) */
.right-box {
  flex: 1; /* 占满右边剩余宽度 */
  display: flex;
  flex-direction: column; /* 竖向排列：上面是 Tags，下面是 Content */
  overflow: hidden; /* 内部防溢出 */
}

/* 内容区域 */
.content-box {
  flex: 1; /* 占满 Tags 剩下的高度 */
  padding: 20px;
  overflow-y: auto; /* ✅ 关键：只有这里会出现滚动条 */
  background-color: #f5f7fa; 
}

/* 简单的淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

</style>