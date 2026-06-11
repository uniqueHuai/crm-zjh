<template>
  <div class="order-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">订单管理</h2>
        <p class="page-subtitle">查看和管理客户订单，处理发货与退款</p>
      </div>
      <div class="page-header-right" />
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="订单号/客户" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="待付款" value="pending" />
                <el-option label="已付款" value="paid" />
                <el-option label="已发货" value="shipped" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item label="下单时间">
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
          <span class="result-count">共 <b>{{ total }}</b> 个订单</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="orderList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="orderNo" label="订单号" width="160">
          <template #default="{row}"><span class="cell-name">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="userName" label="客户" width="130" />
        <el-table-column label="商品金额" width="130">
          <template #default="{row}">¥{{ (row.totalAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="实付金额" width="130">
          <template #default="{row}">¥{{ (row.payAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" effect="light">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="handleDetail(row)">查看详情</el-button>
            <el-button v-if="row.status === 'paid'" link type="success" size="small" @click="handleShip(row)">发货</el-button>
            <el-button v-if="row.status === 'pending'" link type="danger" size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px" destroy-on-close>
      <template v-if="detailRow">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">订单号</span>
            <span class="detail-value">{{ detailRow.orderNo }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">客户</span>
            <span class="detail-value">{{ detailRow.userName || detailRow.customerId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">商品金额</span>
            <span class="detail-value" style="font-weight:600">¥{{ (detailRow.totalAmount || 0).toLocaleString() }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">实付金额</span>
            <span class="detail-value" style="font-weight:600;color:var(--crm-primary)">¥{{ (detailRow.payAmount || 0).toLocaleString() }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <span class="detail-value">
              <el-tag :type="statusMap[detailRow.status]?.type" size="small">{{ statusMap[detailRow.status]?.label }}</el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">下单时间</span>
            <span class="detail-value">{{ detailRow.createdAt }}</span>
          </div>
        </div>

        <el-divider />
        <h4 style="margin:0 0 12px">商品明细</h4>
        <el-table :data="detailRow.items || []" size="small" border>
          <el-table-column prop="productName" label="商品名称" min-width="160" />
          <el-table-column label="规格" width="120">
            <template #default="{row:item}">{{ item.skuAttrs ? (Array.isArray(item.skuAttrs) ? item.skuAttrs.map((a:any)=>a.value).join('/') : JSON.stringify(item.skuAttrs)) : '-' }}</template>
          </el-table-column>
          <el-table-column prop="unitPrice" label="单价" width="100">
            <template #default="{row:item}">¥{{ (item.price || 0).toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" align="center" />
          <el-table-column prop="subtotal" label="小计" width="100">
            <template #default="{row:item}">¥{{ (item.subtotal || 0).toLocaleString() }}</template>
          </el-table-column>
        </el-table>

        <div v-if="detailRow.remark" class="detail-remark">
          <span class="detail-label">客户备注</span>
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
import { getOrderPage, getOrderById, updateOrderStatus } from '@/api/modules/mall'

interface Order {
  id: number; orderNo: string; userName: string;
  customerId: number; totalAmount: number; payAmount: number;
  status: string; createdAt: string; remark?: string;
  items?: any[];
}

const statusMap: Record<string, {label:string;type:string}> = {
  pending: { label: '待付款', type: 'warning' },
  paid: { label: '已付款', type: 'primary' },
  shipped: { label: '已发货', type: 'success' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'info' },
}

const loading = ref(false)
const orderList = ref<Order[]>([])
const total = ref(0)
const showSearch = ref(true)
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const detailRow = ref<any>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  status: undefined as string | undefined,
})

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', status: undefined, page: 1 })
  dateRange.value = []
  fetchData()
}

async function handleDetail(row: Order) {
  try {
    const res = await getOrderById(row.id)
    detailRow.value = { ...row, items: res?.data?.items || [] }
    detailVisible.value = true
  } catch { /* handled by interceptor */ }
}

function handleShip(row: Order) {
  ElMessageBox.confirm(`确认发货「${row.orderNo}」？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'success',
  }).then(async () => {
    try {
      await updateOrderStatus(row.id, 'shipped')
      ElMessage.success('已标记为发货状态')
      fetchData()
    } catch { /* handled by interceptor */ }
  }).catch(() => {})
}

function handleCancel(row: Order) {
  ElMessageBox.confirm(`确认取消订单「${row.orderNo}」？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    try {
      await updateOrderStatus(row.id, 'cancelled')
      ElMessage.success('订单已取消')
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
    if (queryParams.status) params.status = queryParams.status
    if (dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const res = await getOrderPage(params)
    if (res?.data) {
      orderList.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        orderNo: item.orderNo || `ORD-${item.id}`,
        userName: item.userName || `客户#${item.customerId}`,
        customerId: item.customerId,
        totalAmount: item.totalAmount || 0,
        payAmount: item.payAmount || 0,
        status: item.status || 'pending',
        createdAt: item.createdAt,
        remark: item.remark || item.customerRemark,
      }))
      total.value = Number(res.data.total ?? 0)
    }
  } catch (e) {
    console.error('订单数据加载失败:', e)
    ElMessage.error('订单数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.order-page { max-width: 1400px; }
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
.detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.detail-item { display:flex; flex-direction:column; gap:4px; }
.detail-label { font-size:12px; color:var(--crm-text-secondary); }
.detail-value { font-size:14px; color:var(--crm-text-primary); }
.detail-remark { margin-top:16px; padding-top:16px; border-top:1px solid var(--crm-border-light);
  .detail-label { display:block; margin-bottom:8px; }
  p { margin:0; font-size:14px; color:var(--crm-text-primary); line-height:1.6; }
}
</style>
