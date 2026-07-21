<template>
  <div class="contract-template-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">合同模板</h2>
        <p class="page-subtitle">管理合同模板，支持在线预览与下载</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增模板</el-button>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ list.length }}</b> 个模板</span>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="模板名称" min-width="160">
          <template #default="{row}">
            <span class="cell-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="文件" min-width="200">
          <template #default="{row}">
            <el-link v-if="row.fileUrl" :href="row.fileUrl" target="_blank" type="primary" :underline="false">
              <el-icon style="margin-right:4px"><Download /></el-icon>下载文件
            </el-link>
            <span v-else class="cell-empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="字段定义" min-width="200">
          <template #default="{row}">
            <el-tooltip v-if="row.fields" :content="formatJSON(row.fields)" placement="top" :show-after="300">
              <span class="cell-json">{{ previewJSON(row.fields) }}</span>
            </el-tooltip>
            <span v-else class="cell-empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="{ status: 1 }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="模板名称" prop="name" :rules="[{required:true,message:'请输入模板名称'}]">
          <el-input v-model="form.name" placeholder="模板名称" />
        </el-form-item>
        <el-form-item label="文件地址" prop="fileUrl">
          <el-input v-model="form.fileUrl" placeholder="文件访问地址" />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">输入可下载的文件链接</span></template>
        </el-form-item>
        <el-form-item label="字段定义" prop="fields">
          <el-input v-model="form.fields" type="textarea" :rows="4" placeholder='[{"label":"甲方","key":"partyA"}]' />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">输入JSON格式的字段定义</span></template>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Download } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getContractTemplates, createContractTemplate, updateContractTemplate, deleteContractTemplate } from '@/api/modules/sales'

interface ContractTemplate {
  id: number
  name: string
  fileUrl?: string
  fields?: string
  status: number
  createTime: string
  updateTime: string
}

const loading = ref(false)
const list = ref<ContractTemplate[]>([])
const dialogVisible = ref(false)
const editingRow = ref<ContractTemplate | null>(null)

const dialogTitle = computed(() => editingRow.value ? '编辑模板' : '新增模板')

function openDialog(row?: ContractTemplate) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    const payload = { ...formData }
    if (editingRow.value) {
      await updateContractTemplate(editingRow.value.id, payload)
    } else {
      await createContractTemplate(payload)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: ContractTemplate) {
  try {
    await deleteContractTemplate(row.id)
    ElMessage.success(`已删除模板「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

function formatJSON(value: any): string {
  if (typeof value === 'string') {
    try { return JSON.stringify(JSON.parse(value), null, 2) } catch { return value }
  }
  return JSON.stringify(value, null, 2)
}

function previewJSON(value: any): string {
  const text = typeof value === 'string' ? value : JSON.stringify(value)
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getContractTemplates()
    list.value = (res.data || []) as ContractTemplate[]
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); padding:20px; }
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b { color:var(--crm-text-primary); } }
  &-right { display:flex; align-items:center; gap:4px; }
}
.cell-name { font-weight:500; color:var(--crm-text-primary); }
.cell-empty { color:var(--crm-text-tertiary); }
.cell-json { font-size:12px; color:var(--crm-text-secondary); font-family:monospace; cursor:default; }
</style>
