<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">字典管理</h2>
        <p class="page-subtitle">管理系统数据字典，定义枚举值与业务分类</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openTypeDialog()"><el-icon><Plus /></el-icon>新增字典</el-button>
      </div>
    </div>

    <div class="card dict-layout">
      <!-- Left: Dict Type -->
      <div class="dict-panel dict-type-panel">
        <div class="panel-header">
          <span class="panel-title">字典类型</span>
        </div>
        <el-collapse-transition>
          <div v-if="showSearch" class="search-form">
            <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
              <el-form-item label="关键字"><el-input v-model="queryParams.keywords" placeholder="搜索字典" clearable style="width:140px" /></el-form-item>
              <el-form-item><el-button type="primary" size="small" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button><el-button size="small" @click="handleReset">重置</el-button></el-form-item>
            </el-form>
          </div>
        </el-collapse-transition>
        <div class="toolbar">
          <div class="toolbar-left"><span class="result-count">共 <b>{{ types.length }}</b> 个</span></div>
          <div class="toolbar-right">
            <el-tooltip content="搜索"><el-button text size="small" @click="showSearch = !showSearch"><el-icon><Search /></el-icon></el-button></el-tooltip>
            <el-tooltip content="刷新"><el-button text size="small" @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
          </div>
        </div>
        <el-table :data="types" v-loading="loading" stripe max-height="500" highlight-current-row @current-change="handleTypeChange">
          <el-table-column prop="typeName" label="字典名称" min-width="50" />
          <el-table-column prop="typeCode" label="字典编码" width="150">
            <template #default="{row}"><el-tag size="small" effect="plain">{{ row.typeCode }}</el-tag></template>
          </el-table-column>
          <el-table-column label="状态" width="70">
            <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{row}">
              <el-button link type="primary" size="small" @click="openTypeDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="handleDeleteType(row)">
                <template #reference><el-button link type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Right: Dict Items -->
      <div class="dict-panel dict-item-panel">
        <div class="panel-header">
          <span class="panel-title">{{ selectedType ? `${selectedType.typeName} - 字典项` : '字典项' }}</span>
          <el-button v-if="selectedType" type="primary" size="small" @click="openItemDialog()"><el-icon><Plus /></el-icon>新增字典项</el-button>
        </div>
        <div class="toolbar">
          <div class="toolbar-left">
            <span v-if="selectedType" class="result-count">共 <b>{{ items.length }}</b> 个字典项</span>
            <span v-else class="result-count text-muted">请先在左侧选择一个字典类型</span>
          </div>
        </div>
        <el-table v-if="selectedType" :data="items" stripe max-height="500">
          <el-table-column prop="itemValue" label="数据标签" min-width="120" />
          <el-table-column prop="itemCode" label="数据值" width="130" />
          <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
          <el-table-column label="状态" width="70">
            <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="标签类型" width="100">
            <template #default="{row}">
              <el-tag v-if="row.cssClass" :type="row.cssClass" size="small">{{ row.cssClass }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{row}">
              <el-button link type="primary" size="small" @click="openItemDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="handleDeleteItem(row)">
                <template #reference><el-button link type="danger" size="small">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="请选择字典类型" />
      </div>
    </div>

    <!-- Type FormDialog -->
    <FormDialog v-model:visible="typeDialogVisible" :title="typeDialogTitle" :initial-data="editingType || { status: 1 }" @submit="handleSubmitType">
      <template #default="{form}">
        <el-form-item label="字典名称" prop="typeName" :rules="[{required:true,message:'请输入名称'}]"><el-input v-model="form.typeName" /></el-form-item>
        <el-form-item label="字典编码" prop="typeCode" :rules="[{required:true,message:'请输入编码'}]"><el-input v-model="form.typeCode" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </template>
    </FormDialog>

    <!-- Item FormDialog -->
    <FormDialog v-model:visible="itemDialogVisible" :title="itemDialogTitle" :initial-data="editingItem || { status: 1 }" @submit="handleSubmitItem">
      <template #default="{form}">
        <el-form-item label="数据标签" prop="itemValue" :rules="[{required:true,message:'请输入标签'}]"><el-input v-model="form.itemValue" /></el-form-item>
        <el-form-item label="数据值" prop="itemCode" :rules="[{required:true,message:'请输入值'}]"><el-input v-model="form.itemCode" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="排序号" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="标签类型" prop="cssClass">
          <el-select v-model="form.cssClass" clearable style="width:100%">
            <el-option label="默认" value="" />
            <el-option label="成功" value="success" />
            <el-option label="信息" value="info" />
            <el-option label="警告" value="warning" />
            <el-option label="危险" value="danger" />
          </el-select>
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
import {
  getDictTypePage, createDictType, updateDictType, deleteDictType,
  getDictItems, createDictItem, updateDictItem, deleteDictItem,
} from '@/api/modules/system/dict'
import type { SysDictType, SysDictItem } from '@/api/modules/system/dict'

const loading = ref(false)
const showSearch = ref(true)
const types = ref<SysDictType[]>([])
const items = ref<SysDictItem[]>([])
const selectedType = ref<SysDictType | null>(null)
const typeDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const editingType = ref<SysDictType | null>(null)
const editingItem = ref<SysDictItem | null>(null)

const queryParams = reactive({ keywords:'' })
const typeDialogTitle = computed(() => editingType.value ? '编辑字典' : '新增字典')
const itemDialogTitle = computed(() => editingItem.value ? '编辑字典项' : '新增字典项')

function handleSearch() { fetchData() }
function handleReset() { queryParams.keywords = ''; fetchData() }

function handleTypeChange(type: SysDictType | null) {
  selectedType.value = type
  if (type) {
    fetchItems(type.typeCode)
  } else {
    items.value = []
  }
}

function openTypeDialog(row?: SysDictType) {
  editingType.value = row || null
  typeDialogVisible.value = true
}

async function handleSubmitType(formData: any, done: () => void) {
  try {
    if (editingType.value) {
      await updateDictType(editingType.value.id, formData)
    } else {
      await createDictType(formData)
    }
    ElMessage.success(editingType.value ? '字典修改成功' : '字典新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDeleteType(row: SysDictType) {
  try {
    await deleteDictType(row.id)
    ElMessage.success(`已删除字典「${row.typeName}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

function openItemDialog(row?: SysDictItem) {
  editingItem.value = row || null
  itemDialogVisible.value = true
}

async function handleSubmitItem(formData: any, done: () => void) {
  try {
    if (editingItem.value) {
      await updateDictItem(editingItem.value.id, formData)
    } else if (selectedType.value) {
      await createDictItem(selectedType.value.typeCode, formData)
    }
    ElMessage.success(editingItem.value ? '字典项修改成功' : '字典项新增成功')
    done()
    if (selectedType.value) {
      fetchItems(selectedType.value.typeCode)
    }
  } catch { done() }
}

async function handleDeleteItem(row: SysDictItem) {
  try {
    await deleteDictItem(row.id)
    ElMessage.success(`已删除字典项「${row.itemValue}」`)
    if (selectedType.value) {
      fetchItems(selectedType.value.typeCode)
    }
  } catch { /* handled by interceptor */ }
}

async function fetchItems(typeCode: string) {
  try {
    const res = await getDictItems(typeCode)
    items.value = res.data.records
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDictTypePage({ page:1, size:999, keywords: queryParams.keywords })
    types.value = res.data.records
    // Re-select previously selected type
    if (selectedType.value) {
      const found = types.value.find(t => t.id === selectedType.value!.id)
      if (found) {
        selectedType.value = found
      } else {
        selectedType.value = null
        items.value = []
      }
    }
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.sys-page { max-width:1400px; }
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); padding:20px; }
.dict-layout { display:flex; gap:16px; }
.dict-panel { flex:1; min-width:0; }
.dict-type-panel { flex:0 0 45%; }
.dict-item-panel { flex:1; }
.panel-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .panel-title { font-size:15px; font-weight:600; color:var(--crm-text-primary); }
}
.search-form { margin-bottom:12px; padding-bottom:12px; border-bottom:1px solid var(--crm-border-light);
  .el-form{display:flex;flex-wrap:wrap;gap:0} :deep(.el-form-item){margin-bottom:0}
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b{color:var(--crm-text-primary);} }
  &-right { display:flex; align-items:center; gap:4px; }
}
.text-muted { color:var(--crm-text-placeholder); }
</style>
