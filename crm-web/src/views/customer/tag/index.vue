<template>
  <div class="tag-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">标签管理</h2>
        <p class="page-subtitle">管理客户标签，精准分类与营销</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增标签</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="标签名称" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="queryParams.type" placeholder="全部" clearable style="width:130px">
                <el-option label="系统标签" value="auto" />
                <el-option label="自定义标签" value="manual" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个标签</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="tagList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="标签名称" min-width="150">
          <template #default="{row}">
            <span class="cell-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="颜色" width="120">
          <template #default="{row}">
            <div class="color-dot-row">
              <span class="color-dot" :style="{ background: row.color }" />
              <span class="color-value">{{ row.color }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="130">
          <template #default="{row}">
            <el-tag v-if="row.type === 'auto'" size="small" type="primary" effect="plain">系统标签</el-tag>
            <el-tag v-else size="small" type="success" effect="plain">自定义标签</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="{ type: 'manual', color: '#1890ff' }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="标签名称" prop="name" :rules="[{required:true,message:'请输入标签名称'}]">
          <el-input v-model="form.name" placeholder="标签名称" />
        </el-form-item>
        <el-form-item label="颜色" prop="color" :rules="[{required:true,message:'请输入颜色值'}]">
          <el-input v-model="form.color" placeholder="例如: #1890ff" />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">输入十六进制颜色值</span></template>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="auto">系统标签</el-radio>
            <el-radio value="manual">自定义标签</el-radio>
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
import { getTagPage, createTag, updateTag, deleteTag } from '@/api/modules/customer'

interface TagItem {
  id: number; name: string; color: string; type: string;
  remark?: string; createdAt: string;
}

const loading = ref(false)
const tagList = ref<TagItem[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<TagItem | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  type: undefined as string | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑标签' : '新增标签')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', type: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: TagItem) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateTag(editingRow.value.id, formData)
    } else {
      await createTag({ ...formData, status: 1 })
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: TagItem) {
  try {
    await deleteTag(row.id)
    ElMessage.success(`已删除标签「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.type) params.type = queryParams.type
    const res = await getTagPage(params as any)
    tagList.value = (res.data.records || []) as TagItem[]
    total.value = Number(res.data.total ?? 0)
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.tag-page { max-width: 1400px; }
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
.color-dot-row { display:flex; align-items:center; gap:8px; }
.color-dot { width:18px; height:18px; border-radius:50%; border:1px solid var(--crm-border-light); display:inline-block; flex-shrink:0; }
.color-value { font-size:12px; color:var(--crm-text-secondary); font-family:monospace; }
</style>
