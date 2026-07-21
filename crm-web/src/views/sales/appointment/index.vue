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
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
              <el-option label="待拜访" value="scheduled" />
              <el-option label="已签到" value="checked_in" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="90" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 'scheduled'" link type="success" size="small" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 'checked_in'" link type="primary" size="small" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 'scheduled' || row.status === 'checked_in'" link type="warning" size="small" @click="handleCancel(row)">取消</el-button>
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
        <el-form-item label="日程标题" prop="title" :rules="[{required:true,message:'请输入日程标题'}]"><el-input v-model="form.title" placeholder="日程标题" /></el-form-item>
        <el-form-item label="客户" prop="customerId" :rules="[{required:true,message:'请选择客户'}]">
          <el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日程日期" prop="appointmentDate" :rules="[{required:true,message:'请选择日程日期'}]">
          <el-date-picker v-model="form.appointmentDate" type="datetime" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间" />
        </el-form-item>
        <el-form-item label="地点" prop="location"><el-input v-model="form.location" placeholder="日程地点" /></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="日程描述" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" /></el-form-item>
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
  getAppointmentPage,
  createAppointment,
  updateAppointment,
  deleteAppointment,
  checkInAppointment,
  completeAppointment,
  updateAppointmentStatus,
} from '@/api/modules/sales'
import type { Appointment } from '@/api/modules/sales'
import { getCustomerPage, type Customer } from '@/api/modules/customer'

interface AppointmentRecord extends Appointment {
  customerName?: string
  creatorName?: string
  remark?: string
  cancelReason?: string
  location?: string
}

const statusMap: Record<string, string> = {
  scheduled: '待拜访',
  checked_in: '已签到',
  completed: '已完成',
  cancelled: '已取消',
}

function statusTagType(status: string) {
  const map: Record<string, string> = {
    scheduled: 'primary',
    checked_in: 'success',
    completed: 'info',
    cancelled: 'danger',
  }
  return map[status] || 'info'
}

const loading = ref(false)
const appointmentList = ref<AppointmentRecord[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<AppointmentRecord | null>(null)
const customerOptions = ref<{ id: number; name: string }[]>([])

const queryParams = reactive({
  page: 1,
  size: 20,
  keywords: '',
  status: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
})

const dialogTitle = computed(() => (editingRow.value ? '编辑日程' : '新增日程'))

function handleSearch() {
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  Object.assign(queryParams, {
    keywords: '',
    status: undefined,
    dateRange: undefined,
    page: 1,
  })
  fetchData()
}

function openDialog(row?: AppointmentRecord) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateAppointment(editingRow.value.id!, formData)
    } else {
      await createAppointment({ ...formData, type: 'visit' })
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch {
    done()
  }
}

async function handleCheckIn(row: AppointmentRecord) {
  try {
    await checkInAppointment(row.id!, {
      longitude: 0,
      latitude: 0,
      address: row.location || '',
    })
    ElMessage.success('签到成功')
    fetchData()
  } catch {
    /* handled by interceptor */
  }
}

async function handleComplete(row: AppointmentRecord) {
  try {
    await ElMessageBox.confirm(`确认完成日程「${row.title}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await completeAppointment(row.id!, { summary: '已完成拜访' })
    ElMessage.success('日程已完成')
    fetchData()
  } catch {
    /* cancelled or error */
  }
}

async function handleCancel(row: AppointmentRecord) {
  try {
    await ElMessageBox.confirm(`确认取消日程「${row.title}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateAppointmentStatus(row.id!, 'cancelled')
    ElMessage.success('日程已取消')
    fetchData()
  } catch {
    /* cancelled or error */
  }
}

async function handleDelete(row: AppointmentRecord) {
  try {
    await deleteAppointment(row.id!)
    ElMessage.success(`已删除日程「${row.title}」`)
    fetchData()
  } catch {
    /* handled by interceptor */
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.dateRange && queryParams.dateRange.length === 2) {
      params.startDate = queryParams.dateRange[0]
      params.endDate = queryParams.dateRange[1]
    }
    const res = await getAppointmentPage(params as any)
    appointmentList.value = (res.data.records || []) as AppointmentRecord[]
    total.value = Number(res.data.total ?? 0)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  getCustomerPage({ page: 1, size: 999 }).then((res) => {
    customerOptions.value = (res.data?.records || []).map((c: Customer) => ({
      id: c.id,
      name: c.name,
    }))
  })
})
</script>

<style scoped lang="scss">
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
