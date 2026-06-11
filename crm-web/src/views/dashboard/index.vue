<template>
  <div class="dashboard" v-loading="loading" element-loading-text="加载中...">
    <!-- Stats Row -->
    <el-row :gutter="20">
      <el-col :xs="12" :sm="12" :md="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ '--icon-bg': stat.iconBg, '--value-color': stat.valueColor }">
          <div class="stat-icon"><el-icon :size="22"><component :is="stat.icon" /></el-icon></div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
            <span v-if="stat.trend" class="stat-trend" :class="stat.trendDir">
              <el-icon :size="10"><CaretTop v-if="stat.trendDir==='up'" /><CaretBottom v-else /></el-icon>
              {{ stat.trend }}
            </span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts & Sidebar -->
    <el-row :gutter="20" class="section chart-row">
      <el-col :span="16" class="chart-col">
        <div class="card chart-card">
          <div class="card-header">
            <div class="card-header-left">
              <h3>销售趋势</h3>
            </div>
            <el-radio-group v-model="chartRange" size="small">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="quarter">本季</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-body" style="flex:1;min-height:0">
            <v-chart :option="chartOption" autoresize style="height:100%;width:100%" />
          </div>
        </div>
      </el-col>
      <el-col :span="8" class="chart-col">
        <div class="card quick-actions-card">
          <div class="card-header"><h3>快捷入口</h3></div>
          <div class="quick-actions">
            <div class="quick-action-item" v-for="a in quickActions" :key="a.label" @click="a.click">
              <div class="qa-icon" :style="{ background: a.bg }">
                <el-icon :size="20" :color="a.color"><component :is="a.icon" /></el-icon>
              </div>
              <span class="qa-label">{{ a.label }}</span>
            </div>
          </div>
        </div>
        <div class="card" style="margin-top:16px;flex:1">
          <div class="card-header">
            <h3>待办事项</h3>
            <el-badge :value="todos.length" :hidden="!todos.length" />
          </div>
          <div class="todo-list">
            <div v-for="t in todos" :key="t.id" class="todo-item">
              <el-checkbox v-model="t.done">
                <span :class="{ 'todo-done': t.done }">{{ t.content }}</span>
              </el-checkbox>
              <el-tag :type="t.priority==='urgent'?'danger':t.priority==='high'?'warning':'info'" size="small" effect="plain">{{ t.priorityLabel }}</el-tag>
            </div>
            <el-empty v-if="!todos.length" description="暂无待办" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Recent Activity -->
    <div class="card section">
      <div class="card-header">
        <div class="card-header-left">
          <h3>近期跟进提醒</h3>
          <span class="card-subtitle">今日 {{ recentActivities.length }} 条跟进记录</span>
        </div>
        <el-button text type="primary" size="small">查看更多</el-button>
      </div>
      <el-table :data="recentActivities" stripe max-height="320">
        <el-table-column prop="customerName" label="客户" width="150">
          <template #default="{row}">
            <div class="tc-name">
              <el-avatar :size="24">{{ row.customerName.charAt(0) }}</el-avatar>
              <span>{{ row.customerName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="跟进内容" min-width="260" />
        <el-table-column prop="type" label="方式" width="90">
          <template #default="{row}">
            <el-tag :type="row.type==='visit'?'primary':row.type==='call'?'success':'info'" size="small" effect="plain">{{ row.typeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="跟进时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status==='completed'?'success':'warning'" size="small">{{ row.status==='completed'?'已完成':'待处理' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pipeline & Ranking -->
    <el-row :gutter="20" class="section" style="display:flex;align-items:stretch">
      <el-col :span="12" style="display:flex;flex-direction:column">
        <div class="card" style="display:flex;flex-direction:column;flex:1">
          <div class="card-header">
            <h3>销售漏斗</h3>
            <el-button text type="primary" size="small" @click="$router.push('/sales/pipeline')">查看详情</el-button>
          </div>
          <div style="flex:1;min-height:0">
            <v-chart :option="funnelOption" autoresize style="height:100%;width:100%;min-height:200px" />
          </div>
        </div>
      </el-col>
      <el-col :span="12" style="display:flex;flex-direction:column">
        <div class="card" style="display:flex;flex-direction:column;flex:1">
          <div class="card-header">
            <h3>团队排行</h3>
            <el-tag size="small" effect="plain">本月</el-tag>
          </div>
          <div class="ranking-list">
            <div v-for="(m,i) in teamRanking" :key="m.name" class="ranking-item">
              <div class="ranking-index" :class="{top:i<3}">{{ i+1 }}</div>
              <div class="ranking-info">
                <span class="ranking-name">{{ m.name }}</span>
                <span class="ranking-dept">{{ m.dept }}</span>
              </div>
              <div class="ranking-stats">
                <span class="ranking-amount">¥{{ m.amount.toLocaleString() }}</span>
                <span class="ranking-count">{{ m.dealCount }}单</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User, TrendCharts, Wallet, ChatDotSquare, Plus, Tickets, Files, DataAnalysis, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, FunnelChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getDashboardSummary, getSalesFunnel, getCustomerAnalysis, getEmployeePerformance } from '@/api/modules/report'
import { getFollowUpPage, getAppointmentPage } from '@/api/modules/sales'
import { getPendingApprovals } from '@/api/modules/collaboration'
import { useUserStore } from '@/store/modules/user'

use([CanvasRenderer, LineChart, BarChart, FunnelChart, GridComponent, TooltipComponent, LegendComponent])

const router = useRouter()
const chartRange = ref('month')
const loading = ref(true)

// 根据范围计算起止日期
function getDateRange(range: string) {
  const now = new Date()
  const fmt = (d: Date) => {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }

  switch (range) {
    case 'week': {
      const dayOfWeek = now.getDay() || 7 // Sunday=0 -> 7
      const start = new Date(now)
      start.setDate(now.getDate() - dayOfWeek + 1)
      return { startDate: fmt(start), endDate: fmt(now) }
    }
    case 'month':
      return { startDate: fmt(new Date(now.getFullYear(), now.getMonth(), 1)), endDate: fmt(now) }
    case 'quarter': {
      const qStartMonth = Math.floor(now.getMonth() / 3) * 3
      return { startDate: fmt(new Date(now.getFullYear(), qStartMonth, 1)), endDate: fmt(now) }
    }
    default:
      return { startDate: '', endDate: '' }
  }
}

// === API data refs ===
const summaryData = ref<Record<string, any>>({})
const funnelStages = ref<any[]>([])
const customerTrend = ref<any[]>([])
const rankingList = ref<any[]>([])
const recentActivities = ref<any[]>([])

// === Stats cards (computed from summary API) ===
const funnelColors = ['#1a73e8', '#4a90d9', '#f59e0b', '#f97316', '#22c55e', '#999']
const stats = computed(() => [
  { value: summaryData.value.monthlyNewCustomers ?? 0, label: '本月新增客户', icon: User, iconBg: '#e8f0fe', valueColor: '#1a73e8', trend: `共 ${summaryData.value.totalCustomers ?? 0} 位客户`, trendDir: 'up' },
  { value: summaryData.value.activeOppCount ?? 0, label: '跟进中商机', icon: TrendCharts, iconBg: '#e6f7ed', valueColor: '#22c55e', trend: `总金额 ¥${((summaryData.value.totalOppAmount ?? 0) / 10000).toFixed(0)}万`, trendDir: 'up' },
  { value: summaryData.value.totalContracts ?? 0, label: '合同总数', icon: ChatDotSquare, iconBg: '#fff7e6', valueColor: '#f59e0b', trend: `签约金额 ¥${((summaryData.value.signedContractAmount ?? 0) / 10000).toFixed(0)}万`, trendDir: 'up' },
  { value: summaryData.value.totalCustomers ?? 0, label: '累计客户数', icon: Wallet, iconBg: '#f0e6ff', valueColor: '#8b5cf6', trend: `本月新增 ${summaryData.value.monthlyNewCustomers ?? 0}`, trendDir: 'up' },
])

// === Todos (pending approvals + today's appointments + follow-ups) ===
const todos = ref<any[]>([])

// === Team ranking (computed from employee performance API) ===
const teamRanking = computed(() =>
  rankingList.value.map((r: any) => ({
    name: r.realName,
    dept: r.deptName,
    amount: r.dealAmount ?? 0,
    dealCount: r.dealCount ?? 0,
  }))
)

// === Quick actions ===
const quickActions = [
  { label: '新增线索', icon: Plus, bg: '#e8f0fe', color: '#1a73e8', click: () => router.push('/customer/lead') },
  { label: '商机看板', icon: DataAnalysis, bg: '#e6f7ed', color: '#22c55e', click: () => router.push('/sales/pipeline') },
  { label: '新建报价', icon: Tickets, bg: '#fff7e6', color: '#f59e0b', click: () => router.push('/sales/quotation') },
  { label: '合同管理', icon: Files, bg: '#f0e6ff', color: '#8b5cf6', click: () => router.push('/sales/contract') },
]

// === Bar chart: new customer trend ===
const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' as const },
  legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8 },
  grid: { left: '3%', right: '4%', bottom: '18%', top: '3%', containLabel: true },
  xAxis: { type: 'category' as const, data: customerTrend.value.length ? customerTrend.value.map((t: any) => formatDateLabel(t.date)) : [], axisLine: { show: false }, axisTick: { show: false } },
  yAxis: { type: 'value' as const, splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { formatter: (v: number) => v } },
  series: [
    { name: '新增客户', type: 'bar' as const, barWidth: '32%', itemStyle: { borderRadius: [4, 4, 0, 0], color: '#1a73e8' }, data: customerTrend.value.map((t: any) => t.count) },
  ],
}))

// 格式化 x 轴日期标签
function formatDateLabel(date: string) {
  if (!date) return ''
  const parts = date.split('-')
  if (parts.length === 3) return parts[1] + '/' + parts[2]    // YYYY-MM-DD → MM/DD
  if (parts.length === 2) return parts[1] + '月'                // YYYY-MM → MM月
  return date
}

// 按所选范围补全天/月数据（没有数据的填 0）
function fillTrendData(data: { date: string; count: number }[], range: string) {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth()
  const d = now.getDate()

  // 查找已存在数据的映射
  const map = new Map<string, number>()
  data.forEach(item => map.set(item.date, item.count))

  function pad(n: number) { return String(n).padStart(2, '0') }

  if (range === 'week') {
    // 本周一到周日 7 天
    const dayOfWeek = now.getDay() || 7
    const monday = new Date(now)
    monday.setDate(d - dayOfWeek + 1)
    const result: { date: string; count: number }[] = []
    for (let i = 0; i < 7; i++) {
      const day = new Date(monday)
      day.setDate(monday.getDate() + i)
      const key = `${day.getFullYear()}-${pad(day.getMonth() + 1)}-${pad(day.getDate())}`
      result.push({ date: key, count: map.get(key) ?? 0 })
    }
    return result
  }

  if (range === 'month') {
    // 整个月的每一天
    const totalDays = new Date(y, m + 1, 0).getDate()
    const result: { date: string; count: number }[] = []
    for (let i = 1; i <= totalDays; i++) {
      const key = `${y}-${pad(m + 1)}-${pad(i)}`
      result.push({ date: key, count: map.get(key) ?? 0 })
    }
    return result
  }

  if (range === 'quarter') {
    // 本季度的 3 个月（按月聚合）
    const qStartMonth = Math.floor(m / 3) * 3
    const result: { date: string; count: number }[] = []
    // 先把数据按月聚合
    const monthMap = new Map<string, number>()
    data.forEach(item => {
      const monthKey = item.date.substring(0, 7) // YYYY-MM-DD → YYYY-MM
      monthMap.set(monthKey, (monthMap.get(monthKey) ?? 0) + item.count)
    })
    for (let i = 0; i < 3; i++) {
      const month = qStartMonth + i
      const key = `${y}-${pad(month + 1)}`
      result.push({ date: key, count: monthMap.get(key) ?? 0 })
    }
    return result
  }

  return data
}

// === Funnel chart: opportunity stages ===
const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' as const, formatter: '{b}: {c}个' },
  series: [{
    type: 'funnel' as const, left: '10%', right: '10%', bottom: 0, height: 220,
    label: { show: true, position: 'inside', fontSize: 12, formatter: '{b}\n{c}个' },
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    data: funnelStages.value.map((s: any, i: number) => ({
      value: s.count,
      name: s.stageName,
      itemStyle: { color: funnelColors[i] || '#999' },
    })),
  }],
}))

// === Fetch all dashboard data ===
const fetchDashboardData = async (range?: string) => {
  loading.value = true
  const dateRange = getDateRange(range || chartRange.value)
  const dateParams = dateRange.startDate ? dateRange : {}
  try {
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    const userId = useUserStore().userInfo?.userId

    const [summaryRes, funnelRes, analysisRes, perfRes, followUpRes, approvalRes, appointmentRes] = await Promise.all([
      getDashboardSummary(),
      getSalesFunnel(dateParams),
      getCustomerAnalysis(dateParams),
      getEmployeePerformance(dateParams),
      getFollowUpPage({ page: 1, size: 5 }),
      userId ? getPendingApprovals({ page: 1, size: 10, approverId: userId }).catch(() => null) : Promise.resolve(null),
      getAppointmentPage({ page: 1, size: 10, startDate: todayStr, endDate: todayStr, status: 'pending' }),
    ])

    if (summaryRes?.data) summaryData.value = summaryRes.data as Record<string, any>
    if (funnelRes?.data?.stages) funnelStages.value = (funnelRes.data as any).stages as any[]
    if (analysisRes?.data?.trend?.newCustomers) {
      const raw = (analysisRes.data as any).trend.newCustomers as any[]
      customerTrend.value = fillTrendData(raw, chartRange.value)
    }
    if (perfRes?.data?.rankings) rankingList.value = (perfRes.data as any).rankings as any[]

    if (followUpRes?.data?.records) {
      recentActivities.value = (followUpRes.data.records as any[]).map((r: any) => ({
        customerName: r.customerName || `客户#${r.customerId}`,
        content: r.content || r.remark || '—',
        type: r.type || 'other',
        typeLabel: ({ visit: '拜访', call: '电话', meeting: '会议', online: '在线' } as Record<string, string>)[r.type] || '其他',
        createdAt: r.createdAt || '',
        status: r.status || 'completed',
      }))
    }

    // 合并待办事项
    const items: any[] = []
    // 待审批
    if (approvalRes?.data?.records) {
      ;(approvalRes.data.records as any[]).forEach((r: any) => {
        items.push({
          id: 'approval-' + r.id,
          done: false,
          content: `审批：${r.bizType === 'quotation' ? '报价' : r.bizType === 'contract' ? '合同' : r.bizType}申请 - ${r.applicantName || '未知'}`,
          priority: 'high',
          priorityLabel: '待审批',
        })
      })
    }
    // 今日待办日程
    if (appointmentRes?.data?.records) {
      ;(appointmentRes.data.records as any[]).forEach((r: any) => {
        const timeLabel = r.startTime ? r.startTime.substring(0, 5) : ''
        items.push({
          id: 'appt-' + r.id,
          done: false,
          content: `拜访：${r.title || '无标题'}${timeLabel ? ' ' + timeLabel : ''}`,
          priority: 'normal',
          priorityLabel: '今日日程',
        })
      })
    }

    todos.value = items.sort((a, b) => {
      const order: Record<string, number> = { urgent: 0, high: 1, normal: 2 }
      return (order[a.priority] ?? 9) - (order[b.priority] ?? 9)
    })
  } catch (e) {
    console.error('Dashboard data fetch failed:', e)
    ElMessage.error('看板数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchDashboardData())
watch(chartRange, (val) => fetchDashboardData(val))
</script>

<style scoped lang="scss">
.dashboard { width:100%; min-height:100%; display:flex; flex-direction:column; }
.section { margin-top:20px; }

/* Chart row: equal-height columns */
.chart-row { display:flex; align-items:stretch; }
.chart-col { display:flex; flex-direction:column; }
.chart-card { display:flex; flex-direction:column; flex:1; }

/* Stat Cards */
.stat-card {
  display:flex; align-items:center; gap:16px;
  padding:20px; background:var(--crm-bg-white); border-radius:var(--crm-radius-lg);
  border:1px solid var(--crm-border); cursor:pointer; transition:all var(--crm-transition-base);
  &:hover { box-shadow:var(--crm-shadow-md); transform:translateY(-2px); }
  .stat-icon {
    width:48px; height:48px; border-radius:12px; display:flex; align-items:center; justify-content:center;
    flex-shrink:0; background:var(--icon-bg,var(--crm-primary-lighter));
    .el-icon { color:var(--value-color,var(--crm-primary)); }
  }
  .stat-info { flex:1; min-width:0; display:flex; flex-direction:column; }
  .stat-value { font-size:24px; font-weight:700; color:var(--value-color,var(--crm-primary)); line-height:1.2; }
  .stat-label { font-size:13px; color:var(--crm-text-secondary); margin-top:2px; }
  .stat-trend { font-size:12px; margin-top:4px; display:inline-flex; align-items:center; gap:2px; &.up { color:var(--crm-success); } &.down { color:var(--crm-danger); } }
}

/* Cards */
.card {
  background:var(--crm-bg-white); border-radius:var(--crm-radius-lg);
  border:1px solid var(--crm-border); padding:20px;
  .card-header {
    display:flex; align-items:center; justify-content:space-between; margin-bottom:16px;
    h3 { margin:0; font-size:15px; font-weight:600; color:var(--crm-text-primary); }
    &-left { display:flex; align-items:baseline; gap:8px; }
  }
  .card-subtitle { font-size:12px; color:var(--crm-text-secondary); }
}

/* Quick Actions */
.quick-actions { display:grid; grid-template-columns:1fr 1fr; gap:12px; }
.quick-action-item {
  display:flex; align-items:center; gap:10px; padding:12px; border-radius:var(--crm-radius-md);
  cursor:pointer; transition:background var(--crm-transition-fast); border:1px solid var(--crm-border);
  &:hover { background:var(--crm-bg-gray); }
  .qa-icon { width:36px; height:36px; border-radius:8px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
  .qa-label { font-size:13px; font-weight:500; color:var(--crm-text-primary); }
}

/* Todo */
.todo-list { display:flex; flex-direction:column; gap:4px; }
.todo-item {
  display:flex; align-items:center; justify-content:space-between; padding:8px 4px;
  :deep(.el-checkbox__label) { font-size:13px; color:var(--crm-text-primary); }
  .todo-done { text-decoration:line-through; color:var(--crm-text-secondary); }
}

/* Table cell */
.tc-name { display:flex; align-items:center; gap:8px; font-weight:500; }

/* Ranking */
.ranking-list { display:flex; flex-direction:column; gap:2px; }
.ranking-item {
  display:flex; align-items:center; gap:12px; padding:10px 4px; border-radius:var(--crm-radius-sm);
  &:hover { background:var(--crm-bg-gray); }
  .ranking-index {
    width:24px; height:24px; border-radius:50%; display:flex; align-items:center; justify-content:center;
    font-size:12px; font-weight:600; color:var(--crm-text-secondary); background:var(--crm-bg-gray);
    &.top { background:var(--crm-primary); color:white; }
  }
  .ranking-info { flex:1; display:flex; flex-direction:column; }
  .ranking-name { font-size:13px; font-weight:500; color:var(--crm-text-primary); }
  .ranking-dept { font-size:12px; color:var(--crm-text-secondary); }
  .ranking-stats { text-align:right; display:flex; flex-direction:column; }
  .ranking-amount { font-size:13px; font-weight:600; color:var(--crm-primary); }
  .ranking-count { font-size:12px; color:var(--crm-text-secondary); }
}
</style>
