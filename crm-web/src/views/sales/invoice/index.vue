<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">发票管理</h2>
        <p class="page-subtitle">管理发票申请、开票、邮寄、签收与作废</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>开票申请</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="发票抬头/纳税人识别号" clearable style="width:180px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="待开票" value="pending" />
                <el-option label="已开票" value="issued" />
                <el-option label="已邮寄" value="shipped" />
                <el-option label="已签收" value="confirmed" />
                <el-option label="已作废" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item label="客户">
              <el-select v-model="queryParams.customerId" placeholder="全部客户" clearable filterable style="width:150px">
                <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个发票</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="invoiceList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="id" label="发票编号" width="100" />
        <el-table-column prop="title" label="发票抬头" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taxId" label="纳税人识别号" width="160" />
        <el-table-column prop="amount" label="金额" width="130">
          <template #default="{row}">¥{{ (row.amount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{row}">
            {{ invoiceTypeMap[row.invoiceType] || row.invoiceType }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTypeMap[row.status]" size="small" effect="dark">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceNo" label="发票号码" width="150" />
        <el-table-column prop="issueDate" label="开票日期" width="110" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status === 'pending'" link type="primary" size="small" @click="openIssueDialog(row)">开票</el-button>
            <el-button v-if="row.status === 'issued'" link type="primary" size="small" @click="openShipDialog(row)">邮寄</el-button>
            <el-button v-if="row.status === 'shipped'" link type="success" size="small" @click="handleConfirm(row)">签收确认</el-button>
            <el-button v-if="row.status === 'pending' || row.status === 'issued'" link type="danger" size="small" @click="handleCancel(row)">作废</el-button>
            <el-tag v-if="row.status === 'confirmed' || row.status === 'cancelled'" size="small" effect="plain" type="info">无操作</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <!-- 发票申请对话框 -->
    <FormDialog v-model:visible="dialogVisible" title="开票申请" :initial-data="{ invoiceType: 'regular' }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="发票抬头" prop="title" :rules="[{required:true,message:'请输入发票抬头'}]">
          <el-input v-model="form.title" placeholder="发票抬头" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="纳税人识别号" prop="taxId" :rules="[{required:true,message:'请输入纳税人识别号'}]">
              <el-input v-model="form.taxId" placeholder="纳税人识别号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票类型" prop="invoiceType" :rules="[{required:true,message:'请选择发票类型'}]">
              <el-select v-model="form.invoiceType" style="width:100%">
                <el-option label="普通发票" value="regular" />
                <el-option label="增值税专用发票" value="special" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount" :rules="[{required:true,message:'请输入金额'}]">
              <el-input-number v-model="form.amount" :min="0" :step="100" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId" :rules="[{required:true,message:'请选择客户'}]">
              <el-select v-model="form.customerId" filterable style="width:100%">
                <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="合同" prop="contractId">
              <el-select v-model="form.contractId" filterable clearable style="width:100%">
                <el-option v-for="c in contractOptions" :key="c.id" :label="c.title" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接收邮箱" prop="receiveEmail">
              <el-input v-model="form.receiveEmail" placeholder="接收邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="发票内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="2" placeholder="发票内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </template>
    </FormDialog>

    <!-- 开票对话框 -->
    <el-dialog v-model="issueVisible" title="开票" width="500px" :close-on-click-modal="false">
      <el-form ref="issueFormRef" :model="issueForm" label-width="110px">
        <el-form-item label="发票号码" prop="invoiceNo" :rules="[{required:true,message:'请输入发票号码'}]">
          <el-input v-model="issueForm.invoiceNo" placeholder="请输入发票号码" />
        </el-form-item>
        <el-form-item label="开票日期" prop="issueDate" :rules="[{required:true,message:'请选择开票日期'}]">
          <el-date-picker v-model="issueForm.issueDate" type="date" style="width:100%" value-format="YYYY-MM-DD" placeholder="选择开票日期" />
        </el-form-item>
        <el-form-item label="发票文件URL" prop="invoiceFileUrl">
          <el-input v-model="issueForm.invoiceFileUrl" placeholder="发票文件URL(可选)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueVisible = false">取消</el-button>
        <el-button type="primary" :loading="issueLoading" @click="handleIssue">确定</el-button>
      </template>
    </el-dialog>

    <!-- 邮寄对话框 -->
    <el-dialog v-model="shipVisible" title="邮寄" width="500px" :close-on-click-modal="false">
      <el-form ref="shipFormRef" :model="shipForm" label-width="110px">
        <el-form-item label="快递公司" prop="expressCompany" :rules="[{required:true,message:'请输入快递公司'}]">
          <el-input v-model="shipForm.expressCompany" placeholder="请输入快递公司" />
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo" :rules="[{required:true,message:'请输入快递单号'}]">
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="handleShip">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import {
  getInvoicePage,
  createInvoice,
  issueInvoice,
  shipInvoice,
  confirmInvoice,
  cancelInvoice,
} from '@/api/modules/sales'
import { getCustomerPage, type Customer } from '@/api/modules/customer'
import { getContractPage } from '@/api/modules/sales'

interface InvoiceItem {
  id: number
  title: string
  taxId: string
  invoiceType: string
  amount: number
  content?: string
  receiveEmail?: string
  status: string
  invoiceNo?: string
  invoiceFileUrl?: string
  issueDate?: string
  expressCompany?: string
  expressNo?: string
  shipDate?: string
  customerId?: number
  contractId?: number
  remark?: string
  createTime?: string
}

const statusMap: Record<string, string> = {
  pending: '待开票',
  issued: '已开票',
  shipped: '已邮寄',
  confirmed: '已签收',
  cancelled: '已作废',
}

const statusTypeMap: Record<string, string> = {
  pending: 'primary',
  issued: 'warning',
  shipped: 'info',
  confirmed: 'success',
  cancelled: 'danger',
}

const invoiceTypeMap: Record<string, string> = {
  regular: '普通发票',
  special: '增值税专用发票',
}

const loading = ref(false)
const invoiceList = ref<InvoiceItem[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)

const queryParams = reactive({
  page: 1,
  size: 20,
  keywords: '',
  status: undefined as string | undefined,
  customerId: undefined as number | undefined,
  contractId: undefined as number | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined,
})

const dateRange = ref<[string, string] | null>(null)
const customerOptions = ref<{ id: number; name: string }[]>([])
const contractOptions = ref<{ id: number; title: string; contractNo: string }[]>([])

function handleSearch() {
  if (dateRange.value) {
    queryParams.startDate = dateRange.value[0]
    queryParams.endDate = dateRange.value[1]
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
  queryParams.page = 1
  fetchData()
}

function handleReset() {
  Object.assign(queryParams, {
    keywords: '',
    status: undefined,
    customerId: undefined,
    contractId: undefined,
    startDate: undefined,
    endDate: undefined,
    page: 1,
  })
  dateRange.value = null
  fetchData()
}

function openDialog() {
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    await createInvoice(formData)
    ElMessage.success('开票申请已提交')
    done()
    fetchData()
  } catch {
    done()
  }
}

// 开票
const issueVisible = ref(false)
const issueLoading = ref(false)
const issueInvoiceId = ref(0)
const issueFormRef = ref<FormInstance>()
const issueForm = reactive({
  invoiceNo: '',
  issueDate: '',
  invoiceFileUrl: '',
})

function openIssueDialog(row: InvoiceItem) {
  issueInvoiceId.value = row.id
  issueForm.invoiceNo = ''
  issueForm.issueDate = ''
  issueForm.invoiceFileUrl = ''
  issueVisible.value = true
}

async function handleIssue() {
  const valid = await issueFormRef.value?.validate().catch(() => false)
  if (!valid) return
  issueLoading.value = true
  try {
    await issueInvoice(issueInvoiceId.value, {
      invoiceNo: issueForm.invoiceNo,
      issueDate: issueForm.issueDate,
      invoiceFileUrl: issueForm.invoiceFileUrl || undefined,
    })
    ElMessage.success('开票成功')
    issueVisible.value = false
    fetchData()
  } catch {
    /* handled by interceptor */
  } finally {
    issueLoading.value = false
  }
}

// 邮寄
const shipVisible = ref(false)
const shipLoading = ref(false)
const shipInvoiceId = ref(0)
const shipFormRef = ref<FormInstance>()
const shipForm = reactive({
  expressCompany: '',
  expressNo: '',
})

function openShipDialog(row: InvoiceItem) {
  shipInvoiceId.value = row.id
  shipForm.expressCompany = ''
  shipForm.expressNo = ''
  shipVisible.value = true
}

async function handleShip() {
  const valid = await shipFormRef.value?.validate().catch(() => false)
  if (!valid) return
  shipLoading.value = true
  try {
    await shipInvoice(shipInvoiceId.value, {
      expressCompany: shipForm.expressCompany,
      expressNo: shipForm.expressNo,
    })
    ElMessage.success('邮寄信息已提交')
    shipVisible.value = false
    fetchData()
  } catch {
    /* handled by interceptor */
  } finally {
    shipLoading.value = false
  }
}

// 签收确认
async function handleConfirm(row: InvoiceItem) {
  try {
    await ElMessageBox.confirm(`确认发票「${row.title}」已签收？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })
    await confirmInvoice(row.id)
    ElMessage.success('签收确认成功')
    fetchData()
  } catch {
    /* cancelled or error handled by interceptor */
  }
}

// 作废
async function handleCancel(row: InvoiceItem) {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确认作废发票「${row.title}」？请填写作废原因`,
      '作废确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入作废原因',
        inputValidator: (val: string) => !!val.trim() || '作废原因不能为空',
        inputErrorMessage: '作废原因不能为空',
        type: 'warning',
      }
    )
    await cancelInvoice(row.id, reason)
    ElMessage.success('发票已作废')
    fetchData()
  } catch {
    /* cancelled or error handled by interceptor */
  }
}

async function fetchData() {
  loading.value = true
  try {
    if (dateRange.value) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    }
    const params: Record<string, any> = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.customerId) params.customerId = queryParams.customerId
    if (queryParams.contractId) params.contractId = queryParams.contractId
    if (queryParams.startDate) params.startDate = queryParams.startDate
    if (queryParams.endDate) params.endDate = queryParams.endDate
    const res = await getInvoicePage(params)
    invoiceList.value = (res.data?.records || []) as InvoiceItem[]
    total.value = Number(res.data?.total ?? 0)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  getCustomerPage({ page: 1, size: 999 }).then(res => {
    customerOptions.value = (res.data?.records || []).map((c: Customer) => ({ id: c.id, name: c.name }))
  })
  getContractPage({ page: 1, size: 999 }).then(res => {
    contractOptions.value = (res.data?.records || []).map((c: any) => ({ id: c.id, title: c.title, contractNo: c.contractNo }))
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
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
