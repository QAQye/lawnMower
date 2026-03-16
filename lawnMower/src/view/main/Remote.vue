<template>
  <div class="remote-container">
    <div class="main-panel">
      <div class="side-panel left-panel">
        <h3>远程驾驶控制台</h3>
        <div class="status-box">
          <p>小车状态</p>
          <div class="status-indicator">
            <span :class="['dot', isConnected ? 'dot-green' : 'dot-red']"></span>
            <span :class="{ connected: isConnected }">{{ statusText }}</span>
          </div>
        <el-divider border-style="dashed" style="margin: 15px 0; border-color: #444;" />

        <h4 class="virtual-title">运动控制虚拟摇杆</h4>
        <div class="control-pad">
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('w')">⬆️</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('a')">⬅️</el-button>
            <el-button type="danger" circle size="large" @click="sendCommand('x')" style="font-weight: bold;">M</el-button>
            <el-button type="primary" circle size="large" @click="sendCommand('d')">➡️</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('s')">⬇️</el-button>
          </div>
        </div>
        <h4 class="virtual-title">摄像头虚拟摇杆</h4>
        <div class="control-pad">
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('5')">⬆️</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('1')">⬅️</el-button>
                        <el-button type="danger" circle size="large" @click="sendCommand('x')" style="font-weight: bold;">M</el-button>
            <el-button type="primary" circle size="large" @click="sendCommand('3')">➡️</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('2')">⬇️</el-button>
          </div>
        </div>
        </div>
      </div>

      <div class="video-box">
        <img v-if="videoSrc" :src="videoSrc" alt="Car Camera" class="camera-feed"/>
        <div v-else class="placeholder">
          <span>等待连接摄像头...</span>
        </div>
      </div>

      <div class="side-panel right-panel">
        <h4>按键操作说明</h4>
        <div class="key-instructions">
          <p><span>⬆️ (向上)</span> 前进</p>
          <p><span>⬇️ (向下)</span> 后退</p>
          <p><span>⬅️ (向左)</span> 左转</p>
          <p><span>➡️ (向右)</span> 右转</p>
          <p><span class="key-btn">M</span> 紧急停止</p>
          <p><span class="key-btn">1</span> 摄像头左转</p>
          <p><span class="key-btn">3</span> 摄像头右转</p>
          <p><span class="key-btn">5</span> 摄像头上转</p>
          <p><span class="key-btn">2</span> 摄像头下转</p>
          <el-divider border-style="dashed" style="margin: 15px 0; border-color: #444;" />
        
        <h4 class="virtual-title">功能控制</h4>
        <div class="control-pad">
          <el-button 
            :type="isWeeding ? 'success' : 'warning'" 
            size="large" 
            style="width: 80%; font-weight: bold; font-size: 16px;"
            @click="toggleWeeding">
            <span v-if="!isWeeding">🌿 开启除草电机</span>
            <span v-else>🛑 停止除草电机</span>
          </el-button>
        </div>
          
        </div>
        <p class="hint">注：请先点击页面任意处激活键盘响应</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const socketUrl = "ws://localhost:8080/ws/robot"; // 指向 Java 后端
const videoSrc = ref(null);
const statusText = ref("未连接");
const isConnected = ref(false);
let ws = null;
// 新增：记录除草状态
const isWeeding = ref(false);

// 新增：切换除草状态的专属函数
const toggleWeeding = () => {
  isWeeding.value = !isWeeding.value;
  if (isWeeding.value) {
    sendCommand('c'); // 发送 c 代表开启除草
  } else {
    sendCommand('v'); // 发送 v 代表停止除草
  }
};
// 增加统一的指令发送函数,这样在界面中也可以使用这个二函数来完成指令的发送
const sendCommand = (cmd) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.warn("WebSocket 未连接，无法发送指令");
    return;
  }
  ws.send(cmd);
  console.log("发送控制指令:", cmd);
};
// 初始化 WebSocket
const initWebSocket = () => {
  ws = new WebSocket(socketUrl);

  ws.onopen = () => {
    statusText.value = "已连接 (Java Backend)";
    isConnected.value = true;
    console.log("WebSocket Connected");
  };

  ws.onclose = () => {
    statusText.value = "连接断开";
    isConnected.value = false;
  };

  ws.onerror = (e) => {
    statusText.value = "连接错误";
    console.error("WebSocket Error", e);
  };

  // 接收消息 (主要是视频流)
  ws.onmessage = (event) => {
    // 收到的是 Blob (二进制图片数据)
    const blob = event.data;
    // 创建临时的 URL 指向这个 Blob，赋给 img 标签
    videoSrc.value = URL.createObjectURL(blob);
  };
};

// 键盘按下事件
const handleKeyDown = (e) => {
  console.log("前端按键检测:", e.key);
  if (!ws || ws.readyState !== WebSocket.OPEN) return;

  const key = e.key.toLowerCase();
switch(e.key.toLowerCase()) {
    case 'arrowup':   
      sendCommand('w');
      break;

    case 'arrowdown': 
      sendCommand('s');
      break;

    case 'arrowleft': 
      sendCommand('a');
      break;

    case 'arrowright': 
      sendCommand('d');
      break;
    case 'm':        
      sendCommand('x');
      break;
    // ---- 新增：摄像头云台控制 ----
    case '1':
      sendCommand('1');
      break;
    case '2':
      sendCommand('2');
      break;
    case '3':
      sendCommand('3');
      break;
    case '5':
      sendCommand('5');
      break;
      
  }
};


onMounted(() => {
  initWebSocket();
  // 全局监听键盘
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  if (ws) ws.close();
  window.removeEventListener('keydown', handleKeyDown);
  if (videoSrc.value) URL.revokeObjectURL(videoSrc.value); // 释放内存
});
</script>
<style scoped>
/* 1. 最外层容器 */
.remote-container {
  width: 100%;
  height: calc(100vh - 140px); 
  background-color: #000;
  border-radius: 8px; 
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 2. 主面板：采用横向三列 Flex 布局 */
.main-panel {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
}

/* 3. 左右两侧面板通用样式 */
.side-panel {
  width: 240px; /* 固定宽度，你可以根据需要调宽或调窄 */
  height: 100%;
  background-color: #1e1e1e; /* 比纯黑稍微亮一点的深灰色，区分层级 */
  color: white;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 4. 左侧独有样式 */
.left-panel {
  border-right: 1px solid #333;
}
.left-panel h3 {
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 20px;
  color: #fff;
}
.status-box p {
  color: #aaa;
  font-size: 14px;
  margin-bottom: 10px;
}
.status-indicator {
  display: flex;
  align-items: center;
  font-size: 16px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 10px;
}
.dot-green { background-color: #4ade80; box-shadow: 0 0 8px #4ade80; }
.dot-red { background-color: #ef4444; box-shadow: 0 0 8px #ef4444; }
.connected { color: #4ade80; font-weight: bold; }

/* 5. 中间视频区域：自适应撑满剩余空间 */
.video-box {
  flex: 1; /* 占据除了左右两侧外的所有剩余空间 */
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000;
}
.camera-feed {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 保证画面完整，且绝对不会和文字重叠 */
}
.placeholder {
  color: #555;
  font-size: 20px;
  border: 2px dashed #333;
  padding: 40px;
  border-radius: 10px;
}

/* 6. 右侧独有样式 */
.right-panel {
  border-left: 1px solid #333;
}
.right-panel h4 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 16px;
  color: #ddd;
}
.key-instructions p {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 15px 0;
  font-size: 15px;
  color: #ccc;
}
.key-instructions span {
  background-color: #333;
  padding: 4px 8px;
  border-radius: 4px;
  color: #fff;
  font-weight: bold;
}
.hint {
  margin-top: auto; /* 把提示文字顶到最下面 */
  font-size: 12px;
  color: #666;
  text-align: center;
}
/* ---- 虚拟摇杆样式 ---- */
.virtual-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #ddd;
  text-align: center;
}

.control-pad {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.pad-row {
  display: flex;
  gap: 15px;
  justify-content: center;
}

/* 稍微放大一下 Element 的圆形按钮，手感更好 */
.control-pad .el-button.is-circle {
  width: 50px;
  height: 50px;
  font-size: 18px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.3);
}

/* 调整右侧布局，让提示文字沉底 */
.right-panel {
  display: flex;
  flex-direction: column;
}
.key-instructions p {
  margin: 10px 0; /* 稍微压实一点文字间距给按钮留空间 */
}
</style>