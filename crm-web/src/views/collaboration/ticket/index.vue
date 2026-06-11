<template>
  <div class="ticket-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">服务工单</h2>
        <p class="page-subtitle">管理客户服务请求，跟踪工单处理进度</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增工单</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="标题/客户" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="优先级">
              <el-select v-model="queryParams.priority" placeholder="全部" clearable style="width:120px">
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
                <el-option label="紧急" value="urgent" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="待处理" value="pending" />
                <el-option label="已分配" value="assigned" />
                <el-option label="处理中" value="in_progress" />
                <el-option label="已解决" value="completed" />
              </el-select>
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
          <span class="result-count">共 <b>{{ total }}</b> 个工单</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="ticketList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="ticketNo" label="工单编号" width="140">
          <template #default="{row}"><span class="cell-name">{{ row.ticketNo }}</span></template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{row}">{{ row.title }}</template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" width="110" />
        <el-table-column label="优先级" width="100">
          <template #default="{row}">
            <el-tag :type="priorityMap[row.priority]?.type || 'info'" size="small" effect="dark">
              {{ priorityMap[row.priority]?.label || row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" effect="light">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="100">
          <template #default="{row}">{{ row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 'pending'" link type="primary" size="small" @click="handleAssign(row)">分配</el-button>
            <el-button v-if="row.status === 'in_progress'" link type="success" size="small" @click="handleComplete(row)">处理完成</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="标题" prop="title" :rules="[{required:true,message:'请输入工单标题'}]">
          <el-input v-model="form.title" placeholder="工单标题" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerName" :rules="[{required:true,message:'请输入客户名称'}]">
              <el-input v-model="form.customerName" placeholder="客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" style="width:100%">
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
                <el-option label="紧急" value="urgent" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="问题描述" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import {
  getTicketPage, createTicket, updateTicket,
  deleteTicket, assignTicket, completeTicket,
} from '@/api/modules/collaboration'

interface Ticket {
  id: number; ticketNo: string; title: string;
  customerName: string; priority: string; status: string;
  assigneeName: string; description?: string; createdAt: string;
}

const priorityMap: Record<string, {label:string;type:string}> = {
  low: { label: '低', type: 'info' },
  medium: { label: '中', type: 'primary' },
  high: { label: '高', type: 'warning' },
  urgent: { label: '紧急', type: 'danger' },
}

const statusMap: Record<string, {label:string;type:string}> = {
  pending: { label: '待处理', type: 'info' },
  assigned: { label: '已分配', type: 'primary' },
  accepted: { label: '已接单', type: 'primary' },
  in_progress: { label: '处理中', type: 'warning' },
  completed: { label: '已解决', type: 'success' },
  rated: { label: '已评价', type: 'default' },
}

const loading = ref(false)
const ticketList = ref<Ticket[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Ticket | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  priority: undefined as string | undefined,
  status: undefined as string | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑工单' : '新增工单')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', priority: undefined, status: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: Ticket) { editingRow.value = row || null; dialogVisible.value = true }

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateTicket(editingRow.value.id, {
        title: formData.title,
        description: formData.description || '',
        priority: formData.priority || 'medium',
        customerName: formData.customerName || '',
      })
    } else {
      await createTicket({
        customerId: 0,
        type: 'service',
        title: formData.title,
        description: formData.description || '',
        priority: formData.priority || 'medium',
        source: 'manual',
      })
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { /* handled by interceptor */ }
}

function handleAssign(row: Ticket) {
  ElMessageBox.prompt('请输入处理人用户ID', '分配工单', { confirmButtonText: '确定', cancelButtonText: '取消', inputType: 'textarea' })
    .then(async ({ value }) => {
      try {
        await assignTicket(row.id, Number(value) || 0)
        ElMessage.success('已分配')
        fetchData()
      } catch { /* handled by interceptor */ }
    }).catch(() => {})
}

function handleComplete(row: Ticket) {
  ElMessageBox.confirm(`确认工单「${row.ticketNo}」已处理完成？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'success',
  }).then(async () => {
    try {
      await completeTicket(row.id)
      ElMessage.success('工单状态已更新为已解决')
      fetchData()
    } catch { /* handled by interceptor */ }
  }).catch(() => {})
}

async function handleDelete(row: Ticket) {
  try {
    await deleteTicket(row.id)
    ElMessage.success(`已删除工单「${row.ticketNo}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.priority) params.priority = queryParams.priority
    if (queryParams.status) params.status = queryParams.status

    const res = await getTicketPage(params)
    if (res?.data) {
      ticketList.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        ticketNo: item.ticketNo || `TK-${item.id}`,
        title: item.title || '',
        customerName: item.customerName || (item.customerId ? `客户#${item.customerId}` : '—'),
        priority: item.priority || 'medium',
        status: item.status || 'pending',
        assigneeName: item.assigneeName || '',
        description: item.description || '',
        createdAt: item.createdAt,
      }))
      total.value = Number(res.data.total ?? 0)
    }
  } catch (e) {
    console.error('工单数据加载失败:', e)
    ElMessage.error('工单数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.ticket-page { max-width: 1400px; }
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); padding:20px; }
.search-form { margin-bottom:16px; padding-bottom:16px; border-bottom:1px solid var(--crm-border-light);
  .el-form { display:flex; flex-wrap:wrap; gap:0; }
  :deep(.el-form-item) { margin-bottom:0; }
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b { color:var(--crm-text-primary); } }
  &-right { display:flex; align-items:center; gap:4px; }
}
.cell-name { font-weight:500; color:var(--crm-text-primary); }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
