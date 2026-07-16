<template>
  <div class="customer-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">客户列表</h2>
        <p class="page-subtitle">管理所有客户信息与360°视图</p>
      </div>
      <div class="page-header-right">
        <el-dropdown>
          <el-button><el-icon><Download /></el-icon>导出<el-icon><ArrowDown /></el-icon></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>导出选中</el-dropdown-item>
              <el-dropdown-item>导出全部</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增客户</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="客户名称"><el-input v-model="queryParams.keywords" placeholder="名称/手机号" clearable style="width:160px" /></el-form-item>
            <el-form-item label="等级">
              <el-select v-model="queryParams.levelId" placeholder="全部" clearable style="width:120px">
                <el-option v-for="l in levelOptions" :key="l.id" :label="l.name" :value="l.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="负责人">
              <el-select v-model="queryParams.ownerId" placeholder="全部" clearable style="width:120px">
                <el-option label="我" :value="currentUserId" />
                <el-option v-for="u in userOptions" :key="u.id" :label="u.realName" :value="u.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <el-select v-model="queryParams.tagIds" placeholder="全部" clearable multiple collapse-tags style="width:180px">
                <el-option v-for="t in tagOptions" :key="t.id" :label="t.name" :value="t.id" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个客户</span>
          <el-divider direction="vertical" v-if="selection.length" />
          <template v-if="selection.length">
            <el-button size="small" @click="handleBatchTag">批量标签</el-button>
            <el-button size="small" @click="handleBatchTransfer">转移</el-button>
          </template>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="customerList" v-loading="loading" stripe @selection-change="selection = $event" max-height="600">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="客户名称" min-width="150">
          <template #default="{row}">
            <el-link type="primary" :underline="'never'" @click="openDetail(row)" class="cell-link">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="等级" width="90">
          <template #default="{row}">
            <el-tag :type="row.levelId===3?'warning':row.levelId===2?'primary':'info'" size="small">{{ row.levelName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="160">
          <template #default="{row}">
            <el-tag v-for="t in (row.tags||[])" :key="t.id" size="small" style="margin-right:4px;margin-bottom:2px">{{ t.name }}</el-tag>
            <span v-if="!row.tags?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column label="来源" width="100">
          <template #default="{row}">{{ row.sourceChannelName || row.sourceChannel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="lastContactAt" label="最后联系" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleTransfer(row)">转移</el-button>
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

    <!-- Form Dialog -->
    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || { levelId: 1 }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="客户名称" prop="name" :rules="[{required:true,message:'请输入名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="省份" prop="province"><el-input v-model="form.province" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="城市" prop="city"><el-input v-model="form.city" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="公司" prop="company"><el-input v-model="form.company" /></el-form-item>
        <el-form-item label="等级" prop="levelId">
          <el-select v-model="form.levelId" style="width:100%">
            <el-option v-for="l in levelOptions" :key="l.id" :label="l.name" :value="l.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </template>
    </FormDialog>

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" :title="detailCustomer?.name || '客户详情'" size="700px" destroy-on-close @open="loadDetail">
      <template v-if="detailCustomer">
        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item"><span class="label">手机号</span><span class="value">{{ detailCustomer.phone || '-' }}</span></div>
            <div class="detail-item"><span class="label">等级</span><span class="value"><el-tag size="small">{{ detailCustomer.levelName || '-' }}</el-tag></span></div>
            <div class="detail-item"><span class="label">负责人</span><span class="value">{{ detailCustomer.ownerName || '-' }}</span></div>
            <div class="detail-item"><span class="label">公司</span><span class="value">{{ detailCustomer.company || '-' }}</span></div>
            <div class="detail-item"><span class="label">省份</span><span class="value">{{ detailCustomer.province || '-' }}</span></div>
            <div class="detail-item"><span class="label">城市</span><span class="value">{{ detailCustomer.city || '-' }}</span></div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-header">
            <h4 class="section-title">标签</h4>
            <el-button link type="primary" size="small" @click="showTagSelector = true">编辑标签</el-button>
          </div>
          <div class="tag-list">
            <el-tag v-for="t in detailTagList" :key="t.id" size="small" closable :disable-transitions @close="handleDetailRemoveTag(t.id)" style="margin-right:6px;margin-bottom:4px">{{ t.name }}</el-tag>
            <span v-if="!detailTagList.length" class="text-secondary">暂无标签</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-header">
            <h4 class="section-title">最近跟进</h4>
            <el-button link type="primary" size="small" @click="followUpDialogVisible = true">写跟进</el-button>
          </div>
          <div v-for="f in detailFollowUps" :key="f.id" class="timeline-item">
            <div class="timeline-dot" />
            <div class="timeline-content">
              <p>{{ f.content }}</p>
              <span class="timeline-meta">{{ f.creatorName }} · {{ f.createdAt }}</span>
            </div>
          </div>
          <el-empty v-if="!detailFollowUps.length" description="暂无跟进" :image-size="50" />
        </div>
        <div class="detail-section">
          <div class="section-header">
            <h4 class="section-title">关联商机</h4>
            <el-button link type="primary" size="small" @click="oppDialogVisible = true">新建商机</el-button>
          </div>
          <el-table :data="detailOpportunities" size="small" max-height="200">
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column prop="stageName" label="阶段" width="100" />
            <el-table-column prop="expectedAmount" label="金额" width="100"><template #default="{row}">¥{{ row.expectedAmount }}</template></el-table-column>
          </el-table>
          <el-empty v-if="!detailOpportunities.length" description="暂无商机" :image-size="50" />
        </div>
        <div class="detail-section">
          <h4 class="section-title">联系人</h4>
          <div v-for="c in detailContacts" :key="c.id" class="contact-item">
            <el-avatar :size="32">{{ c.name.charAt(0) }}</el-avatar>
            <div class="contact-info"><span class="contact-name">{{ c.name }}</span><span class="contact-pos">{{ c.position }}</span></div>
            <el-tag v-if="c.isDecisionMaker" size="small" type="warning" effect="plain">决策人</el-tag>
          </div>
          <el-empty v-if="!detailContacts.length" description="暂无联系人" :image-size="50" />
        </div>
      </template>
    </el-drawer>

    <!-- Transfer Dialog -->
    <el-dialog v-model="transferDialogVisible" title="转移客户负责人" width="420px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="转移数量">
          <el-tag type="info">{{ transferTargetIds.length }} 个客户</el-tag>
        </el-form-item>
        <el-form-item label="新负责人" :rules="[{required:true,message:'请选择新负责人'}]">
          <el-select v-model="transferForm.newOwnerId" placeholder="请选择新负责人" style="width:100%" filterable>
            <el-option v-for="u in userOptions" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleTransferSubmit">确认转移</el-button>
      </template>
    </el-dialog>
    <!-- Tag Selector Dialog -->
    <el-dialog v-model="showTagSelector" title="编辑标签" width="420px" :close-on-click-modal="false" @open="editTagIds = [...(detailCustomer?.tagIds || [])]">
      <el-select v-model="editTagIds" multiple style="width:100%" placeholder="选择标签">
        <el-option v-for="t in tagOptions" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <template #footer>
        <el-button @click="showTagSelector = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSaveTags">保存</el-button>
      </template>
    </el-dialog>

    <!-- Follow-up Dialog -->
    <el-dialog v-model="followUpDialogVisible" title="写跟进" width="480px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="跟进类型">
          <el-select v-model="followUpForm.type" style="width:100%">
            <el-option label="电话" value="call" />
            <el-option label="拜访" value="visit" />
            <el-option label="邮件" value="mail" />
            <el-option label="会议" value="meeting" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" :rules="[{required:true,message:'请输入跟进内容'}]">
          <el-input v-model="followUpForm.content" type="textarea" :rows="4" placeholder="请描述跟进情况..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followUpDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleAddFollowUp">保存</el-button>
      </template>
    </el-dialog>

    <!-- Opportunity Dialog -->
    <el-dialog v-model="oppDialogVisible" title="新建商机" width="480px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="商机名称" :rules="[{required:true,message:'请输入商机名称'}]">
          <el-input v-model="oppForm.name" placeholder="例如：CRM系统采购" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="销售阶段" :rules="[{required:true,message:'请选择阶段'}]">
              <el-select v-model="oppForm.stageId" style="width:100%">
                <el-option v-for="s in stageOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计金额">
              <el-input-number v-model="oppForm.expectedAmount" :min="0" :step="1000" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预计成交">
              <el-date-picker v-model="oppForm.expectedCloseDate" type="date" style="width:100%" value-format="YYYY-MM-DD" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赢率">
              <el-tag v-if="oppForm.stageId" type="primary">{{ stageOptions.find(s => s.id === oppForm.stageId)?.probability ?? 0 }}%</el-tag>
              <span v-else class="text-secondary">选择阶段后显示</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="oppForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="oppDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleAddOpportunity">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Download, ArrowDown } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getCustomerPage, createCustomer, updateCustomer, deleteCustomer, getCustomerDetail, getCustomerLevels, getAllTags, getContactsByCustomer, transferCustomer, batchTagCustomers, removeBatchTags } from '@/api/modules/customer'
import { createFollowUp, createOpportunity, getOpportunityStages } from '@/api/modules/sales'
import { getUserPage } from '@/api/modules/system/user'
import type { Customer, Contact } from '@/types'

const loading = ref(false)
const customerList = ref<Customer[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Customer | null>(null)
const selection = ref<Customer[]>([])
const drawerVisible = ref(false)
const transferDialogVisible = ref(false)
const transferTargetIds = ref<number[]>([])
const transferForm = reactive({ newOwnerId: undefined as number | undefined })
const isBatchTransfer = ref(false)
const detailCustomer = ref<Customer | null>(null)
const detailFollowUps = ref<any[]>([])
const detailOpportunities = ref<any[]>([])
const detailContacts = ref<Contact[]>([])

const detailTagList = computed(() => {
  const ids = detailCustomer.value?.tagIds || []
  return tagOptions.value.filter(t => ids.includes(t.id))
})

const followUpDialogVisible = ref(false)
const followUpForm = reactive({ type: 'call' as string, content: '' })
const oppDialogVisible = ref(false)
const oppForm = reactive({ name: '', stageId: undefined as number | undefined, expectedAmount: 0, expectedCloseDate: '', remark: '' })
const stageOptions = ref<{id:number;name:string;probability:number}[]>([])
const showTagSelector = ref(false)
const editTagIds = ref<number[]>([])

const currentUserId = computed(() => {
  try {
    const info = localStorage.getItem('userInfo')
    return info ? JSON.parse(info).userId : 0
  } catch { return 0 }
})

const levelOptions = ref<{id:number;name:string}[]>([])
const tagOptions = ref<{id:number;name:string}[]>([])
const userOptions = ref<{id:number;realName:string}[]>([])

const queryParams = reactive({
  page: 1, size: 20, keywords: '', levelId: undefined as number | undefined,
  ownerId: undefined as number | undefined, tagIds: [] as number[],
})

const dialogTitle = computed(() => editingRow.value ? '编辑客户' : '新增客户')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', levelId: undefined, ownerId: undefined, tagIds: [], page: 1 }); fetchData() }
function openDialog(row?: Customer) { editingRow.value = row || null; dialogVisible.value = true }

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateCustomer(editingRow.value.id, formData)
    } else {
      await createCustomer(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

function handleTransfer(row: Customer) {
  isBatchTransfer.value = false
  transferTargetIds.value = [row.id]
  transferForm.newOwnerId = undefined
  transferDialogVisible.value = true
}
function handleBatchTag() { ElMessage.info('功能开发中') }
function handleBatchTransfer() {
  if (!selection.value.length) return
  isBatchTransfer.value = true
  transferTargetIds.value = selection.value.map(s => s.id)
  transferForm.newOwnerId = undefined
  transferDialogVisible.value = true
}

async function handleTransferSubmit() {
  if (!transferForm.newOwnerId) {
    ElMessage.warning('请选择新负责人')
    return
  }
  if (!transferTargetIds.value.length) return
  try {
    for (const id of transferTargetIds.value) {
      await transferCustomer(id, transferForm.newOwnerId, true)
    }
    ElMessage.success(`已将 ${transferTargetIds.value.length} 个客户转移给新负责人`)
    transferDialogVisible.value = false
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: Customer) {
  try {
    await deleteCustomer(row.id)
    ElMessage.success(`已删除「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

function openDetail(row: Customer) {
  detailCustomer.value = row
  detailFollowUps.value = []
  detailOpportunities.value = []
  detailContacts.value = []
  drawerVisible.value = true
}

async function loadDetail() {
  if (!detailCustomer.value) return

  // 加载销售阶段选项（如果还没加载过）
  if (!stageOptions.value.length) {
    try {
      const res = await getOpportunityStages()
      stageOptions.value = res.data.map((s: any) => ({ id: s.id, name: s.name, probability: s.probability }))
    } catch { /* ignore */ }
  }

  try {
    const res = await getCustomerDetail(detailCustomer.value.id)
    const d = res.data as any
    if (d.customer) detailCustomer.value = d.customer
    if (d.recentFollowUps) detailFollowUps.value = d.recentFollowUps
    if (d.opportunities) detailOpportunities.value = d.opportunities
    if (d.contacts) detailContacts.value = d.contacts
  } catch { /* detail load failed, show basic info */ }
  // always try contacts endpoint
  try {
    const res = await getContactsByCustomer(detailCustomer.value.id)
    detailContacts.value = (res.data as any).records || res.data || []
  } catch { /* ignore */ }
}

async function handleDetailRemoveTag(tagId: number) {
  if (!detailCustomer.value) return
  try {
    await removeBatchTags({ customerIds: [detailCustomer.value.id], tagIds: [tagId] })
    ElMessage.success('已移除标签')
    await loadDetail()
  } catch { /* handled by interceptor */ }
}

async function handleSaveTags() {
  if (!detailCustomer.value) return
  try {
    await batchTagCustomers({ customerIds: [detailCustomer.value.id], tagIds: editTagIds.value, mode: 'overwrite' })
    ElMessage.success('标签已更新')
    showTagSelector.value = false
    await loadDetail()
  } catch { /* handled by interceptor */ }
}

async function handleAddFollowUp() {
  if (!followUpForm.content.trim()) {
    ElMessage.warning('请输入跟进内容')
    return
  }
  if (!detailCustomer.value) return
  try {
    await createFollowUp({
      customerId: detailCustomer.value.id,
      type: followUpForm.type,
      content: followUpForm.content,
    })
    ElMessage.success('跟进记录已添加')
    followUpDialogVisible.value = false
    followUpForm.content = ''
    await loadDetail()
  } catch { /* handled by interceptor */ }
}

async function handleAddOpportunity() {
  if (!oppForm.name.trim()) {
    ElMessage.warning('请输入商机名称')
    return
  }
  if (!oppForm.stageId) {
    ElMessage.warning('请选择销售阶段')
    return
  }
  if (!detailCustomer.value) return
  try {
    await createOpportunity({
      customerId: detailCustomer.value.id,
      name: oppForm.name,
      stageId: oppForm.stageId,
      expectedAmount: oppForm.expectedAmount,
      expectedCloseDate: oppForm.expectedCloseDate || undefined,
      remark: oppForm.remark || undefined,
    })
    ElMessage.success('商机已创建')
    oppDialogVisible.value = false
    oppForm.name = ''
    oppForm.expectedAmount = 0
    oppForm.expectedCloseDate = ''
    oppForm.remark = ''
    await loadDetail()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page, size: queryParams.size,
    }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.levelId) params.levelId = queryParams.levelId
    if (queryParams.ownerId) params.ownerId = queryParams.ownerId
    if (queryParams.tagIds?.length) params.tagIds = queryParams.tagIds.join(',')
    const res = await getCustomerPage(params as any)
    customerList.value = res.data.records
    total.value = Number(res.data.total ?? 0)
  } finally { loading.value = false }
}

async function fetchOptions() {
  try {
    const [levelRes, tagRes, userRes] = await Promise.all([
      getCustomerLevels(),
      getAllTags(),
      getUserPage({ page: 1, size: 999 }),
    ])
    levelOptions.value = levelRes.data.map((l: any) => ({ id: l.id, name: l.name }))
    tagOptions.value = tagRes.data.map((t: any) => ({ id: t.id, name: t.name }))
    userOptions.value = userRes.data.records.map((u: any) => ({ id: u.id, realName: u.realName }))
  } catch { /* options load failed */ }
}

onMounted(() => { fetchData(); fetchOptions() })
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
.cell-link { font-weight:500; }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }

.detail-section { margin-bottom:24px; }
.tag-list { min-height:24px; }
.text-secondary { font-size:13px; color:var(--crm-text-secondary); }
.section-title { font-size:14px; font-weight:600; color:var(--crm-text-primary); margin:0 0 12px; padding-bottom:8px; border-bottom:1px solid var(--crm-border-light); }
.section-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; padding-bottom:8px; border-bottom:1px solid var(--crm-border-light);
  .section-title { margin:0; padding:0; border:none; }
}
.detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.detail-item { display:flex; flex-direction:column; gap:4px;
  .label { font-size:12px; color:var(--crm-text-secondary); }
  .value { font-size:14px; color:var(--crm-text-primary); }
}
.timeline-item { display:flex; gap:12px; padding:8px 0; position:relative;
  .timeline-dot { width:8px; height:8px; border-radius:50%; background:var(--crm-primary-lighter); border:2px solid var(--crm-primary); flex-shrink:0; margin-top:5px; }
  .timeline-content { flex:1;
    p { margin:0 0 4px; font-size:13px; color:var(--crm-text-primary); }
    .timeline-meta { font-size:12px; color:var(--crm-text-secondary); }
  }
}
.contact-item { display:flex; align-items:center; gap:12px; padding:8px 0; border-bottom:1px solid var(--crm-border-light);
  .contact-info { flex:1; display:flex; flex-direction:column; }
  .contact-name { font-size:13px; font-weight:500; }
  .contact-pos { font-size:12px; color:var(--crm-text-secondary); }
}
</style>
