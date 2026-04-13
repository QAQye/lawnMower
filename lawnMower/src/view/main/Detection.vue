<template>
  <div class="detection-container">
    <div class="video-section">
      <div class="section-header">
        <h3>实时监控画面</h3>
        <span :class="['status-dot', isConnected ? 'online' : 'offline']"></span>
        <span class="status-text">{{ statusText }}</span>
      </div>
      
      <div class="video-box">
        <video
          v-show="isVideoVisible"
          ref="videoElement"
          class="live-video"
          autoplay
          playsinline
          muted
        ></video>

        <div v-if="showPlayOverlay" class="play-overlay" @click="forcePlayVideo">
          <div class="play-button">▶</div>
          <p>点击播放视频</p>
        </div>

        <div v-if="!isVideoVisible" class="placeholder">
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'

import mqtt from 'mqtt'
// ================= 配置区 =================
// 改成你的 Spring Boot 后端地址
const BACKEND_BASE = 'http://localhost:8081'

// 你这页默认查看哪一路流 / 哪台车
const ROBOT_ID = 'robot1'

// 统计轮询间隔
const STATS_POLL_INTERVAL = 1000

// ================= 响应式状态 =================
const videoElement = ref<HTMLVideoElement | null>(null)

const statusText = ref<string>('未连接')
const isConnected = ref<boolean>(false)

const cropCount = ref<number>(0)
const weedCount = ref<number>(0)

const isVideoVisible = ref<boolean>(false)
const showPlayOverlay = ref<boolean>(false)

// ================= MQTT 配置区 =================
const MQTT_WS_URL = 'ws://localhost:8083/mqtt'  // 替换成你的 EMQX 地址
const MQTT_VIDEO_TOPIC = 'frontend/control/video' // 专门用来控制视频流的 topic
let mqttClient: mqtt.MqttClient | null = null

const streamInfo = ref({
  streamName: '',
  rtmpPushUrl: '',
  flvUrl: '',
  hlsUrl: '',
  whepUrl: '',
})

let peerConnection: RTCPeerConnection | null = null
let statsTimer: number | null = null

// ================= 获取流地址 =================
// ================= 获取流地址 =================
const loadStreamInfo = async () => {
  try {
    const res = await axios.get(`${BACKEND_BASE}/api/robot/stream-url`, {
      params: {
        robotId: ROBOT_ID,  // 告诉后端是哪台车
        type: 'ai'          // 告诉后端：我要看带有目标检测框的 AI 流！
      }
    })

    // 这里的赋值逻辑非常清爽，因为后端已经把完整的 "_ai" 地址全拼好了返回给你
    streamInfo.value = {
      streamName: res.data?.streamName || '', 
      rtmpPushUrl: res.data?.rtmpPushUrl || '',
      flvUrl: res.data?.flvUrl || '',
      hlsUrl: res.data?.hlsUrl || '',
      whepUrl: res.data?.whepUrl || '',
    }

    console.log('✅ 获取 AI 检测流地址成功:', streamInfo.value)
  } catch (error) {
    console.error('❌ 获取流地址失败:', error)
    statusText.value = '获取流地址失败'
    isConnected.value = false
    throw error
  }
}

// ================= 销毁 WebRTC =================
const destroyWebRTC = () => {
  if (peerConnection) {
    try {
      peerConnection.ontrack = null
      peerConnection.oniceconnectionstatechange = null
      peerConnection.onconnectionstatechange = null
      peerConnection.close()
    } catch (e) {
      console.warn('销毁 WebRTC 失败:', e)
    }
    peerConnection = null
  }

  if (videoElement.value) {
    try {
      videoElement.value.pause()
      videoElement.value.srcObject = null
      videoElement.value.load()
    } catch (e) {
      console.warn('清理 video 元素失败:', e)
    }
  }

  isVideoVisible.value = false
  showPlayOverlay.value = false
  isConnected.value = false
}

// ================= 初始化 WHEP/WebRTC 拉流 =================
const initWebRTC = async () => {
  if (!videoElement.value) {
    statusText.value = 'video 元素未挂载'
    return
  }

  if (!streamInfo.value.whepUrl) {
    statusText.value = '未获取到 WHEP 地址'
    return
  }

  destroyWebRTC()
  statusText.value = '正在建立视频连接...'
  isConnected.value = false

  try {
    const rtcConfig: RTCConfiguration = {
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    }

    peerConnection = new RTCPeerConnection(rtcConfig)

    peerConnection.addTransceiver('video', { direction: 'recvonly' })

    peerConnection.ontrack = async (event) => {
      console.log('📡 ontrack 触发:', event.track.kind)
      if (event.track.kind !== 'video') return

      const remoteStream = event.streams?.[0]
      if (!remoteStream || !videoElement.value) return

      videoElement.value.srcObject = remoteStream
      isVideoVisible.value = true
      statusText.value = '视频已连接，正在播放...'
      isConnected.value = true

      try {
        await videoElement.value.play()
        showPlayOverlay.value = false
      } catch (e) {
        console.warn('⚠️ 自动播放被拦截:', e)
        showPlayOverlay.value = true
        statusText.value = '视频已连接，请点击播放'
      }
    }

    peerConnection.oniceconnectionstatechange = () => {
      const state = peerConnection?.iceConnectionState
      console.log('🧊 ICE状态:', state)

      if (state === 'failed' || state === 'disconnected' || state === 'closed') {
        statusText.value = `视频连接断开 (${state})`
        isConnected.value = false
        isVideoVisible.value = false
      }
    }

    peerConnection.onconnectionstatechange = () => {
      const state = peerConnection?.connectionState
      console.log('🔗 PeerConnection状态:', state)

      if (state === 'failed') {
        statusText.value = '视频连接失败'
        isConnected.value = false
        isVideoVisible.value = false
      }
    }

    const offer = await peerConnection.createOffer()
    await peerConnection.setLocalDescription(offer)

    const response = await fetch(streamInfo.value.whepUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/sdp'
      },
      body: offer.sdp
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`WHEP 请求失败: HTTP ${response.status}, ${errorText}`)
    }

    const answerSdp = await response.text()

    await peerConnection.setRemoteDescription({
      type: 'answer',
      sdp: answerSdp
    })

    console.log('✅ WHEP 握手完成')
  } catch (error: any) {
    console.error('❌ WebRTC 初始化失败:', error)
    statusText.value = `视频连接失败: ${error.message || error}`
    isConnected.value = false
    isVideoVisible.value = false
  }
}

// ================= 手动播放 =================
const forcePlayVideo = async () => {
  if (!videoElement.value) return

  try {
    await videoElement.value.play()
    showPlayOverlay.value = false
    statusText.value = '视频播放中'
  } catch (error) {
    console.error('❌ 手动播放失败:', error)
    statusText.value = '播放失败，请检查浏览器权限'
  }
}

// ================= 拉取统计 =================
const fetchStats = async () => {
  try {
    const res = await axios.get(`${BACKEND_BASE}/api/robot/detection-stats/latest`, {
      params: {
        robotId: ROBOT_ID
      }
    })

    const data = res.data?.data
    if (data) {
      cropCount.value = Number(data.cropCount || 0)
      weedCount.value = Number(data.weedCount || 0)
    }
  } catch (error) {
    console.error('❌ 获取统计失败:', error)
  }
}

// ================= 启动统计轮询 =================
const startStatsPolling = () => {
  stopStatsPolling()
  fetchStats()

  statsTimer = window.setInterval(() => {
    fetchStats()
  }, STATS_POLL_INTERVAL)
}

// ================= 停止统计轮询 =================
const stopStatsPolling = () => {
  if (statsTimer !== null) {
    clearInterval(statsTimer)
    statsTimer = null
  }
}

// ================= 按需推流控制 =================
const startOnDemandStream = () => {
  mqttClient = mqtt.connect(MQTT_WS_URL)
  
  mqttClient.on('connect', () => {
    console.log('✅ MQTT 已连接，通知小车启动 AI 视频流...')
    const msg = { action: 'start_ai' }
    mqttClient?.publish(MQTT_VIDEO_TOPIC, JSON.stringify(msg), { qos: 1 })
  })
}

const stopOnDemandStream = () => {
  if (mqttClient && mqttClient.connected) {
    console.log('🛑 离开页面，通知小车关闭 AI 视频流...')
    const msg = { action: 'stop_ai' }
    mqttClient.publish(MQTT_VIDEO_TOPIC, JSON.stringify(msg), { qos: 1 })
    
    // 稍微延迟断开连接，确保消息发出去
    setTimeout(() => {
      mqttClient?.end()
    }, 500)
  }
}

// ================= 页面生命周期 =================
onMounted(async () => {
  try {
    startOnDemandStream() // 👈 进页面第一件事：通知小车开机
    await loadStreamInfo()
    await nextTick()
    await initWebRTC()
    startStatsPolling()
  } catch (error) {
    console.error('页面初始化失败:', error)
  }
})

onBeforeUnmount(() => {
  stopStatsPolling()
  destroyWebRTC()
  stopOnDemandStream() // 👈 离开页面：通知小车停机休息
})

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
  position: relative;
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

/* 保持界面风格，只补一个播放覆盖层 */
.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  cursor: pointer;
}
.play-button {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin-bottom: 10px;
}

/* 统计看板样式保持 */
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