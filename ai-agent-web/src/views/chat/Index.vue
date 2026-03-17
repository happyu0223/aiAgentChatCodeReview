<template>
  <div class="chat-container">
    <el-card class="chat-card">
      <div class="chat-messages" ref="messagesRef">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
        >
          <div class="message-content">{{ msg.content }}</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          placeholder="请输入消息..."
          @keyup.enter="sendMessage"
        >
          <template #append>
            <el-button @click="sendMessage" :loading="loading">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const messages = ref([
  { role: 'assistant', content: '你好！我是AI助手，有什么可以帮助你的吗？' }
])
const inputMessage = ref('')
const loading = ref(false)
const messagesRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    return
  }
  
  const userMsg = { role: 'user', content: inputMessage.value }
  messages.value.push(userMsg)
  inputMessage.value = ''
  
  loading.value = true
  await scrollToBottom()
  
  try {
    if (!sessionId.value) {
      const sessionRes = await request.post('/chat/session/create')
      sessionId.value = sessionRes.data.sessionId
    }
    
    const res = await request.post('/chat/message/send', {
      sessionId: sessionId.value,
      message: userMsg.content
    })
    
    messages.value.push({ role: 'assistant', content: res.data.message })
  } catch (error) {
    ElMessage.error('发送失败: ' + error.message)
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const sessionId = ref('')

onMounted(async () => {
  try {
    const res = await request.get('/chat/session/list')
    if (res.data && res.data.length > 0) {
      sessionId.value = res.data[0].sessionId
    }
  } catch (error) {
    console.error(error)
  }
})
</script>

<style scoped>
.chat-container {
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  margin-bottom: 20px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  word-break: break-word;
}

.message.user .message-content {
  background: #409eff;
  color: white;
}

.message.assistant .message-content {
  background: #f5f7fa;
  color: #333;
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #eee;
}
</style>
