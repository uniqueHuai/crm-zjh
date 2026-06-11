<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">消息中心</h2>
        <p class="page-subtitle">查看系统通知与待办事项，及时处理工作任务</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="handleReadAll"><el-icon><Check /></el-icon>全部标为已读</el-button>
      </div>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab" class="message-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="全部消息" name="all" />
        <el-tab-pane label="未读消息" name="unread" />
      </el-tabs>

      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键字"><el-input v-model="queryParams.keywords" placeholder="搜索消息标题" clearable style="width:180px" /></el-form-item>
            <el-form-item label="消息类型">
              <el-select v-model="queryParams.messageType" placeholder="全部" clearable style="width:130px">
                <el-option label="系统通知" value="system" />
                <el-option label="审批通知" value="approval" />
                <el-option label="公告" value="notice" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ total }}</b> 条消息，未读 <b class="unread-count">{{ unreadTotal }}</b> 条</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="messageList" v-loading="loading" stripe max-height="600" @row-click="handleRowClick">
        <el-table-column label="标题" min-width="260">
          <template #default="{row}">
            <div class="msg-title" :class="{ 'is-unread': !row.readAt }">
              <span class="unread-dot" v-if="!row.readAt" />
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="消息类型" width="110">
          <template #default="{row}">
            <el-tag :type="messageTypeMap[row.messageType]?.type || 'info'" size="small">
              {{ messageTypeMap[row.messageType]?.label || row.messageType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="240">
          <template #default="{row}">
            <span class="text-ellipsis" :title="row.content">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="发送人" width="100" />
        <el-table-column prop="createdAt" label="发送时间" width="170" />
        <el-table-column label="阅读时间" width="170">
          <template #default="{row}">{{ row.readAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button v-if="!row.readAt" link type="primary" size="small" @click.stop="handleMarkRead(row)">标为已读</el-button>
            <el-button link type="primary" size="small" @click.stop="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="消息详情" width="600px" :close-on-click-modal="false">
      <template v-if="detailRow">
        <div class="detail-header">
          <h3 class="detail-title">{{ detailRow.title }}</h3>
          <div class="detail-meta">
            <el-tag :type="messageTypeMap[detailRow.messageType]?.type || 'info'" size="small">
              {{ messageTypeMap[detailRow.messageType]?.label || detailRow.messageType }}
            </el-tag>
            <span class="meta-item">发送人：{{ detailRow.creatorName }}</span>
            <span class="meta-item">发送时间：{{ detailRow.createdAt }}</span>
            <span class="meta-item">阅读时间：{{ detailRow.readAt || '未读' }}</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-content">{{ detailRow.content }}</div>
      </template>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Check } from '@element-plus/icons-vue'
import { getMessagePage, markMessageRead, markAllMessagesRead, getUnreadCount } from '@/api/modules/system'
import { useAppStore } from '@/store/modules/app'

interface Message {
  id: number; title: string; messageType: string; content: string;
  creatorName: string; createdAt: string; readAt?: string;
}

const appStore = useAppStore()

const messageTypeMap: Record<string, {label:string,type:string}> = {
  system: {label:'系统通知',type:'primary'},
  approval: {label:'审批通知',type:'warning'},
  notice: {label:'公告',type:'success'},
}

const loading = ref(false)
const messageList = ref<Message[]>([])
const total = ref(0)
const unreadTotal = ref(0)
const showSearch = ref(true)
const detailVisible = ref(false)
const detailRow = ref<Message | null>(null)
const activeTab = ref('all')

const queryParams = reactive({
  page:1, size:20, keywords:'', messageType:undefined as string|undefined,
  dateRange:undefined as [string,string]|undefined, unreadOnly:false
})

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords:'', messageType:undefined, dateRange:undefined, page:1 })
  fetchData()
}

function handleTabChange() {
  queryParams.unreadOnly = activeTab.value === 'unread'
  fetchData()
}

function openDetail(row: Message) {
  detailRow.value = row
  detailVisible.value = true
  if (!row.readAt) handleMarkRead(row)
}

async function handleMarkRead(row: Message) {
  try {
    await markMessageRead(row.id)
    row.readAt = new Date().toISOString().slice(0, 19).replace('T', ' ')
    // 立即递减全局未读数，不使用异步等待
    if (appStore.unreadCount > 0) appStore.unreadCount--
    ElMessage.success('已标为已读')
  } catch { /* error handled by interceptor */ }
}

async function handleReadAll() {
  try {
    await markAllMessagesRead()
    messageList.value.forEach(m => { (m as any).readAt = new Date().toISOString().slice(0, 19).replace('T', ' ') })
    unreadTotal.value = 0
    // 立即清零全局未读数
    appStore.unreadCount = 0
    ElMessage.success('全部标为已读')
  } catch { /* error handled by interceptor */ }
}

function handleRowClick(row: Message) {
  openDetail(row)
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.unreadOnly) params.isRead = false
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.messageType) params.bizType = queryParams.messageType
    if (queryParams.dateRange && queryParams.dateRange.length === 2) {
      params.dateFrom = queryParams.dateRange[0]
      params.dateTo = queryParams.dateRange[1]
    }

    const [pageRes, unreadRes] = await Promise.all([
      getMessagePage(params),
      getUnreadCount(),
    ])

    if (pageRes?.data) {
      messageList.value = (pageRes.data.records || []).map((item: any) => ({
        id: item.id,
        title: item.title,
        messageType: item.bizType || 'system',
        content: item.content,
        creatorName: item.creatorName || '系统',
        createdAt: item.createdAt,
        readAt: item.readAt,
      }))
      total.value = pageRes.data.total ?? 0
    }
    if (unreadRes?.data) {
      unreadTotal.value = unreadRes.data.total
      // 同步到全局store，保持Navbar小红点一致
      appStore.unreadCount = unreadRes.data.total
    }
  } catch (e) {
    console.error('消息加载失败:', e)
    ElMessage.error('消息数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.sys-page { max-width:1400px; }
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); padding:20px; }
.message-tabs { margin-bottom:12px; }
.search-form { margin-bottom:16px; padding-bottom:16px; border-bottom:1px solid var(--crm-border-light);
  .el-form{display:flex;flex-wrap:wrap;gap:0} :deep(.el-form-item){margin-bottom:0}
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b{color:var(--crm-text-primary);} }
  .unread-count { color:var(--el-color-danger); }
  &-right { display:flex; align-items:center; gap:4px; }
}
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
.text-ellipsis { display:inline-block; max-width:100%; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; vertical-align:bottom; }
.msg-title { display:flex; align-items:center; gap:6px;
  &.is-unread { font-weight:700; color:var(--crm-text-primary); }
}
.unread-dot { width:8px; height:8px; border-radius:50%; background:var(--el-color-danger); flex-shrink:0; }
.detail-header { margin-bottom:8px;
  .detail-title { margin:0 0 12px; font-size:18px; color:var(--crm-text-primary); }
  .detail-meta { display:flex; gap:16px; align-items:center; flex-wrap:wrap; font-size:13px; color:var(--crm-text-secondary); }
  .meta-item { color:var(--crm-text-secondary); }
}
.detail-content { font-size:14px; line-height:1.8; color:var(--crm-text-primary); white-space:pre-wrap; }
</style>
