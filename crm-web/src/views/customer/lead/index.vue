<template>
  <div class="lead-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">线索管理</h2>
        <p class="page-subtitle">管理潜在客户线索，分配与跟进转化</p>
      </div>
      <div class="page-header-right">
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>导入
        </el-button>
        <el-button @click="handleDistribute" :disabled="selectedLeads.length === 0">
          <el-icon><Share /></el-icon>分配{{ selectedLeads.length ? `(${selectedLeads.length})` : '' }}
        </el-button>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增线索
        </el-button>
      </div>
    </div>

    <!-- Search & Table Card -->
    <div class="card">
      <!-- Search Form -->
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="姓名/手机">
              <el-input v-model="queryParams.keywords" placeholder="姓名/手机号" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="待跟进" value="pending" />
                <el-option label="跟进中" value="following" />
                <el-option label="已转化" value="converted" />
                <el-option label="已无效" value="invalid" />
              </el-select>
            </el-form-item>
            <el-form-item label="来源">
              <el-select v-model="queryParams.sourceChannel" placeholder="全部" clearable style="width:130px">
                <el-option label="小程序注册" value="mini_program" />
                <el-option label="广告落地页" value="ad_landing" />
                <el-option label="线下活动" value="offline" />
                <el-option label="手动录入" value="manual_input" />
              </el-select>
            </el-form-item>
            <el-form-item label="负责人">
              <el-select v-model="queryParams.ownerId" placeholder="全部" clearable style="width:120px">
                <el-option label="我" :value="currentUserId" />
              </el-select>
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ total }}</b> 条线索</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch">
            <el-icon><Search /></el-icon>{{ showSearch ? '隐藏搜索' : '展开搜索' }}
          </el-button>
          <el-tooltip content="刷新">
            <el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- Table -->
      <el-table :data="leadList" v-loading="loading" stripe max-height="600" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="姓名" width="110">
          <template #default="{row}">
            <div class="cell-name">{{ row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="sourceChannelName" label="来源" width="110">
          <template #default="{row}"><el-tag size="small" effect="plain">{{ row.sourceChannelName || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" effect="light">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="company" label="公司" min-width="150">
          <template #default="{row}">{{ row.company || '-' }}</template>
        </el-table-column>
        <el-table-column label="地区" width="130">
          <template #default="{row}">{{ [row.province, row.city].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleConvert(row)">转化</el-button>
            <el-button link type="primary" size="small" @click="handleDistributeOne(row)">分配</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10,20,50,100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="fetchData"
        />
      </div>
    </div>

    <!-- Lead Form Dialog -->
    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || { status: 'pending', sourceChannel: '' }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="姓名" prop="name" :rules="[{required:true,message:'请输入姓名'}]">
          <el-input v-model="form.name" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" :rules="[{required:true,message:'请输入手机号'}]">
          <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="省份" prop="province">
              <el-input v-model="form.province" placeholder="省份" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="城市" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="公司" prop="company">
          <el-input v-model="form.company" placeholder="公司名称" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="职位" />
        </el-form-item>
        <el-form-item label="来源" prop="sourceChannel" :rules="[{required:true,message:'请选择来源'}]">
          <el-select v-model="form.sourceChannel" placeholder="选择来源" style="width:100%">
            <el-option label="小程序注册" value="mini_program" />
            <el-option label="广告落地页" value="ad_landing" />
            <el-option label="线下活动" value="offline" />
            <el-option label="手动录入" value="manual_input" />
            <el-option label="朋友推荐" value="referral" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </template>
    </FormDialog>

    <!-- Import Dialog -->
    <el-dialog v-model="importDialogVisible" title="导入线索" :width="520" :close-on-click-modal="false">
      <template v-if="!importResult">
        <p style="margin:0 0 16px;font-size:13px;color:var(--crm-text-secondary)">
          下载<a href="/templates/lead_import_template.xlsx" style="color:var(--el-color-primary)">导入模板</a>，按格式填写后上传
        </p>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="true"
          :limit="1"
          :on-change="handleFileChange"
          accept=".xlsx,.xls"
        >
          <template #trigger>
            <el-button type="primary">选择文件</el-button>
          </template>
          <template #tip>
            <p style="font-size:12px;color:var(--crm-text-secondary);margin-top:8px">仅支持 .xlsx / .xls 格式</p>
          </template>
        </el-upload>
      </template>
      <template v-else>
        <el-result :icon="importResult.failCount > 0 ? 'warning' : 'success'" :title="`导入完成`">
          <template #extra>
            <p>共 <b>{{ importResult.totalCount }}</b> 条，成功 <b style="color:var(--el-color-success)">{{ importResult.successCount }}</b> 条，失败 <b style="color:var(--el-color-danger)">{{ importResult.failCount }}</b> 条</p>
            <el-alert v-for="(err,i) in importResult.errors" :key="i" :title="err" type="error" :closable="false" show-icon style="margin-top:8px" />
          </template>
        </el-result>
      </template>
      <template #footer>
        <el-button v-if="!importResult" :disabled="!importFile" type="primary" :loading="importSubmitting" @click="confirmImport">开始导入</el-button>
        <el-button v-else @click="handleImportDone">完成</el-button>
        <el-button @click="importDialogVisible = false">{{ importResult ? '关闭' : '取消' }}</el-button>
      </template>
    </el-dialog>

    <!-- Distribute Dialog -->
    <el-dialog v-model="distributeDialogVisible" title="分配负责人" :width="480" :close-on-click-modal="false" @open="fetchUsers">
      <el-form label-width="80px">
        <el-form-item label="分配方式">
          <el-radio-group v-model="distributeAssignType">
            <el-radio value="manual">手动分配</el-radio>
            <el-radio value="fair">公平分配</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="负责人" prop="ownerId" :rules="[{required:true,message:'请选择负责人'}]">
          <el-select v-model="distributeOwnerId" placeholder="请选择负责人" filterable style="width:100%" :loading="usersLoading">
            <el-option v-for="u in userOptions" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="线索数">
          <span>{{ distributeLeadIds.length }} 条</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="distributeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="distributeSubmitting" :disabled="!distributeOwnerId" @click="confirmDistribute">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Share, Upload } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getLeadPage, createLead, updateLead, deleteLead, convertLead, distributeLeads, importLeads } from '@/api/modules/customer'
import { getUserPage } from '@/api/modules/system/user'
import type { Lead } from '@/types'

const statusMap: Record<string, {label:string;type:string}> = {
  pending: { label: '待跟进', type: 'info' },
  following: { label: '跟进中', type: 'primary' },
  converted: { label: '已转化', type: 'success' },
  invalid: { label: '已无效', type: 'danger' },
}

const currentUserId = computed(() => {
  try {
    const info = localStorage.getItem('userInfo')
    return info ? JSON.parse(info).userId : 0
  } catch { return 0 }
})

const loading = ref(false)
const leadList = ref<Lead[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Lead | null>(null)
const dateRange = ref<string[]>([])

const queryParams = reactive({
  page: 1, size: 20,
  keywords: '', status: undefined as string | undefined,
  sourceChannel: undefined as string | undefined,
  ownerId: undefined as number | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑线索' : '新增线索')

/* ===== 分配 ===== */
const selectedLeads = ref<Lead[]>([])
const distributeDialogVisible = ref(false)
const distributeLeadIds = ref<number[]>([])
const distributeOwnerId = ref<number | undefined>(undefined)
const distributeAssignType = ref('manual')
const distributeSubmitting = ref(false)
const userOptions = ref<{id:number;realName:string}[]>([])
const usersLoading = ref(false)

function handleSelectionChange(rows: Lead[]) {
  selectedLeads.value = rows
}

function openDistributeDialog(leadIds: number[]) {
  distributeLeadIds.value = leadIds
  distributeOwnerId.value = undefined
  distributeAssignType.value = 'manual'
  distributeDialogVisible.value = true
}

function handleDistribute() {
  const ids = selectedLeads.value.map(r => r.id)
  if (!ids.length) { ElMessage.warning('请先勾选需要分配的线索'); return }
  openDistributeDialog(ids)
}

function handleDistributeOne(row: Lead) {
  openDistributeDialog([row.id])
}

async function fetchUsers() {
  usersLoading.value = true
  try {
    const res = await getUserPage({ page: 1, size: 999 })
    userOptions.value = res.data.records.map(u => ({ id: u.id, realName: u.realName }))
  } finally { usersLoading.value = false }
}

async function confirmDistribute() {
  if (!distributeOwnerId.value) return
  distributeSubmitting.value = true
  try {
    await distributeLeads({
      leadIds: distributeLeadIds.value,
      ownerId: distributeOwnerId.value,
      assignType: distributeAssignType.value,
    })
    ElMessage.success(`已分配给 ${userOptions.value.find(u => u.id === distributeOwnerId.value)?.realName || ''}`)
    distributeDialogVisible.value = false
    selectedLeads.value = []
    fetchData()
  } finally { distributeSubmitting.value = false }
}

/* ===== CRUD ===== */

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', status: undefined, sourceChannel: undefined, ownerId: undefined, page: 1 })
  dateRange.value = []
  fetchData()
}

function openDialog(row?: Lead) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateLead(editingRow.value.id, formData)
    } else {
      await createLead(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: Lead) {
  try {
    await deleteLead(row.id)
    ElMessage.success(`已删除线索「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleConvert(row: Lead) {
  try {
    await ElMessageBox.confirm(`确认将线索「${row.name}」转化为客户？`, '提示')
    await convertLead(row.id, {})
    ElMessage.success(`线索「${row.name}」已转化`)
    fetchData()
  } catch { /* cancelled or error */ }
}

/* ===== 导入 ===== */
const importDialogVisible = ref(false)
const importFile = ref<File | undefined>(undefined)
const importResult = ref<{ totalCount: number; successCount: number; failCount: number; errors: string[] } | null>(null)
const importSubmitting = ref(false)

function handleImport() {
  importDialogVisible.value = true
  importFile.value = undefined
  importResult.value = null
}

function handleFileChange(uploadFile: any) {
  importFile.value = uploadFile.raw
}

async function confirmImport() {
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }
  importSubmitting.value = true
  try {
    const res = await importLeads(importFile.value)
    importResult.value = res.data
  } finally { importSubmitting.value = false }
}

function handleImportDone() {
  importDialogVisible.value = false
  importFile.value = undefined
  importResult.value = null
  fetchData()
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
    if (queryParams.sourceChannel) params.sourceChannel = queryParams.sourceChannel
    if (queryParams.ownerId) params.ownerId = queryParams.ownerId
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getLeadPage(params as any)
    leadList.value = res.data.records
    total.value = Number(res.data.total ?? 0)
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.lead-page { max-width: 1400px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 20px;
  .page-title { margin: 0; font-size: 20px; font-weight: 700; color: var(--crm-text-primary); }
  .page-subtitle { margin: 4px 0 0; font-size: 13px; color: var(--crm-text-secondary); }
  &-right { display: flex; gap: 8px; flex-shrink: 0; }
}

.card {
  background: var(--crm-bg-white); border-radius: var(--crm-radius-lg);
  border: 1px solid var(--crm-border); padding: 20px;
}

.search-form {
  margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--crm-border-light);
  .el-form { display: flex; flex-wrap: wrap; gap: 0; }
  :deep(.el-form-item) { margin-bottom: 0; }
}

.toolbar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  .result-count { font-size: 13px; color: var(--crm-text-secondary); b { color: var(--crm-text-primary); } }
  &-right { display: flex; align-items: center; gap: 4px; }
}

.cell-name { font-weight: 500; color: var(--crm-text-primary); }

.pagination-wrap {
  display: flex; justify-content: flex-end; padding-top: 16px;
}
</style>
