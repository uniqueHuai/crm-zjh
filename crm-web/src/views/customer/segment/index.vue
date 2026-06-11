<template>
  <div class="segment-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">客户分群</h2>
        <p class="page-subtitle">通过条件筛选创建客户群体，实现精细化运营</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增分群</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="分群名称" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个客户分群</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="segmentList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="分群名称" min-width="160">
          <template #default="{row}"><span class="cell-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="memberCount" label="客户数" width="90" align="center">
          <template #default="{row}">{{ row.memberCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastRefreshAt" label="最近刷新" width="170">
          <template #default="{row}">{{ row.lastRefreshAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleRefresh(row)">刷新人群</el-button>
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="{ status: 1, conditions: [] }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="分群名称" prop="name" :rules="[{required:true,message:'请输入分群名称'}]">
          <el-input v-model="form.name" placeholder="分群名称" />
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
import { getSegmentPage, createSegment, updateSegment, deleteSegment, refreshSegment } from '@/api/modules/customer'

interface SegmentItem {
  id: number; name: string; memberCount: number;
  status: number; lastRefreshAt: string; updatedAt: string; createdAt: string;
}

const loading = ref(false)
const segmentList = ref<SegmentItem[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<SegmentItem | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  status: undefined as number | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑分群' : '新增分群')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', status: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: SegmentItem) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateSegment(editingRow.value.id, formData)
    } else {
      await createSegment(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleRefresh(row: SegmentItem) {
  try {
    await refreshSegment(row.id)
    ElMessage.success(`已刷新人群「${row.name}」`)
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: SegmentItem) {
  try {
    await deleteSegment(row.id)
    ElMessage.success(`已删除分群「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.status !== undefined) params.status = queryParams.status
    const res = await getSegmentPage(params as any)
    segmentList.value = res.data.records
    total.value = Number(res.data.total ?? 0)
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.segment-page { max-width: 1400px; }
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
