<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">报价管理</h2>
        <p class="page-subtitle">管理销售报价单，快速响应客户询价需求</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增报价</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词"><el-input v-model="queryParams.keywords" placeholder="报价编号/客户" clearable style="width:180px" /></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
              <el-option label="草稿" value="draft" /><el-option label="待审批" value="pending_approval" /><el-option label="已通过" value="approved" /><el-option label="已拒绝" value="rejected" /><el-option label="已作废" value="void" />
            </el-select></el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ total }}</b> 个报价</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="quotationList" v-loading="loading" stripe max-height="600">
        <el-table-column label="报价编号" width="170">
          <template #default="{row}">
            <el-tag size="small" effect="plain">{{ row.quotationNo }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" width="130" />
        <el-table-column prop="totalAmount" label="总金额" width="130">
          <template #default="{row}">¥{{ (row.totalAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="validUntil" label="有效期至" width="110" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{row}">
            <el-tag :type="statusTypeMap[row.status]" size="small" effect="dark">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="90" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 'pending_approval'" link type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'pending_approval'" link type="warning" size="small" @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'approved'" link type="info" size="small" @click="handleVoid(row)">作废</el-button>
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || {}" @submit="handleSubmit">
      <template #default="{form}">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户" prop="customerId" :rules="[{required:true,message:'请选择客户'}]"><el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联商机" prop="opportunityId"><el-select v-model="form.opportunityId" filterable clearable style="width:100%">
            <el-option v-for="o in opportunityOptions" :key="o.id" :label="o.name" :value="o.id" />
          </el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="总金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :step="1000" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="折扣金额" prop="discountAmount"><el-input-number v-model="form.discountAmount" :min="0" :step="1000" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="最终金额" prop="finalAmount"><el-input-number v-model="form.finalAmount" :min="0" :step="1000" style="width:100%" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="有效期至" prop="validUntil"><el-date-picker v-model="form.validUntil" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" style="width:100%">
            <el-option label="草稿" value="draft" /><el-option label="待审批" value="pending_approval" /><el-option label="已通过" value="approved" />
          </el-select></el-form-item></el-col>
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
import { getQuotationPage, createQuotation, updateQuotation, deleteQuotation, approveQuotation, rejectQuotation, voidQuotation, getOpportunityPage } from '@/api/modules/sales'
import { getCustomerPage, type Customer } from '@/api/modules/customer'

interface Quotation {
  id: number; quotationNo: string; customerId: number; customerName?: string;
  opportunityId?: number; totalAmount: number; discountAmount?: number;
  finalAmount?: number; validUntil: string; status: string;
  creatorName?: string; remark?: string; createdAt?: string;
}

const statusMap: Record<string, string> = {
  draft: '草稿', pending_approval: '待审批', approved: '已通过', rejected: '已拒绝', void: '已作废',
}

const statusTypeMap: Record<string, string> = {
  draft: 'info', pending_approval: 'warning', approved: 'success', rejected: 'danger', void: 'info',
}

const loading = ref(false)
const quotationList = ref<Quotation[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Quotation | null>(null)
const customerOptions = ref<{ id: number; name: string }[]>([])
const opportunityOptions = ref<{ id: number; name: string }[]>([])

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  status: undefined as string | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑报价' : '新增报价')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', status: undefined, page: 1 }); fetchData() }
function openDialog(row?: Quotation) {
  editingRow.value = row || null
  dialogVisible.value = true
}
async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateQuotation(editingRow.value.id, formData)
      ElMessage.success('修改成功')
    } else {
      await createQuotation(formData)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    ElMessage.error(editingRow.value ? '修改失败' : '新增失败')
  }
}
async function handleApprove(row: Quotation) {
  try {
    await ElMessageBox.confirm(`确认通过报价「${row.quotationNo}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' })
    await approveQuotation(row.id)
    ElMessage.success('报价已通过')
    fetchData()
  } catch {}
}
async function handleReject(row: Quotation) {
  try {
    await ElMessageBox.confirm(`确认拒绝报价「${row.quotationNo}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await rejectQuotation(row.id, '')
    ElMessage.success('报价已拒绝')
    fetchData()
  } catch {}
}
async function handleVoid(row: Quotation) {
  try {
    await ElMessageBox.confirm(`确认作废报价「${row.quotationNo}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await voidQuotation(row.id, '手动作废')
    ElMessage.success('报价已作废')
    fetchData()
  } catch {}
}
async function handleDelete(row: Quotation) {
  try {
    await deleteQuotation(row.id)
    ElMessage.success(`已删除报价`)
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getQuotationPage({ ...queryParams })
    quotationList.value = (res.data?.records || []) as Quotation[]
    total.value = Number(res.data?.total ?? 0)
  } finally { loading.value = false }
}

onMounted(() => {
  fetchData()
  getCustomerPage({ page: 1, size: 999 }).then(res => {
    customerOptions.value = (res.data?.records || []).map((c: Customer) => ({ id: c.id, name: c.name }))
  })
  getOpportunityPage({ page: 1, size: 999 }).then(res => {
    opportunityOptions.value = ((res.data?.records || []) as any[]).map((o: any) => ({ id: o.id, name: o.name }))
  })
})
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
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
