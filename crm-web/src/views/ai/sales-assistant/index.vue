<template>
  <div class="chat-container">
    <!-- 侧边栏：对话列表 -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>历史对话</h3>
        <el-button type="primary" size="small" @click="newConversation">新对话</el-button>
      </div>
      <div class="conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: currentConvId === conv.id }"
          @click="switchConversation(conv.id)"
        >
          <div class="conv-title">{{ conv.title }}</div>
          <div class="conv-meta">{{ conv.messageCount }} 条消息</div>
        </div>
        <el-empty v-if="conversations.length === 0" description="暂无对话" :image-size="60" />
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-main">
      <div class="messages-container" ref="messagesRef">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="message-row"
          :class="msg.role"
        >
          <div class="avatar">
            <el-icon v-if="msg.role === 'assistant'" :size="20"><MagicStick /></el-icon>
            <el-icon v-else :size="20"><User /></el-icon>
          </div>
          <div class="message-content" v-html="msg.html || msg.content"></div>
        </div>

        <!-- 流式回复占位 -->
        <div v-if="loading" class="message-row assistant">
          <div class="avatar"><el-icon :size="20"><MagicStick /></el-icon></div>
          <div class="message-content streaming">
            <div v-if="streamingContent" v-html="streamingHtml"></div>
            <span v-else class="typing-dots"><span>.</span><span>.</span><span>.</span></span>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入你的问题，例如：我有哪些客户？"
          :disabled="loading"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <div class="input-actions">
          <span class="hint">按 Enter 发送</span>
          <el-button type="primary" :loading="loading" @click="sendMessage">
            {{ loading ? '思考中...' : '发送' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { User, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { SseClient } from '@/utils/sse'
import { renderMarkdown } from '@/utils/markdown'
import { getConversationList, getConversationMessages } from '@/api/modules/ai'
import { getToken } from '@/utils/auth'

const messagesRef = ref<HTMLElement | null>(null)

const conversations = ref<any[]>([])
const currentConvId = ref<number | undefined>(undefined)
const messages = ref<any[]>([])
const inputMessage = ref('')
const loading = ref(false)
const streamingContent = ref('')
const sseClient = new SseClient()

const streamingHtml = computed(() => renderMarkdown(streamingContent.value))

onMounted(() => {
  loadConversations()
})

async function loadConversations() {
  try {
    const res = await getConversationList('sales_assistant')
    conversations.value = res.data || []
  } catch {
    // ignore
  }
}

async function switchConversation(id: number) {
  currentConvId.value = id
  messages.value = []
  try {
    const res = await getConversationMessages('sales_assistant', id)
    messages.value = (res.data || []).map((m: any) => ({
      ...m,
      html: m.role === 'assistant' ? renderMarkdown(m.content || '') : m.content,
    }))
    await nextTick()
    scrollToBottom()
  } catch {
    // ignore
  }
}

function newConversation() {
  currentConvId.value = undefined
  messages.value = []
  inputMessage.value = ''
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const msg = inputMessage.value.trim()
  if (!msg || loading.value) return

  const token = getToken() || ''
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }

  inputMessage.value = ''
  loading.value = true
  streamingContent.value = ''

  // 添加用户消息
  messages.value.push({ role: 'user', content: msg })
  scrollToBottom()

  let fullContent = ''

  await sseClient.connect(
    '/api/v1/ai/sales-assistant/chat',
    { conversationId: currentConvId.value, message: msg },
    {
      token,
      onConvId: (id) => {
        currentConvId.value = Number(id)
        loadConversations()
      },
      onMessage: (data) => {
        try {
          const parsed = JSON.parse(data)
          if (parsed.content) {
            fullContent += parsed.content
            streamingContent.value = fullContent
            scrollToBottom()
          }
        } catch {
          // raw content
        }
      },
      onDone: () => {
        loading.value = false
        messages.value.push({ role: 'assistant', content: fullContent, html: renderMarkdown(fullContent) })
        streamingContent.value = ''
        loadConversations()
        scrollToBottom()
      },
      onError: (err) => {
        loading.value = false
        ElMessage.error('请求失败: ' + err.message)
      },
    },
  )
}
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: var(--crm-radius-lg);
  overflow: hidden;
  box-shadow: var(--crm-shadow-sm);
}

.chat-sidebar {
  width: 260px;
  border-right: 1px solid var(--crm-border);
  display: flex;
  flex-direction: column;
  background: #fafbfc;

  .sidebar-header {
    padding: var(--crm-space-lg);
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid var(--crm-border);
    h3 { margin: 0; font-size: 15px; }
  }

  .conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: var(--crm-space-sm);
  }

  .conversation-item {
    padding: var(--crm-space-md);
    border-radius: var(--crm-radius-md);
    cursor: pointer;
    margin-bottom: 2px;
    transition: background 0.2s;
    &:hover { background: var(--crm-bg-gray); }
    &.active { background: var(--crm-primary); color: #fff; }
    .conv-title { font-size: 13px; font-weight: 500; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .conv-meta { font-size: 11px; opacity: 0.7; }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--crm-space-lg);
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;

  &.user {
    flex-direction: row-reverse;
    margin-left: auto;
    .avatar { background: var(--crm-primary); color: #fff; }
    .message-content {
      background: var(--crm-primary);
      color: #fff;
      border-radius: 12px 4px 12px 12px;
    }
  }

  &.assistant {
    .avatar { background: #f0f0f0; color: var(--crm-text-regular); }
    .message-content {
      background: var(--crm-bg-gray);
      border-radius: 4px 12px 12px 12px;
    }
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  padding: 12px 16px;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
  min-height: 36px;

  :deep(pre) {
    background: #1e1e1e;
    color: #d4d4d4;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 13px;
    margin: 8px 0;
  }
  :deep(code) {
    background: rgba(0,0,0,0.06);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
  }
  :deep(pre code) {
    background: none;
    padding: 0;
  }
  :deep(ul) { padding-left: 20px; margin: 4px 0; }
  :deep(li) { margin: 2px 0; }
  :deep(strong) { font-weight: 600; }
  :deep(p) { margin: 0 0 8px; &:last-child { margin-bottom: 0; } }
  :deep(h1), :deep(h2), :deep(h3) { margin: 12px 0 6px; font-weight: 600; }
  :deep(h1) { font-size: 17px; }
  :deep(h2) { font-size: 16px; }
  :deep(h3) { font-size: 15px; }
  :deep(a) {
    color: var(--crm-primary);
    text-decoration: underline;
    word-break: break-all;
    &:hover { opacity: 0.8; }
  }
}

.streaming { position: relative; }

.typing-dots span {
  animation: blink 1.4s infinite both;
  font-size: 24px;
  line-height: 1;
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}
@keyframes blink {
  0% { opacity: 0.2; }
  20% { opacity: 1; }
  100% { opacity: 0.2; }
}

.input-area {
  padding: var(--crm-space-lg);
  border-top: 1px solid var(--crm-border);
  background: #fff;

  .input-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    .hint { font-size: 12px; color: var(--crm-text-secondary); }
  }
}
</style>
