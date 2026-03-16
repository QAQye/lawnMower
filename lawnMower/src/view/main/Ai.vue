<template>
  <div class="ai-container">
    <el-card class="chat-card">
      <template #header>
        <div class="header">
          <el-icon><ChatLineRound /></el-icon>
          <span style="margin-left: 8px; font-weight: bold;">割草机智能助手</span>
        </div>
      </template>

      <div class="chat-body" ref="chatScroll">
        <div v-for="(item, index) in chatList" :key="index" :class="['msg-row', item.role]">
          <el-avatar 
            :size="32" 
            :icon="item.role === 'user' ? User : Service" 
            :style="{ backgroundColor: item.role === 'user' ? '#f56c6c' : '#409EFF' }" 
            />
            <div v-if="item.role === 'user'" class="msg-content">
                {{ item.content }}
            </div>
            <div v-else class="msg-content markdown-body" v-html="renderMarkdown(item.content)">
            </div>
        </div>
        <div v-if="loading" class="msg-row assistant">
          <el-avatar :size="32" :icon="Service" style="background-color: #409EFF;" />
          <div class="msg-content loading">思考中...</div>
        </div>
      </div>

      <div class="chat-footer">
        <div class="suggestion-box">
          <el-button 
            v-for="(text, index) in suggestionList" 
            :key="index"
            size="small"
            round
            plain
            type="primary"
            :disabled="loading"
            @click="useSuggestion(text)"
          >
            {{ text }}
          </el-button>
        </div>

        <el-input
          v-model="inputMsg"
          placeholder="请输入您的问题..."
          :disabled="loading"
          @keyup.enter="handleSend"
        >
          <template #append>
            <el-button @click="handleSend" :loading="loading" type="primary">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import axios from 'axios'
import { ChatLineRound, User, Cpu, Service } from '@element-plus/icons-vue'
import { marked } from 'marked' 

const inputMsg = ref('')
const loading = ref(false)
const chatScroll = ref<HTMLElement | null>(null)

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

// 🌟 新增：快捷提示词列表
const suggestionList = ref([
  'Slam在单目视频的应用',
  '智能除草机的应用前景',
  'A*避障算法介绍',
  '导航算法NAV介绍'
])

// 🌟 新增：点击快捷提示词，直接发送
const useSuggestion = (text: string) => {
  inputMsg.value = text
  handleSend() // 自动触发发送
}

const renderMarkdown = (text: string) => {
  return marked.parse(text)
}

const chatList = ref<ChatMessage[]>([
  { role: 'assistant', content: '您好！我是您的割草机视觉算法专家。您可以随时点击下方的快捷提示，或者直接向我提问。' }
])

const scrollToBottom = async () => {
  await nextTick()
  if (chatScroll.value) {
    chatScroll.value.scrollTop = chatScroll.value.scrollHeight
  }
}

const handleSend = async () => {
  const rawToken = localStorage.getItem('token') || ''
  if (!inputMsg.value.trim() || loading.value) return

  const userMsg = inputMsg.value
  chatList.value.push({ role: 'user', content: userMsg })
  inputMsg.value = ''
  loading.value = true
  scrollToBottom()

  chatList.value.push({ role: 'assistant', content: '' })
  scrollToBottom()

  try {
    const authHeader = rawToken.startsWith('Bearer ') ? rawToken : `Bearer ${rawToken}`

    const response = await fetch('http://localhost:8080/ai/ask', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'Authorization': authHeader
      },
      body: JSON.stringify({ message: userMsg })
    })

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`)
    }

    loading.value = false 

    const reader = response.body?.getReader()
    const decoder = new TextDecoder('utf-8')
    let done = false
    let buffer = '' 
    const currentMsg = chatList.value[chatList.value.length - 1]

    while (!done && reader) {
      const { value, done: readerDone } = await reader.read()
      done = readerDone
      if (value) {
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' 

        for (const line of lines) {
          const trimmedLine = line.trim()
          
          if (trimmedLine.startsWith('data:')) {
            const dataStr = trimmedLine.substring(5).trim()
            
            if (dataStr === '[DONE]') {
              break 
            }

            try {
              const dataObj = JSON.parse(dataStr)
              if (dataObj.choices && dataObj.choices[0].delta.content) {
                currentMsg.content += dataObj.choices[0].delta.content
                scrollToBottom() 
              }
            } catch (e) {
              console.warn("解析跳过当前片段(属于正常网络拼包):", dataStr)
            }
          }
        }
      }
    }
  } catch (error: any) {
    console.error('AI 流式请求报错:', error)
    chatList.value[chatList.value.length - 1].content = '**[连接失败]** 请检查后端服务是否正常。'
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-container {
  padding: 20px;
  height: 100%; 
  box-sizing: border-box;
  display: flex;
}

.chat-card {
  width: 100%;
  height: 100%; 
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; 
  padding: 20px;
}

:deep(.markdown-body p) { margin: 0 0 10px 0; }
:deep(.markdown-body p:last-child) { margin: 0; }
:deep(.markdown-body pre) {
  background-color: #282c34;
  color: #abb2bf;
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
}
:deep(.markdown-body code) {
  font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 4px;
  border-radius: 4px;
}
:deep(.markdown-body pre code) {
  background-color: transparent;
  padding: 0;
}
:deep(.markdown-body ul), :deep(.markdown-body ol) {
  padding-left: 20px;
  margin: 10px 0;
}

.chat-body {
  flex: 1; 
  overflow-y: auto;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
}

.msg-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.msg-content {
  max-width: 70%;
  margin: 0 12px;
  padding: 10px 15px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.user .msg-content {
  background-color: #ffd04b;
  color: #303133;
  white-space: pre-wrap;
}

.assistant .msg-content {
  background-color: #ffffff;
  color: #303133;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  white-space: normal;
}

.loading {
  color: #909399;
  font-style: italic;
}

.chat-footer {
  margin-top: 20px;
  flex-shrink: 0; 
  display: flex;
  flex-direction: column;
}

/* 🌟 新增：快捷提示区域样式 */
.suggestion-box {
  display: flex;
  flex-wrap: wrap; /* 空间不够会自动换行 */
  gap: 10px;       /* 按钮之间的间距 */
  margin-bottom: 15px; /* 和下方输入框保持距离 */
}

/* 覆盖 Element Plus 按钮自带的左边距，让它靠左对齐更整齐 */
.suggestion-box .el-button {
  margin-left: 0;
}
</style>