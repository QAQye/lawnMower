<template>
  <div class="remote-page">
    <div class="top-bar">
      <div class="title-wrap">
        <h2>远程驾驶与视频监控</h2>
        <div class="status-line">
          <span class="status-item">目标设备：{{ targetIp || '未选择' }}</span>
          <span class="status-item">控制状态：{{ controlStatusText }}</span>
          <span class="status-item">视频状态：{{ videoStatusText }}</span>
        </div>
      </div>

      <div class="top-actions">
        <el-button type="primary" @click="openSelectDialog">切换设备</el-button>
        <el-button @click="reconnectVideo">重新连接视频</el-button>
        <el-button type="info" @click="router.push('/main/mowerlist')">返回设备列表</el-button>
      </div>
    </div>

    <div class="main-layout">
      <div class="side-panel left-panel">
        <h4 class="virtual-title">小车移动虚拟摇杆</h4>
        <div class="control-pad">
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('w')">⬆</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('a')">⬅</el-button>
            <el-button type="danger" circle size="large" @click="sendCommand('x')" style="font-weight: bold;">M</el-button>
            <el-button type="primary" circle size="large" @click="sendCommand('d')">➡</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('s')">⬇</el-button>
          </div>
        </div>
        <el-divider border-style="dashed" style="margin: 80px 0; border-color: #444;" />
        <h4 class="virtual-title">摄像头虚拟摇杆</h4>
        <div class="control-pad">
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('5')">⬆</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('1')">⬅</el-button>
            <el-button type="danger" circle size="large" @click="sendCommand('x')" style="font-weight: bold;">M</el-button>
            <el-button type="primary" circle size="large" @click="sendCommand('3')">➡</el-button>
          </div>
          <div class="pad-row">
            <el-button type="primary" circle size="large" @click="sendCommand('2')">⬇</el-button>
          </div>
        </div>
      </div>

      <div class="video-box">
        <video
          ref="videoElement"
          class="camera-feed"
          playsinline
          muted
          autoplay
          v-show="isVideoConnected"
        ></video>

        <div class="play-overlay" v-if="showPlayBtn" @click="forcePlayVideo">
          <el-button type="success" size="large" circle>▶</el-button>
          <p style="margin-top: 10px;">点击画面以播放视频</p>
        </div>

        <div v-show="!isVideoConnected" class="placeholder">
          <div class="placeholder-inner">
            <div class="placeholder-title">等待视频连接...</div>
            <div class="placeholder-text" :style="{ color: isError ? '#f56c6c' : '#98a6b5' }">
              {{ videoStatusText }}
            </div>
            <div class="placeholder-text" v-if="streamInfo.whepUrl">
              当前 WHEP：{{ streamInfo.whepUrl }}
            </div>
          </div>
        </div>
      </div>

      <div class="side-panel right-panel">
        <h4>按键操作说明</h4>
        <div class="key-instructions">
          <p><span>⬆</span> 前进</p>
          <p><span>⬇</span> 后退</p>
          <p><span>⬅</span> 左转</p>
          <p><span>➡</span> 右转</p>
          <el-divider border-style="dashed" style="margin: 15px 0; border-color: #444;" />
          <p><span class="key-btn">M</span> 紧急停止</p>
          <p><span class="key-btn">1</span> 摄像头左转</p>
          <p><span class="key-btn">3</span> 摄像头右转</p>
          <p><span class="key-btn">5</span> 摄像头上转</p>
          <p><span class="key-btn">2</span> 摄像头下转</p>
        </div>

        <el-divider border-style="dashed" style="margin: 15px 0; border-color: #444;" />

        <h4 class="virtual-title">功能控制</h4>
        <div class="control-pad">
          <el-button
            :type="isWeeding ? 'success' : 'warning'"
            size="large"
            style="width: 80%; font-weight: bold; font-size: 16px;"
            @click="toggleWeeding"
          >
            <span v-if="!isWeeding">🌿 开启除草电机</span>
            <span v-else>🛑 停止除草电机</span>
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog
      title="选择要查看的视频设备"
      v-model="showSelectDialog"
      width="420px"
      :close-on-click-modal="false"
    >
      <div v-if="onlineMowers.length === 0" style="text-align: center; color: #999;">
        <p>当前没有在线的小车 🚜</p>
        <el-button type="primary" @click="router.push('/main/mowerlist')">返回设备列表</el-button>
      </div>

      <div v-else style="display: flex; flex-direction: column; gap: 10px;">
        <el-button
          v-for="mower in onlineMowers"
          :key="mower.id"
          type="success"
          plain
          style="justify-content: space-between; padding: 20px;"
          @click="handleSelectMower(mower)"
        >
          <span>{{ mower.name }}</span>
          <span>{{ mower.ipAddress }}</span>
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

// ================= 配置区 =================
const BACKEND_BASE = 'http://localhost:8081'
const WS_CONTROL_URL = 'ws://localhost:8081/ws/frontend/control'
const CURRENT_USER_ID = 1
const ROBOT_ID = 'robot1'

// ================= 状态区 =================
const targetIp = ref(route.query.ip || '')
const showSelectDialog = ref(false)
const onlineMowers = ref([])

const videoElement = ref(null)
let peerConnection = null
let currentPlayPromise = null

const controlStatusText = ref('WebSocket 控制未连接')
const videoStatusText = ref('未连接')

const isVideoConnected = ref(false)
const isError = ref(false)
const showPlayBtn = ref(false)
const isWeeding = ref(false)
const isConnectingVideo = ref(false)

const streamInfo = ref({
  streamName: '',
  rtmpPushUrl: '',
  flvUrl: '',
  hlsUrl: '',
  whepUrl: '',
})

// ================= WebSocket 状态区 =================
let controlSocket = null
const wsStatusText = ref('未连接')

// ================= 工具函数 =================
const resetVideoState = () => {
  isVideoConnected.value = false
  isError.value = false
  showPlayBtn.value = false
  videoStatusText.value = '未连接'
}

const destroyWebRTC = () => {
  if (peerConnection) {
    try {
      peerConnection.ontrack = null
      peerConnection.oniceconnectionstatechange = null
      peerConnection.onconnectionstatechange = null
      peerConnection.close()
    } catch (e) {
      console.warn('销毁 WebRTC 失败：', e)
    }
    peerConnection = null
  }

  if (videoElement.value) {
    try {
      const video = videoElement.value
      video.srcObject = null
      video.pause()
      video.removeAttribute('src')
      video.load()
    } catch (e) {
      console.warn('清理 video 元素失败：', e)
    }
  }

  currentPlayPromise = null
  resetVideoState()
}

// ================= WebSocket 逻辑 =================
const connectControlWs = () => {
  if (controlSocket && controlSocket.readyState === WebSocket.OPEN) {
    return
  }

  wsStatusText.value = '连接中...'
  controlStatusText.value = 'WebSocket 连接中...'

  controlSocket = new WebSocket(WS_CONTROL_URL)

  controlSocket.onopen = () => {
    console.log('前端控制 WebSocket 连接成功')
    wsStatusText.value = '已连接'
    controlStatusText.value = 'WebSocket 已连接，可发送控制命令'
  }

  controlSocket.onmessage = (event) => {
    console.log('收到后端 WebSocket 返回消息:', event.data)

    try {
      const data = JSON.parse(event.data)

      if (data.ok) {
        if (data.command) {
          controlStatusText.value = `已发送命令：${data.command}`
        }
      } else {
        controlStatusText.value = data.msg || '控制失败'
        ElMessage.error(data.msg || '控制失败')
      }
    } catch (e) {
      console.warn('后端返回的不是标准 JSON：', e)
    }
  }

  controlSocket.onclose = () => {
    console.log('⚠️ 前端控制 WebSocket 已断开')
    wsStatusText.value = '已断开'
    controlStatusText.value = 'WebSocket 已断开，控制不可用'
  }

  controlSocket.onerror = (err) => {
    console.error('❌ 前端控制 WebSocket 异常:', err)
    wsStatusText.value = '连接异常'
    controlStatusText.value = 'WebSocket 异常，控制不可用'
  }
}

const disconnectControlWs = () => {
  if (controlSocket) {
    try {
      if (controlSocket.readyState === WebSocket.OPEN) {
        controlSocket.close()
      }
      console.log('WebSocket 已主动断开')
    } catch (e) {
      console.warn('断开 WebSocket 失败：', e)
    } finally {
      controlSocket = null
      wsStatusText.value = '未连接'
      controlStatusText.value = 'WebSocket 已断开'
    }
  }
}

const sendWsMessage = (message, successText = '') => {
  if (!controlSocket || controlSocket.readyState !== WebSocket.OPEN) {
    ElMessage.error('WebSocket 未连接，无法发送控制命令')
    controlStatusText.value = 'WebSocket 未连接，发送失败'
    return false
  }

  try {
    const payload = JSON.stringify(message)
    controlSocket.send(payload)
    console.log('✅ WebSocket 消息发送成功:', payload)

    if (successText) {
      controlStatusText.value = successText
    }

    return true
  } catch (err) {
    console.error('❌ WebSocket 消息发送失败:', err)
    ElMessage.error('控制命令发送失败')
    controlStatusText.value = '控制命令发送失败'
    return false
  }
}

const buildCommandMessage = (cmd) => {
  let type = 'move'

  if (['1', '2', '3', '5'].includes(cmd)) {
    type = 'camera'
  } else if (['c', 'v'].includes(cmd)) {
    type = 'weed'
  } else if (['start_cam', 'stop_cam'].includes(cmd)) {
    type = 'video'
  }

  return {
    userId: CURRENT_USER_ID,
    robotId: ROBOT_ID,
    targetIp: targetIp.value || '',
    type,
    command: cmd,
    timestamp: Date.now(),
  }
}

// ================= 获取在线设备 =================
const loadOnlineMowers = async () => {
  try {
    const res = await axios.get(`${BACKEND_BASE}/api/mower/list`)
    const list = Array.isArray(res.data) ? res.data : (res.data?.data || [])
    onlineMowers.value = list.filter(item => item.status === 1)
    showSelectDialog.value = true
  } catch (error) {
    console.error('获取在线设备失败：', error)
    ElMessage.error('获取在线设备列表失败')
  }
}

const openSelectDialog = async () => {
  await loadOnlineMowers()
}

const handleSelectMower = async (mower) => {
  targetIp.value = mower.ipAddress
  showSelectDialog.value = false

  router.replace({
    path: route.path,
    query: { ...route.query, ip: mower.ipAddress }
  })

  sendVideoControl('start_cam')
  await reconnectVideo()
}

// ================= 获取流地址 =================
const loadStreamInfo = async () => {
  try {
    const res = await axios.get(`${BACKEND_BASE}/api/robot/stream-url`, {
      params: {
        robotId: ROBOT_ID,
        type: 'cam'
      }
    })

    streamInfo.value = {
      streamName: res.data?.streamName || '',
      rtmpPushUrl: res.data?.rtmpPushUrl || '',
      flvUrl: res.data?.flvUrl || '',
      hlsUrl: res.data?.hlsUrl || '',
      whepUrl: res.data?.whepUrl || '',
    }

    console.log('已获取纯净摄像头流地址：', streamInfo.value)
    return streamInfo.value
  } catch (error) {
    console.error('获取流地址失败：', error)
    ElMessage.error('获取流地址失败，请检查后端接口')
    throw error
  }
}


// ================= 强制播放处理 =================
const forcePlayVideo = async () => {
  if (!videoElement.value) return
  showPlayBtn.value = false

  try {
    await videoElement.value.play()
    console.log('✅ 视频已手动触发播放')
    videoStatusText.value = '视频播放中'
  } catch (error) {
    console.error('❌ 手动播放失败：', error)
    videoStatusText.value = '浏览器阻止了播放，请检查权限'
    isError.value = true
  }
}

// ================= WebRTC 拉流 =================
const initWebRTC = async () => {
  if (!videoElement.value) {
    console.warn('videoElement 尚未挂载')
    return
  }

  if (!streamInfo.value.whepUrl) {
    videoStatusText.value = '未获取到 WHEP 地址'
    isError.value = true
    return
  }

  videoStatusText.value = '正在建立 WebRTC 连接...'
  isError.value = false

  try {
    const rtcConfig = {
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    }

    peerConnection = new RTCPeerConnection(rtcConfig)
    peerConnection.addTransceiver('video', { direction: 'recvonly' })

    peerConnection.ontrack = async (event) => {
      console.log('触发 ontrack 事件，接收到媒体轨道：', event.track.kind)
      if (event.track.kind !== 'video') return

      const remoteStream = event.streams?.[0]
      if (!remoteStream || !videoElement.value) return

      const video = videoElement.value
      video.srcObject = remoteStream

      isVideoConnected.value = true
      videoStatusText.value = '视频已连接，正在缓冲...'

      try {
        currentPlayPromise = video.play()
        await currentPlayPromise
        console.log('视频自动播放成功')
        videoStatusText.value = '视频播放中'
        showPlayBtn.value = false
      } catch (e) {
        console.warn('自动播放失败:', e)

        if (e?.name === 'AbortError') {
          console.warn('play() 被中断，忽略这次报错')
          return
        }

        videoStatusText.value = '视频准备就绪，请点击画面播放'
        showPlayBtn.value = true
      } finally {
        currentPlayPromise = null
      }
    }

    peerConnection.oniceconnectionstatechange = () => {
      const state = peerConnection?.iceConnectionState
      console.log('🧊 ICE 状态变更为：', state)

      if (state === 'failed' || state === 'disconnected' || state === 'closed') {
        videoStatusText.value = `连接断开 (ICE: ${state})`
        isError.value = true
      }
    }

    peerConnection.onconnectionstatechange = () => {
      const state = peerConnection?.connectionState
      console.log('🔗 PeerConnection 状态变更为：', state)

      if (state === 'failed') {
        videoStatusText.value = '推流端可能已断开'
        isError.value = true
      }
    }

    const offer = await peerConnection.createOffer()
    await peerConnection.setLocalDescription(offer)
    console.log('📡 开始请求 WHEP API：', streamInfo.value.whepUrl)

    const response = await fetch(streamInfo.value.whepUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/sdp' },
      body: offer.sdp
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`HTTP ${response.status}: ${errorText}`)
    }

    const answerSdp = await response.text()
    console.log('✅ WHEP 握手成功，收到 Answer SDP')
    await peerConnection.setRemoteDescription({
      type: 'answer',
      sdp: answerSdp
    })

  } catch (error) {
    console.error('❌ WebRTC 拉流全流程失败：', error)
    videoStatusText.value = `拉流失败: ${error.message}`
    isError.value = true
  }
}

// ================= 重连视频 =================
const reconnectVideo = async () => {
  if (isConnectingVideo.value) {
    console.log('视频正在连接中，跳过重复请求')
    return
  }

  isConnectingVideo.value = true
  try {
    destroyWebRTC()
    await loadStreamInfo()
    await nextTick()
    await initWebRTC()
  } finally {
    isConnectingVideo.value = false
  }
}

// ================= 控制发送逻辑 =================
const sendCommand = (cmd) => {
  const message = buildCommandMessage(cmd)
  sendWsMessage(message, `已发送命令：${cmd}`)
}

const toggleWeeding = () => {
  const nextCommand = isWeeding.value ? 'v' : 'c'
  const message = buildCommandMessage(nextCommand)

  const ok = sendWsMessage(
    message,
    isWeeding.value ? '已发送除草停止命令' : '已发送除草开启命令'
  )

  if (ok) {
    isWeeding.value = !isWeeding.value
    ElMessage.success(isWeeding.value ? '已发送开启除草命令' : '已发送停止除草命令')
  }
}

const sendVideoControl = (action) => {
  const message = buildCommandMessage(action)
  sendWsMessage(message)
}

// ================= 生命周期 =================
onMounted(async () => {
  connectControlWs()

  if (!targetIp.value) {
    await loadOnlineMowers()
  } else {
    sendVideoControl('start_cam')
    await reconnectVideo()
  }
})

onUnmounted(() => {
  sendVideoControl('stop_cam')
  destroyWebRTC()
  disconnectControlWs()
})
</script>

<style scoped>
.remote-page {
  min-height: 100vh;
  background: #101418;
  color: #fff;
  padding: 16px;
  box-sizing: border-box;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #1b2229;
  border-radius: 14px;
}

.title-wrap h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
}

.status-line {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #cdd6df;
  font-size: 14px;
}

.status-item {
  background: #2a333d;
  padding: 6px 10px;
  border-radius: 999px;
}

.top-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.main-layout {
  display: grid;
  grid-template-columns: 280px 1fr 300px;
  gap: 16px;
  align-items: stretch;
}

.side-panel {
  background: #1b2229;
  border-radius: 16px;
  padding: 18px;
  box-sizing: border-box;
}

.video-box {
  position: relative;
  min-height: 600px;
  background: #000;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid #2e3945;
}

.camera-feed {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

.play-overlay .el-button {
  width: 80px;
  height: 80px;
  font-size: 30px;
}

.placeholder {
  width: 100%;
  height: 100%;
  min-height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(180deg, #0f141a 0%, #0a0d10 100%);
}

.placeholder-inner {
  text-align: center;
  color: #d1dae3;
  max-width: 80%;
}

.placeholder-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 12px;
}

.placeholder-text {
  font-size: 14px;
  margin-bottom: 8px;
  word-break: break-all;
}

.virtual-title {
  margin-top: 8px;
  margin-bottom: 30px;
}

.control-pad {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 22px;
}

.pad-row {
  display: flex;
  gap: 10px;
}

.key-instructions p {
  margin: 8px 0;
  color: #d7dee7;
  font-size: 14px;
}

.key-instructions span {
  display: inline-block;
  min-width: 90px;
  color: #8dc5ff;
}

.key-btn {
  display: inline-block;
  min-width: 30px !important;
  text-align: center;
  background: #394554;
  border-radius: 6px;
  padding: 2px 8px;
  color: #fff !important;
}

.stream-card {
  margin-top: 18px;
  padding: 12px;
  background: #11171d;
  border-radius: 12px;
  border: 1px solid #2e3945;
}

.stream-card p {
  margin: 8px 0;
  word-break: break-all;
  color: #d8e1eb;
  font-size: 13px;
}

.hint {
  margin-top: 16px;
  color: #9eaaba;
  font-size: 13px;
}

@media (max-width: 1200px) {
  .main-layout {
    grid-template-columns: 1fr;
  }

  .video-box,
  .placeholder {
    min-height: 420px;
  }
}
</style>