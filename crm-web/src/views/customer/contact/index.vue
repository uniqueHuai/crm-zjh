<template>
  <div class="contact-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">联系人管理</h2>
        <p class="page-subtitle">管理客户联系人信息，维护关键决策人资料</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增联系人</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="姓名/手机" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="所属客户">
              <el-select v-model="queryParams.customerId" placeholder="全部" clearable filterable style="width:180px">
                <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="决策人">
              <el-select v-model="queryParams.isDecisionMaker" placeholder="全部" clearable style="width:110px">
                <el-option label="是" :value="true" />
                <el-option label="否" :value="false" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个联系人</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="contactList" v-loading="loading" stripe max-height="600">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="姓名" width="110">
          <template #default="{row}"><span class="cell-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="position" label="职位" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{row}">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column prop="customerName" label="所属客户" width="120" />
        <el-table-column label="决策人" width="90">
          <template #default="{row}">
            <el-tag v-if="row.isDecisionMaker" type="warning" size="small" effect="dark">决策人</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="110">
          <template #default="{row}">{{ row.birthday || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150">
          <template #default="{row}">{{ row.remark || '-' }}</template>
        </el-table-column>
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || { customerId: undefined, isDecisionMaker: false }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="姓名" prop="name" :rules="[{required:true,message:'请输入联系人姓名'}]">
          <el-input v-model="form.name" placeholder="联系人姓名" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="手机号" maxlength="11" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="职位" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="邮箱" prop="email" :rules="[{type:'email',message:'格式不正确',trigger:'blur'}]">
          <el-input v-model="form.email" placeholder="电子邮箱" />
        </el-form-item>
        <el-form-item label="所属客户" prop="customerId" :rules="[{required:true,message:'请选择所属客户'}]">
          <el-select v-model="form.customerId" filterable style="width:100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="决策人" prop="isDecisionMaker">
          <el-switch v-model="form.isDecisionMaker" :active-value="true" :inactive-value="false" />
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getContactPage, createContact, updateContact, deleteContact, getCustomerPage } from '@/api/modules/customer'
import type { Contact } from '@/types'

const loading = ref(false)
const contactList = ref<Contact[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Contact | null>(null)
const customerOptions = ref<{ id: number; name: string }[]>([])

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  customerId: undefined as number | undefined,
  isDecisionMaker: undefined as boolean | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑联系人' : '新增联系人')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', customerId: undefined, isDecisionMaker: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: Contact) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateContact(editingRow.value.id, formData)
    } else {
      await createContact(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: Contact) {
  try {
    await deleteContact(row.id)
    ElMessage.success(`已删除联系人「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.customerId) params.customerId = queryParams.customerId
    if (queryParams.isDecisionMaker !== undefined) params.isDecisionMaker = queryParams.isDecisionMaker
    const res = await getContactPage(params as any)
    contactList.value = res.data.records
    total.value = Number(res.data.total ?? 0)
  } finally { loading.value = false }
}

async function fetchCustomerOptions() {
  try {
    const res = await getCustomerPage({ page: 1, size: 999 })
    customerOptions.value = res.data.records.map((c: any) => ({ id: c.id, name: c.name }))
  } catch { /* ignore */ }
}

onMounted(() => { fetchData(); fetchCustomerOptions() })
</script>

<style scoped lang="scss">
.contact-page { max-width: 1400px; }
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
