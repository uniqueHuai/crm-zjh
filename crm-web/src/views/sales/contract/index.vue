<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">合同管理</h2>
        <p class="page-subtitle">管理所有销售合同，跟踪合同状态与执行情况</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增合同</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词"><el-input v-model="queryParams.keywords" placeholder="合同编号/名称/客户" clearable style="width:180px" /></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
              <el-option label="草稿" value="draft" /><el-option label="待签署" value="pending_sign" /><el-option label="已签署" value="signed" /><el-option label="已作废" value="cancelled" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个合同</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="contractList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="contractNo" label="合同编号" width="170">
          <template #default="{row}">
            <el-tag size="small" effect="plain">{{ row.contractNo }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="合同名称" min-width="160" />
        <el-table-column prop="customerName" label="客户名称" width="130" />
        <el-table-column prop="totalAmount" label="合同金额" width="130">
          <template #default="{row}">¥{{ (row.totalAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="110" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{row}">
            <el-tag :type="statusTypeMap[row.status]" size="small" effect="dark">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="90" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="openAttachments(row)">附件</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'pending_sign'" link type="warning" size="small" @click="handleCancel(row)">作废</el-button>
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
        <el-form-item label="合同名称" prop="title" :rules="[{required:true,message:'请输入合同名称'}]"><el-input v-model="form.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="合同编号" prop="contractNo"><el-input v-model="form.contractNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户" prop="customerId" :rules="[{required:true,message:'请选择客户'}]"><el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="合同金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :step="1000" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" style="width:100%">
            <el-option label="草稿" value="draft" /><el-option label="待签署" value="pending_sign" /><el-option label="已签署" value="signed" /><el-option label="已作废" value="cancelled" />
          </el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="生效日期" prop="validFrom"><el-date-picker v-model="form.validFrom" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="失效日期" prop="validUntil"><el-date-picker v-model="form.validUntil" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </template>
    </FormDialog>

    <!-- 附件管理 -->
    <el-dialog v-model="attachVisible" title="合同附件" width="500px">
      <div v-if="attachments.length === 0" class="no-attachment">暂无附件，请上传</div>
      <el-table v-else :data="attachments" stripe>
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{row}">{{ (row.fileSize / 1024).toFixed(1) }} KB</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <a :href="`/api/v1/files/${row.id}/download`" target="_blank" class="attach-download">下载</a>
            <el-button link type="danger" size="small" @click="handleDeleteAttach(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="handleUploadSuccess"
        style="margin-top:16px"
      >
        <el-button type="primary" plain>上传附件</el-button>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getContractPage, createContract, updateContract, deleteContract, cancelContract, getContractAttachments, deleteContractAttachment } from '@/api/modules/sales'
import { getCustomerPage, type Customer } from '@/api/modules/customer'
import { getToken } from '@/utils/auth'

interface Contract {
  id: number; contractNo: string; title: string;
  customerId?: number; customerName: string;
  totalAmount: number; status: string;
  ownerName?: string; signedAt?: string;
  validFrom?: string; validUntil?: string;
  createdAt: string; remark?: string;
}

const statusMap: Record<string, string> = {
  draft: '草稿', pending_sign: '待签署', signed: '已签署', cancelled: '已作废',
}

const statusTypeMap: Record<string, string> = {
  draft: 'info', pending_sign: 'warning', signed: 'success', cancelled: 'danger',
}

const loading = ref(false)
const contractList = ref<Contract[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Contract | null>(null)
const customerOptions = ref<{ id: number; name: string }[]>([])

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  status: undefined as string | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑合同' : '新增合同')

// 附件管理
const attachVisible = ref(false)
const attachContractId = ref(0)
const attachments = ref<any[]>([])
const uploadUrl = computed(() => `/api/v1/contracts/${attachContractId.value}/attachments`)
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + getToken() }))

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', status: undefined, page: 1 }); fetchData() }
function openDialog(row?: Contract) {
  editingRow.value = row || null
  dialogVisible.value = true
}
async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateContract(editingRow.value.id, formData)
      ElMessage.success('修改成功')
    } else {
      await createContract(formData)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    ElMessage.error(editingRow.value ? '修改失败' : '新增失败')
  }
}
async function handleCancel(row: Contract) {
  try {
    await ElMessageBox.confirm(`确认作废合同「${row.title}」？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await cancelContract(row.id, '手动作废')
    ElMessage.success('合同已作废')
    fetchData()
  } catch {}
}
async function handleDelete(row: Contract) {
  try {
    await deleteContract(row.id)
    ElMessage.success(`已删除合同「${row.title}」`)
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getContractPage({ ...queryParams })
    contractList.value = (res.data?.records || []) as Contract[]
    total.value = Number(res.data?.total ?? 0)
  } finally { loading.value = false }
}

// 附件管理
async function openAttachments(row: Contract) {
  attachContractId.value = row.id
  attachments.value = []
  attachVisible.value = true
  try {
    const res = await getContractAttachments(row.id)
    attachments.value = res.data || []
  } catch { /* ignore */ }
}

function handleUploadSuccess(res: any) {
  if (res?.code === 200) {
    ElMessage.success('上传成功')
    getContractAttachments(attachContractId.value).then(r => { attachments.value = r.data || [] })
  } else {
    ElMessage.error(res?.message || '上传失败')
  }
}

async function handleDeleteAttach(row: any) {
  try {
    await deleteContractAttachment(row.id)
    ElMessage.success('附件已删除')
    attachments.value = attachments.value.filter(a => a.id !== row.id)
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchData()
  getCustomerPage({ page: 1, size: 999 }).then(res => {
    customerOptions.value = (res.data?.records || []).map((c: Customer) => ({ id: c.id, name: c.name }))
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
.no-attachment { text-align:center; padding:30px; color:var(--crm-text-secondary); font-size:14px; }
.attach-download { color:var(--crm-primary); text-decoration:none; font-size:13px; margin-right:8px; &:hover { text-decoration:underline; } }
</style>
