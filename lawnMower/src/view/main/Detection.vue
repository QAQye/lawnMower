<template>
  <div class="detection-container">
    <div class="video-section">
      <div class="section-header">
        <h3>实时监控画面</h3>
        <span :class="['status-dot', isConnected ? 'online' : 'offline']"></span>
        <span class="status-text">{{ statusText }}</span>
      </div>
      
      <div class="video-box">
        <img v-if="videoFrame" :src="videoFrame" alt="实时画面" class="live-video" />
        <div v-else class="placeholder">
          <p>暂无视频信号</p>
        </div>
      </div>
    </div>

    <div class="stats-section">
      <div class="section-header">
        <h3>实时作业统计</h3>
      </div>
      
      <div class="stats-box">
        <div class="stat-card crop-card">
          <div class="card-icon">🌾</div>
          <div class="card-content">
            <div class="card-title">已识别农作物 (Crop)</div>
            <div class="card-number">{{ cropCount }}</div>
          </div>
        </div>

        <div class="stat-card weed-card">
          <div class="card-icon">🌿</div>
          <div class="card-content">
            <div class="card-title">已识别杂草 (Weed)</div>
            <div class="card-number">{{ weedCount }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

const wsUrl = "ws://localhost:8080/ws/stream"; 
const videoFrame = ref<string>('');
const statusText = ref<string>("未连接");
const isConnected = ref<boolean>(false);

// ⭐ 新增：定义计数的响应式变量
const cropCount = ref<number>(0);
const weedCount = ref<number>(0);

let ws: WebSocket | null = null;
let isLeaving = false; 
let reconnectTimer: any = null; 

const connectWebSocket = () => {
  if (ws) return; 
  
  isLeaving = false; 
  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    statusText.value = "已连接 (实时推流中)";
    isConnected.value = true;
    // 每次重新连接（视频重新播放），清空计数器
    cropCount.value = 0;
    weedCount.value = 0;
  };

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      
      // 渲染视频画面
      if (data.image) {
        videoFrame.value = 'data:image/jpeg;base64,' + data.image;
      }

      // ⭐ 解析后端的计数数据
      if (data.counts) {
        cropCount.value = data.counts.crop;
        weedCount.value = data.counts.weed;
      }
    } catch (e) {
      console.error("解析视频流数据失败:", e);
    }
  };

  ws.onclose = () => {
    isConnected.value = false;
    ws = null;
    if (!isLeaving) {
      statusText.value = "连接断开，正在重连...";
      reconnectTimer = setTimeout(connectWebSocket, 3000);
    } else {
      statusText.value = "已断开连接";
    }
  };
};

const disconnectWebSocket = () => {
  isLeaving = true; 
  if (reconnectTimer) clearTimeout(reconnectTimer); 
  if (ws) {
    ws.close(); 
    ws = null;
  }
};

onMounted(() => connectWebSocket());
onBeforeUnmount(() => disconnectWebSocket());
</script>

<style scoped>
/* 视频区域样式不变 */
.detection-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 120px);
  min-height: 500px;
}
.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.section-header h3 {
  margin: 0;
  margin-right: 15px;
  color: #303133;
}
.video-section {
  flex: 7;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.video-box {
  flex: 1;
  background-color: #1e1e1e;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.live-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.placeholder {
  color: #909399;
  font-size: 16px;
  letter-spacing: 2px;
}
.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;
}
.status-dot.online {
  background-color: #67c23a;
  box-shadow: 0 0 5px #67c23a;
}
.status-dot.offline {
  background-color: #f56c6c;
}
.status-text {
  font-size: 14px;
  color: #606266;
}

/* ⭐ 新增的统计看板样式 */
.stats-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.stats-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.crop-card {
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border-left: 6px solid #67c23a;
}
.weed-card {
  background: linear-gradient(135deg, #fff1eb 0%, #ace0f9 100%);
  border-left: 6px solid #f56c6c;
}
.card-icon {
  font-size: 40px;
  margin-right: 20px;
}
.card-content {
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
  font-weight: bold;
}
.card-number {
  font-size: 36px;
  font-weight: 900;
  color: #303133;
}
</style>