<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">参数设置</h2>
        <p class="page-subtitle">管理系统运行参数与全局配置项</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增参数</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键字"><el-input v-model="queryParams.keywords" placeholder="搜索参数名称/键名" clearable style="width:240px" /></el-form-item>
            <el-form-item><el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ total }}</b> 个参数</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="configList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="configName" label="参数名称" width="180" />
        <el-table-column prop="configKey" label="参数键名" width="280">
          <template #default="{row}"><el-tag size="small" effect="plain">{{ row.configKey }}</el-tag></template>
        </el-table-column>
        <el-table-column label="参数键值" min-width="80">
          <template #default="{row}">
            <span class="text-ellipsis" :title="row.configValue">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="内置" width="80">
          <template #default="{row}">
            <el-tag :type="row.configType===0?'primary':'warning'" size="small">
              {{ row.configType===0?'系统':'自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm v-if="row.configType!==0" title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
            <el-tag v-else size="small" type="info">内置</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || { status: 1, configType: 1 }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="参数名称" prop="configName" :rules="[{required:true,message:'请输入参数名称'}]"><el-input v-model="form.configName" /></el-form-item>
        <el-form-item label="参数键名" prop="configKey" :rules="[{required:true,message:'请输入参数键名'}]"><el-input v-model="form.configKey" :disabled="!!editingRow" /></el-form-item>
        <el-form-item label="参数键值" prop="configValue" :rules="[{required:true,message:'请输入参数键值'}]"><el-input v-model="form.configValue" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio :value="0">系统参数</el-radio>
            <el-radio :value="1">自定义参数</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { getConfigPage, createConfig, updateConfig, deleteConfig } from '@/api/modules/system/config'
import type { SysConfig } from '@/api/modules/system/config'

const loading = ref(false)
const configList = ref<SysConfig[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<SysConfig | null>(null)

const queryParams = reactive({ page: 1, size: 20, keywords: '' })
const dialogTitle = computed(() => editingRow.value ? '编辑参数' : '新增参数')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', page: 1 }); fetchData() }

function openDialog(row?: SysConfig) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateConfig(editingRow.value.configKey, formData)
    } else {
      await createConfig(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: SysConfig) {
  try {
    await deleteConfig(row.id)
    ElMessage.success(`已删除参数「${row.configName}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getConfigPage({ page: queryParams.page, size: queryParams.size, keywords: queryParams.keywords })
    configList.value = res.data.records
    total.value = Number(res.data.total ?? 0)
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
.search-form { margin-bottom:16px; padding-bottom:16px; border-bottom:1px solid var(--crm-border-light);
  .el-form{display:flex;flex-wrap:wrap;gap:0} :deep(.el-form-item){margin-bottom:0}
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b{color:var(--crm-text-primary);} }
  &-right { display:flex; align-items:center; gap:4px; }
}
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
.text-ellipsis { display:inline-block; max-width:100%; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; vertical-align:bottom; }
</style>
