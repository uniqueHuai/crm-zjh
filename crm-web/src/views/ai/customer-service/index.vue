<template>
  <page-container title="客服中心" fluid>
    <div class="cs-layout">
      <!-- 左侧：对话列表 -->
      <div class="cs-sidebar">
        <div class="cs-sidebar-header">
          <h3>待处理对话（{{ conversations.length }}）</h3>
          <el-button size="small" @click="loadConversations" :icon="Refresh" circle />
        </div>
        <div class="cs-conversation-list" v-loading="loading">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="cs-conv-item"
            :class="{ active: currentConv?.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <div class="cs-conv-title">{{ conv.title || '客户咨询' }}</div>
            <div class="cs-conv-meta">
              <span class="cs-conv-time">{{ formatTime(conv.updatedAt) }}</span>
              <span class="cs-conv-msgcount">{{ conv.messageCount }} 条</span>
            </div>
            <div class="cs-conv-preview" v-if="conv.lastMessage">{{ conv.lastMessage }}</div>
          </div>
          <el-empty v-if="!loading && conversations.length === 0" description="暂无待处理对话" />
        </div>
      </div>

      <!-- 右侧：聊天区 -->
      <div class="cs-main">
        <template v-if="currentConv">
          <div class="cs-chat-header">
            <span class="cs-chat-title">对话 #{{ currentConv.id }} — {{ currentConv.title || '客户咨询' }}</span>
            <el-button size="small" type="danger" plain @click="closeConversation">关闭对话</el-button>
          </div>

          <div class="cs-messages" ref="messagesRef">
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              class="cs-msg"
              :class="{ 'cs-msg--user': msg.role === 'user', 'cs-msg--staff': msg.staffReply, 'cs-msg--ai': msg.role === 'assistant' && !msg.staffReply }"
            >
              <div class="cs-msg-avatar">
                <el-icon v-if="msg.role === 'user'"><User /></el-icon>
                <el-icon v-else><Service /></el-icon>
              </div>
              <div class="cs-msg-body">
                <div class="cs-msg-role">
                  {{ msg.role === 'user' ? '客户' : (msg.staffReply ? '客服（你）' : 'AI') }}
                </div>
                <div class="cs-msg-content">{{ msg.content }}</div>
              </div>
            </div>
          </div>

          <div class="cs-input-area">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="2"
              placeholder="输入回复内容..."
              @keydown.enter.ctrl="sendReply"
            />
            <div class="cs-input-actions">
              <span class="cs-input-hint">Ctrl+Enter 发送</span>
              <el-button type="primary" :loading="sending" @click="sendReply">发送</el-button>
            </div>
          </div>
        </template>

        <el-empty v-else description="请选择一个对话" :image-size="120" />
      </div>
    </div>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, User, Service } from '@element-plus/icons-vue'
import request from '@/api/request'

const conversations = ref<any[]>([])
const currentConv = ref<any>(null)
const messages = ref<any[]>([])
const replyText = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesRef = ref<HTMLElement | null>(null)

let pollingTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadConversations()
  // 每 10 秒刷新对话列表
  pollingTimer = setInterval(loadConversations, 10000)
})

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer)
})

async function loadConversations() {
  try {
    const res = await request.get('/ai/customer_service/conversations/active')
    const list = res.data || []
    conversations.value = list.filter((c: any) => c.status === 'active')
  } catch {
    // ignore
  }
}

async function selectConversation(conv: any) {
  currentConv.value = conv
  await loadMessages(conv.id)
}

async function loadMessages(convId: number) {
  try {
    const res = await request.get(`/ai/customer_service/conversations/${convId}/messages`)
    messages.value = (res.data || []).map((m: any) => ({
      ...m,
      staffReply: m.staffReply || false,
    }))
    await nextTick()
    scrollToBottom()
  } catch {
    // ignore
  }
}

async function sendReply() {
  const text = replyText.value.trim()
  if (!text || !currentConv.value || sending.value) return

  sending.value = true
  try {
    await request.post(`/ai/customer_service/conversations/${currentConv.value.id}/reply`, {
      content: text,
    })
    replyText.value = ''
    await loadMessages(currentConv.value.id)
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

async function closeConversation() {
  if (!currentConv.value) return
  try {
    await ElMessageBox.confirm('确认关闭此对话？', '提示')
    await request.post(`/ai/customer_service/conversations/${currentConv.value.id}/close`)
    ElMessage.success('已关闭')
    currentConv.value = null
    messages.value = []
    loadConversations()
  } catch {
    // cancelled
  }
}

function formatTime(iso: string) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped lang="scss">
.cs-layout {
  display: flex;
  height: calc(100vh - var(--crm-header-height) - 40px);
  background: var(--crm-bg-white);
  border-radius: var(--crm-radius-lg);
  overflow: hidden;
  border: 1px solid var(--crm-border);
}

/* 左侧对话列表 */
.cs-sidebar {
  width: 320px;
  border-right: 1px solid var(--crm-border);
  display: flex;
  flex-direction: column;
}
.cs-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--crm-border-light);
  h3 { margin: 0; font-size: 15px; }
}
.cs-conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.cs-conv-item {
  padding: 12px;
  border-radius: var(--crm-radius-md);
  cursor: pointer;
  transition: background var(--crm-transition-fast);
  margin-bottom: 4px;
  &:hover, &.active { background: var(--crm-primary-lighter); }
}
.cs-conv-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--crm-text-primary);
  margin-bottom: 4px;
}
.cs-conv-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--crm-text-secondary);
  margin-bottom: 4px;
}
.cs-conv-preview {
  font-size: 12px;
  color: var(--crm-text-placeholder);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧聊天区 */
.cs-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.cs-chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid var(--crm-border-light);
  .cs-chat-title { font-size: 15px; font-weight: 600; }
}
.cs-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.cs-msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  &--user { flex-direction: row-reverse;
    .cs-msg-body { align-items: flex-end; }
    .cs-msg-content { background: var(--crm-primary); color: white; border-radius: 12px 4px 12px 12px; }
  }
  &--staff {
    .cs-msg-content { background: #fff8e1; border-color: #ffe0b2; border-radius: 12px 12px 4px 12px; }
  }
  &--ai {
    .cs-msg-content { background: var(--crm-bg-gray); border-radius: 4px 12px 12px 12px; }
  }
}
.cs-msg-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--crm-bg-gray);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
  color: var(--crm-text-secondary);
}
.cs-msg-body {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}
.cs-msg-role {
  font-size: 11px;
  color: var(--crm-text-secondary);
  margin-bottom: 4px;
}
.cs-msg-content {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  border: 1px solid transparent;
}
.cs-input-area {
  padding: 12px 20px;
  border-top: 1px solid var(--crm-border-light);
  background: var(--crm-bg-gray);
}
.cs-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.cs-input-hint {
  font-size: 12px;
  color: var(--crm-text-placeholder);
}
</style>
