<template>
  <div class="approval-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">审批管理</h2>
        <p class="page-subtitle">集中处理请假、报销、采购等审批流程</p>
      </div>
      <div class="page-header-right" />
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="审批标题" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="queryParams.approvalType" placeholder="全部" clearable style="width:130px">
                <el-option label="请假" value="leave" />
                <el-option label="报销" value="expense" />
                <el-option label="采购" value="purchase" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="待审批" value="pending" />
                <el-option label="已通过" value="approved" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
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
          <span class="result-count">共 <b>{{ total }}</b> 条审批记录</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="approvalList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="title" label="审批标题" min-width="200">
          <template #default="{row}"><span class="cell-name">{{ row.title }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{row}">
            <el-tag :type="approvalTypeMap[row.approvalType]?.type || 'info'" size="small" effect="light">
              {{ approvalTypeMap[row.approvalType]?.label || row.approvalType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column label="金额" width="130">
          <template #default="{row}">
            <span v-if="row.amount">¥{{ row.amount.toLocaleString() }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" effect="light">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column prop="completedAt" label="完成时间" width="170">
          <template #default="{row}">{{ row.completedAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <template v-if="row.status === 'pending'">
              <el-button link type="success" size="small" @click="handleApprove(row)">审批通过</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="审批详情" width="560px" destroy-on-close>
      <template v-if="detailRow">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">审批标题</span>
            <span class="detail-value">{{ detailRow.title }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">类型</span>
            <span class="detail-value">
              <el-tag :type="approvalTypeMap[detailRow.approvalType]?.type" size="small">
                {{ approvalTypeMap[detailRow.approvalType]?.label }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">申请人</span>
            <span class="detail-value">{{ detailRow.applicantName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">金额</span>
            <span class="detail-value">{{ detailRow.amount ? '¥' + detailRow.amount.toLocaleString() : '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <span class="detail-value">
              <el-tag :type="statusMap[detailRow.status]?.type" size="small">{{ statusMap[detailRow.status]?.label }}</el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间</span>
            <span class="detail-value">{{ detailRow.createdAt }}</span>
          </div>
        </div>
        <div v-if="detailRow.remark" class="detail-remark">
          <span class="detail-label">备注</span>
          <p>{{ detailRow.remark }}</p>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getApprovalInstancePage, approveApprovalInstance, rejectApprovalInstance } from '@/api/modules/collaboration'

interface Approval {
  id: number; title: string; approvalType: string;
  applicantName: string; amount?: number;
  status: string; remark?: string;
  createdAt: string; completedAt?: string;
}

const approvalTypeMap: Record<string, {label:string;type:string}> = {
  leave: { label: '请假', type: 'primary' },
  expense: { label: '报销', type: 'success' },
  purchase: { label: '采购', type: 'warning' },
  other: { label: '其他', type: 'info' },
}

const statusMap: Record<string, {label:string;type:string}> = {
  pending: { label: '待审批', type: 'info' },
  approved: { label: '已通过', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' },
}

const loading = ref(false)
const approvalList = ref<Approval[]>([])
const total = ref(0)
const showSearch = ref(true)
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const detailRow = ref<Approval | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  approvalType: undefined as string | undefined,
  status: undefined as string | undefined,
})

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', approvalType: undefined, status: undefined, page: 1 })
  dateRange.value = []
  fetchData()
}

function handleView(row: Approval) {
  detailRow.value = row
  detailVisible.value = true
}

function handleApprove(row: Approval) {
  ElMessageBox.confirm(`确认通过审批「${row.title}」？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'success',
  }).then(async () => {
    try {
      await approveApprovalInstance(row.id)
      ElMessage.success('审批已通过')
      fetchData()
    } catch { /* handled by interceptor */ }
  }).catch(() => {})
}

function handleReject(row: Approval) {
  ElMessageBox.prompt(`请输入驳回原因`, '驳回审批', {
    confirmButtonText: '确定', cancelButtonText: '取消', inputType: 'textarea',
  }).then(async ({ value }) => {
    try {
      await rejectApprovalInstance(row.id, value || '')
      ElMessage.success('已驳回')
      fetchData()
    } catch { /* handled by interceptor */ }
  }).catch(() => {})
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.approvalType) params.bizType = queryParams.approvalType
    if (queryParams.status) params.status = queryParams.status
    if (dateRange.value.length === 2) {
      params.dateFrom = dateRange.value[0]
      params.dateTo = dateRange.value[1]
    }

    const res = await getApprovalInstancePage(params)
    if (res?.data) {
      approvalList.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        title: item.formData?.title || item.bizType || '审批申请',
        approvalType: item.bizType || 'other',
        applicantName: item.applicantName || `用户${item.applicantId ?? ''}`,
        amount: item.formData?.amount ? Number(item.formData.amount) : undefined,
        status: item.status || 'pending',
        remark: item.formData?.remark || item.formData?.reason || '',
        createdAt: item.createdAt,
        completedAt: ['approved', 'rejected'].includes(item.status) ? (item.updatedAt || item.createdAt) : undefined,
      }))
      total.value = Number(res.data.total ?? 0)
    }
  } catch (e) {
    console.error('审批数据加载失败:', e)
    ElMessage.error('审批数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.approval-page { max-width: 1400px; }
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

/* Detail Dialog */
.detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.detail-item { display:flex; flex-direction:column; gap:4px; }
.detail-label { font-size:12px; color:var(--crm-text-secondary); }
.detail-value { font-size:14px; color:var(--crm-text-primary); }
.detail-remark { margin-top:16px; padding-top:16px; border-top:1px solid var(--crm-border-light);
  .detail-label { display:block; margin-bottom:8px; }
  p { margin:0; font-size:14px; color:var(--crm-text-primary); line-height:1.6; }
}
</style>
