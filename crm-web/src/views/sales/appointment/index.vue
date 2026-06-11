<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">日程安排</h2>
        <p class="page-subtitle">管理销售日程活动，合理安排工作计划</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增日程</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词"><el-input v-model="queryParams.keywords" placeholder="日程标题/客户" clearable style="width:160px" /></el-form-item>
            <el-form-item label="类型"><el-select v-model="queryParams.type" placeholder="全部" clearable style="width:110px">
              <el-option label="会议" value="meeting" /><el-option label="电话" value="call" /><el-option label="演示" value="demo" />
            </el-select></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
              <el-option label="待进行" value="pending" /><el-option label="已完成" value="completed" /><el-option label="已取消" value="cancelled" />
            </el-select></el-form-item>
            <el-form-item label="日程日期">
              <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:220px" value-format="YYYY-MM-DD" />
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
          <span class="result-count">共 <b>{{ total }}</b> 条日程</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="appointmentList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="title" label="日程标题" min-width="160">
          <template #default="{row}">
            <el-link type="primary" :underline="false" class="cell-link">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" width="130" />
        <el-table-column prop="appointmentDate" label="日程日期" width="110" />
        <el-table-column prop="startTime" label="开始时间" width="90" />
        <el-table-column prop="endTime" label="结束时间" width="90" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{row}">
            <el-tag :type="typeTagType(row.type)" size="small" effect="dark">{{ typeMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="140">
          <template #default="{row}">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="90" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 'pending'" link type="success" size="small" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 'pending'" link type="warning" size="small" @click="handleCancel(row)">取消</el-button>
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
        <el-form-item label="日程标题" prop="title" :rules="[{required:true,message:'请输入日程标题'}]"><el-input v-model="form.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型" prop="type"><el-select v-model="form.type" style="width:100%"><el-option label="会议" value="meeting" /><el-option label="电话" value="call" /><el-option label="演示" value="demo" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="日程日期" prop="appointmentDate"><el-date-picker v-model="form.appointmentDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="地点" prop="location"><el-input v-model="form.location" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-time-picker v-model="form.startTime" style="width:100%" value-format="HH:mm" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-time-picker v-model="form.endTime" style="width:100%" value-format="HH:mm" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'

interface Appointment {
  id: number
  title: string
  customerName: string
  appointmentDate: string
  startTime: string
  endTime: string
  type: string
  location?: string
  status: string
  creatorName: string
  remark?: string
}

const typeMap: Record<string, string> = { meeting: '会议', call: '电话', demo: '演示' }
const statusMap: Record<string, string> = { pending: '待进行', completed: '已完成', cancelled: '已取消' }

function typeTagType(type: string) {
  const map: Record<string, string> = { meeting: 'primary', call: 'success', demo: 'warning' }
  return map[type] || 'info'
}
function statusTagType(status: string) {
  const map: Record<string, string> = { pending: 'info', completed: 'success', cancelled: 'danger' }
  return map[status] || 'info'
}

const loading = ref(false)
const appointmentList = ref<Appointment[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Appointment | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑日程' : '新增日程')

const mockData: Appointment[] = [
  { id: 1, title: '华为CRM需求沟通会', customerName: '华为技术', appointmentDate: '2026-06-03', startTime: '09:00', endTime: '11:00', type: 'meeting', location: '华为总部3楼会议室', status: 'pending', creatorName: '张销售' },
  { id: 2, title: '阿里云合同续签电话沟通', customerName: '阿里巴巴', appointmentDate: '2026-06-01', startTime: '14:00', endTime: '14:30', type: 'call', location: '', status: 'pending', creatorName: '张销售' },
  { id: 3, title: '腾讯数据中台产品演示', customerName: '腾讯科技', appointmentDate: '2026-06-05', startTime: '10:00', endTime: '12:00', type: 'demo', location: '线上腾讯会议', status: 'pending', creatorName: '李销售' },
  { id: 4, title: '字节跳动选型交流会', customerName: '字节跳动', appointmentDate: '2026-05-25', startTime: '14:00', endTime: '16:00', type: 'meeting', location: '字节跳动北京总部', status: 'completed', creatorName: '李销售', remark: '已顺利完成交流' },
  { id: 5, title: '小米智能硬件演示', customerName: '小米科技', appointmentDate: '2026-06-05', startTime: '15:00', endTime: '17:00', type: 'demo', location: '小米科技园', status: 'pending', creatorName: '王销售' },
  { id: 6, title: '比亚迪系统使用回访', customerName: '比亚迪', appointmentDate: '2026-05-23', startTime: '10:30', endTime: '11:00', type: 'call', location: '', status: 'cancelled', creatorName: '张销售', remark: '客户临时有事改期' },
]

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', type: undefined, status: undefined, dateRange: undefined, page: 1 }); fetchData() }
function openDialog(row?: Appointment) {
  editingRow.value = row || null
  dialogVisible.value = true
}
function handleSubmit(formData: any, done: () => void) {
  ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
  done()
  fetchData()
}
function handleComplete(row: Appointment) {
  ElMessageBox.confirm(`确认完成日程「${row.title}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' })
    .then(() => { ElMessage.success('日程已标记为完成'); fetchData() })
    .catch(() => {})
}
function handleCancel(row: Appointment) {
  ElMessageBox.confirm(`确认取消日程「${row.title}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(() => { ElMessage.success('日程已取消'); fetchData() })
    .catch(() => {})
}
function handleDelete(row: Appointment) {
  ElMessage.success(`已删除日程「${row.title}」`)
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    await new Promise(r => setTimeout(r, 300))
    let list = [...mockData]
    if (queryParams.keywords) {
      const kw = queryParams.keywords!
      list = list.filter(i => i.title.includes(kw) || i.customerName.includes(kw))
    }
    if (queryParams.type) list = list.filter(i => i.type === queryParams.type)
    if (queryParams.status) list = list.filter(i => i.status === queryParams.status)
    if (queryParams.dateRange && queryParams.dateRange.length === 2) {
      const [start, end] = queryParams.dateRange
      list = list.filter(i => i.appointmentDate >= start && i.appointmentDate <= end)
    }
    appointmentList.value = list
    total.value = list.length
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.sales-page { max-width: 1400px; }
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
.cell-link { font-weight: 500; }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
