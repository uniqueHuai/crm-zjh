<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">商机列表</h2>
        <p class="page-subtitle">跟踪所有销售机会，推动成交转化</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增商机</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词"><el-input v-model="queryParams.keywords" placeholder="商机名称/客户" clearable style="width:160px" /></el-form-item>
            <el-form-item label="阶段"><el-select v-model="queryParams.stageId" placeholder="全部" clearable style="width:130px">
              <el-option v-for="s in stageOptions" :key="s.id" :label="s.name" :value="s.id" />
            </el-select></el-form-item>
            <el-form-item label="负责人"><el-select v-model="queryParams.ownerId" placeholder="全部" clearable style="width:120px">
              <el-option v-for="u in userOptions" :key="u.id" :label="u.realName" :value="u.id" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个商机</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="opportunityList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="商机名称" min-width="160">
          <template #default="{row}">
            <el-link type="primary" :underline="false" class="cell-link">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" width="130" />
        <el-table-column prop="stageName" label="阶段" width="110">
          <template #default="{row}">
            <el-tag :type="stageTagType(row.stageId)" size="small">{{ row.stageName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedAmount" label="预计金额" width="130">
          <template #default="{row}">¥{{ (row.expectedAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="probability" label="赢率" width="80">
          <template #default="{row}">{{ row.probability ?? '-' }}%</template>
        </el-table-column>
        <el-table-column prop="expectedCloseDate" label="预计成交" width="110" />
        <el-table-column prop="ownerName" label="负责人" width="90" />
        <el-table-column prop="createdAt" label="创建时间" width="110" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || { stageId: stageOptions[0]?.id }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="商机名称" prop="name" :rules="[{required:true,message:'请输入商机名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户" prop="customerId" :rules="[{required:true,message:'请选择客户'}]"><el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="负责人" prop="ownerId" :rules="[{required:true,message:'请选择负责人'}]"><el-select v-model="form.ownerId" style="width:100%">
            <el-option v-for="u in userOptions" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="销售阶段" prop="stageId"><el-select v-model="form.stageId" style="width:100%">
            <el-option v-for="s in stageOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="赢率">
            <el-tag type="primary" style="height:32px;line-height:32px">{{ stageProbability(form.stageId) }}%</el-tag>
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="预计金额" prop="expectedAmount"><el-input-number v-model="form.expectedAmount" :min="0" :step="1000" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预计成交日期" prop="expectedCloseDate"><el-date-picker v-model="form.expectedCloseDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getOpportunityPage, createOpportunity, updateOpportunity, deleteOpportunity, getOpportunityStages, type OpportunityStage } from '@/api/modules/sales'
import { getCustomerPage, type Customer } from '@/api/modules/customer'
import { getUserPage, type SysUser } from '@/api/modules/user'

interface Opportunity {
  id: number; name: string; customerId?: number; customerName: string;
  stageId: number; stageName: string; expectedAmount: number;
  probability?: number; expectedCloseDate: string;
  ownerId?: number; ownerName: string; createdAt: string; remark?: string;
}

const stageOptions = ref<OpportunityStage[]>([])
const customerOptions = ref<{ id: number; name: string }[]>([])
const userOptions = ref<SysUser[]>([])

function stageTagType(stageId: number) {
  if (stageId <= 2) return 'info'
  if (stageId <= 4) return 'primary'
  if (stageId === 5) return 'warning'
  if (stageId === 6) return 'success'
  return 'danger'
}

function stageProbability(stageId: number) {
  return stageOptions.value.find(s => s.id === stageId)?.probability ?? 0
}

const loading = ref(false)
const opportunityList = ref<Opportunity[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Opportunity | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  stageId: undefined as number | undefined,
  ownerId: undefined as number | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑商机' : '新增商机')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', stageId: undefined, ownerId: undefined, page: 1 }); fetchData() }
function openDialog(row?: Opportunity) {
  editingRow.value = row || null
  dialogVisible.value = true
}
async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateOpportunity(editingRow.value.id, formData)
      ElMessage.success('修改成功')
    } else {
      await createOpportunity(formData)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    ElMessage.error(editingRow.value ? '修改失败' : '新增失败')
  }
}
async function handleDelete(row: Opportunity) {
  try {
    await deleteOpportunity(row.id)
    ElMessage.success(`已删除商机「${row.name}」`)
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getOpportunityPage({ ...queryParams })
    opportunityList.value = (res.data?.records || []) as Opportunity[]
    total.value = Number(res.data?.total ?? 0)
  } finally { loading.value = false }
}

onMounted(() => {
  fetchData()
  getOpportunityStages().then(res => { stageOptions.value = res.data || [] })
  getCustomerPage({ page: 1, size: 999 }).then(res => {
    customerOptions.value = (res.data?.records || []).map((c: Customer) => ({ id: c.id, name: c.name }))
  })
  getUserPage({ page: 1, size: 999 }).then(res => {
    userOptions.value = (res.data?.records || []) as SysUser[]
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
.cell-link { font-weight: 500; }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
