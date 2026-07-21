<template>
  <div class="auto-rule-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">自动标签规则</h2>
        <p class="page-subtitle">配置自动化规则，根据客户行为自动打标签</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增规则</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="规则名称" clearable style="width:160px" />
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
          <span class="result-count">共 <b>{{ total }}</b> 条规则</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="ruleList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="ruleName" label="规则名称" min-width="150">
          <template #default="{row}">
            <span class="cell-name">{{ row.ruleName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联标签" min-width="140">
          <template #default="{row}">
            <el-tag size="small" :type="tagMap[row.tagId] ? 'primary' : 'info'" effect="plain">
              {{ tagMap[row.tagId] || '未知标签' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="schedule" label="执行频率" width="140" />
        <el-table-column prop="lastExecuteAt" label="上次执行" width="170">
          <template #default="{row}">
            <span v-if="row.lastExecuteAt" class="cell-time">{{ row.lastExecuteAt }}</span>
            <span v-else class="cell-empty">未执行</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag v-if="row.status === 1" size="small" type="success" effect="plain">启用</el-tag>
            <el-tag v-else size="small" type="info" effect="plain">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
            <el-popconfirm title="确认立即执行规则?" @confirm="handleExecute(row)">
              <template #reference><el-button link type="warning" size="small">执行</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="{ status: 1, schedule: '', conditions: '', tagId: undefined }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="规则名称" prop="ruleName" :rules="[{required:true,message:'请输入规则名称'}]">
          <el-input v-model="form.ruleName" placeholder="自动标签规则名称" />
        </el-form-item>
        <el-form-item label="关联标签" prop="tagId" :rules="[{required:true,message:'请选择关联标签'}]">
          <el-select v-model="form.tagId" placeholder="选择标签" filterable clearable style="width:100%">
            <el-option v-for="t in allTags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行频率" prop="schedule">
          <el-input v-model="form.schedule" placeholder="例如: 0 0 * * * (每日凌晨)" />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">Cron 表达式或文本描述</span></template>
        </el-form-item>
        <el-form-item label="条件配置" prop="conditions">
          <el-input v-model="form.conditions" type="textarea" :rows="4" placeholder='{"field":"tagIds","operator":"contains","value":[1,2,3]}' />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">JSON 格式的规则条件</span></template>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getAllTags, getAutoRulesPage, createAutoRule, updateAutoRule, deleteAutoRule, executeAutoRule } from '@/api/modules/customer'
import type { Tag } from '@/api/modules/customer'

interface AutoRuleItem {
  id: number
  tagId: number
  ruleName: string
  conditions: string
  schedule: string
  lastExecuteAt: string | null
  status: number
  createTime: string
  createBy: string
}

const loading = ref(false)
const ruleList = ref<AutoRuleItem[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<AutoRuleItem | null>(null)
const allTags = ref<Tag[]>([])
const tagMap = ref<Record<number, string>>({})

const queryParams = reactive({
  page: 1,
  size: 20,
  keywords: '',
})

const dialogTitle = computed(() => editingRow.value ? '编辑规则' : '新增规则')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', page: 1 })
  fetchData()
}

function openDialog(row?: AutoRuleItem) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateAutoRule(editingRow.value.id, formData)
    } else {
      await createAutoRule(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: AutoRuleItem) {
  try {
    await deleteAutoRule(row.id)
    ElMessage.success(`已删除规则「${row.ruleName}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleExecute(row: AutoRuleItem) {
  try {
    await executeAutoRule(row.id)
    ElMessage.success(`规则「${row.ruleName}」执行成功`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function loadTags() {
  try {
    const res = await getAllTags()
    allTags.value = (res.data || []) as Tag[]
    tagMap.value = {}
    allTags.value.forEach(t => { if (t.id != null) tagMap.value[t.id] = t.name })
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    const res = await getAutoRulesPage(params as any)
    ruleList.value = (res.data.records || []) as AutoRuleItem[]
    total.value = Number(res.data.total ?? 0)
  } finally { loading.value = false }
}

onMounted(() => {
  loadTags()
  fetchData()
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
.cell-name { font-weight:500; color:var(--crm-text-primary); }
.cell-time { font-size:13px; color:var(--crm-text-secondary); }
.cell-empty { font-size:12px; color:var(--crm-text-placeholder); }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
