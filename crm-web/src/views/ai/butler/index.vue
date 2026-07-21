<template>
  <div class="chat-page">
    <!-- 侧边栏 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-header-top">
          <h3>历史对话</h3>
          <el-button type="primary" size="small" @click="newConversation">
            <el-icon><Plus /></el-icon> 新对话
          </el-button>
        </div>
        <div class="sidebar-search">
          <el-input v-model="searchKeyword" placeholder="搜索对话..." size="small" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </div>
      <div class="conversation-list">
        <div
          v-for="conv in filteredConversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: currentConvId === conv.id }"
          @click="switchConversation(conv.id)"
        >
          <div class="conv-icon"><el-icon><ChatDotRound /></el-icon></div>
          <div class="conv-info">
            <div class="conv-title">{{ conv.title || '新对话' }}</div>
            <div class="conv-meta">{{ conv.messageCount || 0 }} 条消息</div>
          </div>
        </div>
        <el-empty v-if="filteredConversations.length === 0" description="暂无对话" :image-size="50" />
      </div>
    </aside>

    <!-- 主区域 -->
    <main class="chat-main">
      <!-- 无对话时的欢迎页 -->
      <template v-if="!currentConvId && messages.length === 0">
        <div class="welcome">
          <div class="welcome-badge">AI</div>
          <h2>智能管家</h2>
          <p class="welcome-desc">全面掌握公司运营数据，智能分析经营状况</p>
          <div class="suggestions">
            <div class="suggestion-card" @click="quickAsk('查看公司整体销售概况')">
              <el-icon><DataAnalysis /></el-icon>
              <span>公司销售概况</span>
            </div>
            <div class="suggestion-card" @click="quickAsk('分析各部门本月业绩')">
              <el-icon><TrendCharts /></el-icon>
              <span>部门业绩分析</span>
            </div>
            <div class="suggestion-card" @click="quickAsk('客户结构和分布情况')">
              <el-icon><User /></el-icon>
              <span>客户结构分析</span>
            </div>
            <div class="suggestion-card" @click="quickAsk('产品线销售排名')">
              <el-icon><ShoppingCart /></el-icon>
              <span>产品线排名</span>
            </div>
          </div>
        </div>
      </template>

      <!-- 对话中的聊天区域 -->
      <template v-else>
        <div class="chat-header">
          <div class="chat-header-info">
            <span class="chat-agent-name">智能管家</span>
            <span class="chat-status online">在线</span>
          </div>
          <div class="chat-header-actions">
            <el-button text size="small" @click="clearMessages">
              <el-icon><Delete /></el-icon> 清除消息
            </el-button>
          </div>
        </div>
        <div class="messages-container" ref="messagesRef">
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            class="message-row"
            :class="msg.role"
          >
            <div class="avatar">
              <el-icon v-if="msg.role === 'assistant'" :size="18"><MagicStick /></el-icon>
              <el-icon v-else :size="18"><User /></el-icon>
            </div>
            <div class="msg-body">
              <div class="msg-sender">{{ msg.role === 'assistant' ? '智能管家' : '你' }}</div>
              <div class="message-content" v-html="msg.html || msg.content"></div>
            </div>
          </div>
          <div v-if="loading" class="message-row assistant">
            <div class="avatar"><el-icon :size="18"><MagicStick /></el-icon></div>
            <div class="msg-body">
              <div class="msg-sender">智能管家</div>
              <div class="message-content streaming">
                <div v-if="streamingContent" v-html="streamingHtml"></div>
                <span v-else class="typing-dots"><span>.</span><span>.</span><span>.</span></span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 底部输入区 -->
      <div class="input-area" :class="{ 'input-welcome': !currentConvId && messages.length === 0 }">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="inputRows"
            :placeholder="currentConvId || messages.length ? '输入你的问题，Enter 发送...' : '开始新的对话，输入你的问题...'"
            :disabled="loading"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <div class="input-toolbar">
            <div class="input-tools-left">
              <span class="input-hint">Enter 发送 · Shift+Enter 换行</span>
            </div>
            <div class="input-tools-right">
              <el-button
                type="primary"
                :loading="loading"
                :disabled="!inputMessage.trim()"
                @click="sendMessage"
                class="btn-send"
              >
                <span v-if="!loading">发送 <el-icon><Position /></el-icon></span>
                <span v-else>思考中...</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import {
  User, MagicStick, Plus, Search, ChatDotRound, TrendCharts,
  DataAnalysis, ShoppingCart, Delete, Position,
} from '@element-plus/icons-vue'
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
const searchKeyword = ref('')
const inputRows = ref(2)
const sseClient = new SseClient()

const streamingHtml = computed(() => renderMarkdown(streamingContent.value))

const filteredConversations = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return conversations.value
  return conversations.value.filter((c: any) =>
    (c.title || '').toLowerCase().includes(kw)
  )
})

onMounted(() => { loadConversations() })

async function loadConversations() {
  try {
    const res = await getConversationList('butler')
    const raw = res.data || []
    conversations.value = Array.isArray(raw) ? raw : (raw.records || [])
  } catch (e) {
    console.warn('[智能管家] 加载对话列表失败', e)
  }
}

async function switchConversation(id: number) {
  currentConvId.value = id
  messages.value = []
  try {
    const res = await getConversationMessages('butler', id)
    const raw = res.data || []
    const list = Array.isArray(raw) ? raw : (raw.records || [])
    messages.value = list.map((m: any) => ({
      ...m,
      html: m.role === 'assistant' ? renderMarkdown(m.content || '') : m.content,
    }))
    await nextTick()
    scrollToBottom()
  } catch { /* ignore */ }
}
function newConversation() {
  currentConvId.value = undefined
  messages.value = []
  inputMessage.value = ''
}

function clearMessages() {
  messages.value = []
}

function quickAsk(text: string) {
  inputMessage.value = text
  sendMessage()
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
  if (!token) { ElMessage.warning('请先登录'); return }
  inputMessage.value = ''
  loading.value = true
  streamingContent.value = ''
  messages.value.push({ role: 'user', content: msg })
  scrollToBottom()

  let fullContent = ''
  await sseClient.connect(
    '/api/v1/ai/butler/chat',
    { conversationId: currentConvId.value, message: msg },
    {
      token,
      onConvId: (id) => { currentConvId.value = Number(id); loadConversations() },
      onMessage: (data) => {
        try {
          const parsed = JSON.parse(data)
          if (parsed.content) { fullContent += parsed.content; streamingContent.value = fullContent; scrollToBottom() }
        } catch { /* raw */ }
      },
      onDone: () => {
        loading.value = false
        messages.value.push({ role: 'assistant', content: fullContent, html: renderMarkdown(fullContent) })
        streamingContent.value = ''
        loadConversations()
        scrollToBottom()
      },
      onError: (err) => { loading.value = false; ElMessage.error('请求失败: ' + err.message) },
    },
  )
}
</script>

<style scoped lang="scss">
// ─── 现代化布局：保持原始浅色主题 ───
$primary: var(--crm-primary, #409eff);
$bg: #f5f7fa;
$card-bg: #fff;
$border: #e8ecf1;
$text: #303133;
$text-secondary: #909399;
$text-light: #c0c4cc;

.chat-page {
  display: flex;
  height: calc(100vh - 120px);
  border-radius: 16px;
  overflow: hidden;
  background: $card-bg;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

// ═══ 侧边栏 ═══
.chat-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid $border;
  background: #fafbfc;

  .sidebar-header {
    padding: 20px 16px 12px;
    border-bottom: 1px solid $border;
    .sidebar-header-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      h3 { margin: 0; font-size: 15px; font-weight: 600; color: $text; }
      :deep(.el-button) { border-radius: 8px; font-size: 12px; }
    }
  }

  .sidebar-search {
    :deep(.el-input__wrapper) {
      background: $bg; border-radius: 8px; box-shadow: none;
      border: 1px solid transparent; transition: all 0.2s;
      &:hover { border-color: $border; }
    }
    :deep(.el-input__inner) { font-size: 13px; }
  }

  .conversation-list {
    flex: 1; overflow-y: auto; padding: 8px;
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: $text-light; border-radius: 4px; }
  }

  .conversation-item {
    display: flex; align-items: center; gap: 10px;
    padding: 10px 12px; border-radius: 10px;
    cursor: pointer; margin-bottom: 2px;
    transition: all 0.2s ease;
    &:hover { background: $bg; }
    &.active { background: rgba($primary, 0.06); .conv-title { color: $primary; font-weight: 600; } }
    .conv-icon {
      width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
      border-radius: 8px; background: $bg; color: $text-secondary; flex-shrink: 0;
    }
    .conv-info { min-width: 0; flex: 1; }
    .conv-title { font-size: 13px; font-weight: 500; color: $text; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .conv-meta { font-size: 11px; color: $text-secondary; margin-top: 2px; }
  }
}

// ═══ 主区域 ═══
.chat-main { flex: 1; display: flex; flex-direction: column; background: $card-bg; position: relative; }

// ─── 欢迎页 ───
.welcome {
  flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 40px; text-align: center;
  .welcome-badge {
    width: 64px; height: 64px; border-radius: 18px;
    background: linear-gradient(135deg, $primary, #66b1ff);
    display: flex; align-items: center; justify-content: center;
    color: #fff; font-size: 24px; font-weight: 700;
    margin-bottom: 20px; box-shadow: 0 8px 24px rgba($primary, 0.2);
  }
  h2 { margin: 0 0 8px; font-size: 22px; font-weight: 700; color: $text; }
  .welcome-desc { margin: 0 0 32px; font-size: 14px; color: $text-secondary; }
  .suggestions { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; max-width: 500px; width: 100%; }
  .suggestion-card {
    display: flex; align-items: center; gap: 10px;
    padding: 14px 18px; border-radius: 12px; border: 1px solid $border;
    cursor: pointer; transition: all 0.2s ease; font-size: 13px; color: $text;
    &:hover { border-color: $primary; background: rgba($primary, 0.03); box-shadow: 0 2px 8px rgba($primary, 0.06); }
    .el-icon { color: $primary; font-size: 18px; }
  }
}

// ─── 聊天头 ───
.chat-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 24px; border-bottom: 1px solid $border; background: $card-bg;
  .chat-header-info { display: flex; align-items: center; gap: 10px; }
  .chat-agent-name { font-size: 14px; font-weight: 600; color: $text; }
  .chat-status { font-size: 11px; padding: 2px 8px; border-radius: 10px; &.online { background: rgba(103, 194, 58, 0.1); color: #67c23a; } }
  .chat-header-actions :deep(.el-button) { color: $text-secondary; font-size: 12px; }
}

// ─── 消息容器 ───
.messages-container {
  flex: 1; overflow-y: auto; padding: 24px 32px; scroll-behavior: smooth;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: $text-light; border-radius: 4px; }
}

// ─── 消息气泡 ───
.message-row {
  display: flex; gap: 12px; margin-bottom: 24px;
  animation: msgIn 0.3s ease;
  @keyframes msgIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
  &:last-child { margin-bottom: 0; }
  .avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 2px; }
  .msg-body { min-width: 0; max-width: 75%; .msg-sender { font-size: 12px; font-weight: 600; color: $text-secondary; margin-bottom: 6px; } }
  &.user {
    flex-direction: row-reverse;
    .avatar { background: rgba($primary, 0.1); color: $primary; }
    .msg-body { text-align: right; }
    .message-content {
      display: inline-block; background: $primary; color: #fff;
      padding: 10px 16px; border-radius: 16px 4px 16px 16px;
      font-size: 14px; line-height: 1.6; text-align: left; max-width: 100%;
    }
  }
  &.assistant {
    .avatar { background: #f0f2f5; color: $text-secondary; }
    .message-content { background: $bg; padding: 12px 16px; border-radius: 4px 16px 16px 16px; font-size: 14px; line-height: 1.7; word-break: break-word; }
  }
}

.message-content {
  :deep(pre) { background: #1e1e1e; color: #d4d4d4; padding: 14px; border-radius: 10px; overflow-x: auto; font-size: 13px; margin: 10px 0; }
  :deep(code) { background: rgba(0, 0, 0, 0.06); padding: 2px 6px; border-radius: 4px; font-size: 13px; color: #d63384; }
  :deep(pre code) { background: none; color: inherit; padding: 0; }
  :deep(ul) { padding-left: 20px; margin: 6px 0; }
  :deep(li) { margin: 3px 0; }
  :deep(p) { margin: 0 0 8px; &:last-child { margin-bottom: 0; } }
  :deep(h1), :deep(h2), :deep(h3) { margin: 14px 0 8px; font-weight: 600; }
  :deep(h1) { font-size: 17px; }
  :deep(h2) { font-size: 16px; }
  :deep(h3) { font-size: 15px; }
  :deep(strong) { font-weight: 600; }
  :deep(a) { color: $primary; text-decoration: underline; word-break: break-all; }
  :deep(table) { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 13px; th, td { padding: 8px 12px; border: 1px solid #e0e0e0; text-align: left; } th { background: #f5f5f5; font-weight: 600; } }
  :deep(blockquote) { border-left: 3px solid $primary; margin: 10px 0; padding: 8px 16px; background: #f8faff; border-radius: 0 8px 8px 0; color: $text-secondary; }
}

.streaming { position: relative; }
.typing-dots span {
  animation: blink 1.4s infinite both; font-size: 24px; line-height: 1;
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}
@keyframes blink { 0% { opacity: 0.2; } 20% { opacity: 1; } 100% { opacity: 0.2; } }

// ─── 输入区 ───
.input-area {
  padding: 16px 24px 20px; border-top: 1px solid $border; background: $card-bg;
  &.input-welcome { border-top: none; background: linear-gradient(180deg, transparent, rgba($bg, 0.5)); }
  .input-wrapper { max-width: 860px; margin: 0 auto; }
  :deep(.el-textarea__inner) {
    border-radius: 12px; border: 1px solid $border; padding: 12px 16px;
    font-size: 14px; line-height: 1.5; resize: none; transition: all 0.2s;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
    &:focus { border-color: $primary; box-shadow: 0 0 0 3px rgba($primary, 0.08); }
  }
  .input-toolbar { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
  .input-hint { font-size: 12px; color: $text-secondary; }
  .btn-send {
    border-radius: 10px; padding: 8px 20px; font-weight: 500; transition: all 0.2s;
    &:not(:disabled):hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba($primary, 0.3); }
    .el-icon { margin-left: 4px; }
  }
}
</style>
