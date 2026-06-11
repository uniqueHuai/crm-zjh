<template>
  <div class="custom-report-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">自定义报表</h2>
        <p class="page-subtitle">按需创建数据报表，灵活分析业务数据</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增报表</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="报表名称" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="queryParams.chartType" placeholder="全部" clearable style="width:130px">
                <el-option label="表格" value="table" />
                <el-option label="图表" value="chart" />
                <el-option label="数据透视" value="pivot" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个自定义报表</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="reportList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="报表名称" min-width="180">
          <template #default="{row}"><span class="cell-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{row}">
            <el-tag :type="chartTypeMap[row.chartType]?.type || 'info'" size="small" effect="light">
              {{ chartTypeMap[row.chartType]?.label || row.chartType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSource" label="数据源" width="130" />
        <el-table-column prop="createdBy" label="创建人" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleViewData(row)">查看数据</el-button>
            <el-button link type="primary" size="small" @click="handleExport(row)">导出</el-button>
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="报表名称" prop="name" :rules="[{required:true,message:'请输入报表名称'}]">
          <el-input v-model="form.name" placeholder="报表名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="chartType" :rules="[{required:true,message:'请选择报表类型'}]">
              <el-select v-model="form.chartType" style="width:100%">
                <el-option label="表格" value="table" />
                <el-option label="图表" value="chart" />
                <el-option label="数据透视" value="pivot" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据源" prop="dataSource" :rules="[{required:true,message:'请选择数据源'}]">
              <el-select v-model="form.dataSource" style="width:100%">
                <el-option label="客户数据" value="客户数据" />
                <el-option label="商机数据" value="商机数据" />
                <el-option label="合同数据" value="合同数据" />
                <el-option label="工单数据" value="工单数据" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">停用</el-radio>
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
import { getCustomReports, createCustomReport, updateCustomReport, deleteCustomReport, getCustomReportData, exportCustomReport } from '@/api/modules/report'

interface CustomReport {
  id: number; name: string; chartType: string;
  dataSource: string; status: string;
  createdAt: string; updatedAt: string;
  createdBy?: number;
}

const chartTypeMap: Record<string, {label:string;type:'primary'|'success'|'warning'|'info'|'danger'}> = {
  table: { label: '表格', type: 'primary' },
  chart: { label: '图表', type: 'success' },
  pivot: { label: '数据透视', type: 'warning' },
}

const loading = ref(false)
const reportList = ref<CustomReport[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<CustomReport | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  chartType: undefined as string | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑报表' : '新增报表')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', chartType: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: CustomReport) { editingRow.value = row || null; dialogVisible.value = true }
async function handleSubmit(formData: any, done: () => void) {
  try {
    const payload = {
      name: formData.name,
      dataSource: formData.dataSource,
      chartType: formData.chartType,
      dimensions: formData.dimensions || [],
      metrics: formData.metrics || [],
      filters: formData.filters || [],
    }
    if (editingRow.value) {
      await updateCustomReport(editingRow.value.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createCustomReport(payload)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    ElMessage.error(editingRow.value ? '修改失败' : '新增失败')
  }
}
async function handleViewData(row: CustomReport) {
  try {
    const res = await getCustomReportData(row.id, { page: 1, size: 20 })
    ElMessage.info(`报表数据共 ${(res.data as any)?.total || 0} 条`)
  } catch {
    ElMessage.error('查询报表数据失败')
  }
}
async function handleExport(row: CustomReport) {
  try {
    await exportCustomReport(row.id, { format: 'excel' })
    ElMessage.success(`正在导出报表「${row.name}」`)
  } catch {
    ElMessage.error('导出失败')
  }
}
async function handleDelete(row: CustomReport) {
  try {
    await deleteCustomReport(row.id)
    ElMessage.success(`已删除报表「${row.name}」`)
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCustomReports({ ...queryParams })
    const pageData = res.data as any
    reportList.value = (pageData?.records || []) as CustomReport[]
    total.value = pageData?.total || 0
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.custom-report-page { max-width: 1400px; }
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
