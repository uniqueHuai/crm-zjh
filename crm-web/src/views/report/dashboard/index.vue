<template>
  <div class="report-dashboard">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">报表中心</h2>
        <p class="page-subtitle">数据可视化看板，实时掌握业务核心指标</p>
      </div>
      <div class="page-header-right">
        <el-tooltip content="刷新">
          <el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- Summary Cards -->
    <el-row :gutter="20" class="section">
      <el-col :xs="12" :sm="12" :md="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: stat.iconBg }">
            <el-icon :size="22" :color="stat.color"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
            <span class="stat-trend" :class="stat.trendDir">{{ stat.trend }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="section">
      <el-col :span="14">
        <div class="card">
          <div class="card-header">
            <h3>客户增长趋势</h3>
            <span class="card-subtitle">近12个月客户增长情况</span>
          </div>
          <div class="chart-body">
            <v-chart :option="barOption" autoresize style="height:320px" />
          </div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="card">
          <div class="card-header">
            <h3>销售漏斗</h3>
            <span class="card-subtitle">各阶段商机数量分布</span>
          </div>
          <div class="chart-body">
            <v-chart :option="funnelOption" autoresize style="height:320px" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Ranking -->
    <div class="card section">
      <div class="card-header">
        <h3>销售排行榜</h3>
        <el-tag size="small" effect="plain">本月</el-tag>
      </div>
      <el-table :data="rankingList" stripe max-height="400">
        <el-table-column label="排名" width="80" align="center">
          <template #default="{row,$index}">
            <span :class="['rank-num', { 'rank-top': $index < 3 }]">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="150">
          <template #default="{row}"><span class="cell-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column label="成交金额" min-width="180">
          <template #default="{row}">¥{{ row.amount.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="成交单数" width="120" align="center">
          <template #default="{row}">{{ row.count }} 单</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, User, Wallet, Files, TrendCharts } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, FunnelChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getDashboardSummary, getSalesFunnel, getCustomerAnalysis, getEmployeePerformance } from '@/api/modules/report'

use([CanvasRenderer, BarChart, FunnelChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)

const stats = ref([
  { value: '0', label: '总客户数', icon: User, iconBg: '#e8f0fe', color: '#1a73e8', trend: '', trendDir: 'up' },
  { value: '¥0', label: '总商机金额', icon: Wallet, iconBg: '#e6f7ed', color: '#22c55e', trend: '', trendDir: 'up' },
  { value: '0', label: '合同总数', icon: Files, iconBg: '#fff7e6', color: '#f59e0b', trend: '', trendDir: 'up' },
  { value: '0', label: '本月新增', icon: TrendCharts, iconBg: '#f0e6ff', color: '#8b5cf6', trend: '', trendDir: 'up' },
])

const months = ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月']

const barData = ref<number[]>([])
const barOption = computed(() => ({
  tooltip: { trigger: 'axis' as const },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
  xAxis: { type: 'category' as const, data: months.slice(-barData.value.length || 12), axisLine: { show: false }, axisTick: { show: false } },
  yAxis: { type: 'value' as const, splitLine: { lineStyle: { color: '#f0f0f0' } } },
  series: [{
    name: '新增客户', type: 'bar' as const, barWidth: '45%',
    itemStyle: { borderRadius: [6, 6, 0, 0], color: { type: 'linear' as const, x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#1a73e8' }, { offset: 1, color: '#4a90d9' }] } },
    data: barData.value,
  }],
}))

const funnelData = ref<{ name: string; value: number }[]>([])
const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' as const, formatter: '{b}: {c}个' },
  series: [{
    type: 'funnel' as const, left: '10%', right: '10%', bottom: 0, height: 260,
    label: { show: true, position: 'inside', fontSize: 12, formatter: '{b}\n{c}个' },
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    data: funnelData.value.length > 0 ? funnelData.value : [{ value: 0, name: '暂无数据' }],
  }],
}))

interface RankingItem {
  name: string; amount: number; count: number;
}

const rankingList = ref<RankingItem[]>([])

async function fetchData() {
  loading.value = true
  try {
    const [summaryRes, funnelRes, customerRes, empRes] = await Promise.all([
      getDashboardSummary(),
      getSalesFunnel({}),
      getCustomerAnalysis({}),
      getEmployeePerformance({}),
    ])

    const summary = summaryRes.data || {} as any
    stats.value = [
      { value: (summary.totalCustomers ?? 0).toLocaleString(), label: '总客户数', icon: User, iconBg: '#e8f0fe', color: '#1a73e8', trend: '', trendDir: 'up' as const },
      { value: '¥' + ((summary.totalOppAmount ?? 0) as number).toLocaleString(), label: '总商机金额', icon: Wallet, iconBg: '#e6f7ed', color: '#22c55e', trend: '', trendDir: 'up' as const },
      { value: (summary.totalContracts ?? 0).toLocaleString(), label: '合同总数', icon: Files, iconBg: '#fff7e6', color: '#f59e0b', trend: '', trendDir: 'up' as const },
      { value: (summary.monthlyNewCustomers ?? 0).toLocaleString(), label: '本月新增', icon: TrendCharts, iconBg: '#f0e6ff', color: '#8b5cf6', trend: '', trendDir: 'up' as const },
    ]

    // Bar chart - customer trend
    const trend = (customerRes.data as any)?.trend?.newCustomers || []
    const monthData: Record<string, number> = {}
    trend.forEach((t: any) => { monthData[t.date] = t.count })
    const now = new Date()
    barData.value = []
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
      const key = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0')
      barData.value.push(monthData[key] || 0)
    }

    // Funnel chart
    const stages = (funnelRes.data as any)?.stages || []
    const colors = ['#1a73e8', '#4a90d9', '#f59e0b', '#f97316', '#22c55e']
    funnelData.value = stages.map((s: any, i: number) => ({
      name: s.stageName,
      value: s.count,
      itemStyle: { color: colors[i % colors.length] },
    }))

    // Ranking table
    const rankings = (empRes.data as any)?.rankings || []
    rankingList.value = rankings.map((r: any) => ({
      name: r.realName,
      amount: r.dealAmount || 0,
      count: r.dealCount || 0,
    }))
  } catch {
    ElMessage.error('加载看板数据失败')
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.report-dashboard { max-width: 1400px; }
.section { margin-bottom: 20px; }

.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}

/* Stat Cards */
.stat-card {
  display:flex; align-items:center; gap:16px; padding:20px;
  background:var(--crm-bg-white); border-radius:var(--crm-radius-lg);
  border:1px solid var(--crm-border);
  transition:all var(--crm-transition-base);
  &:hover { box-shadow:var(--crm-shadow-md); transform:translateY(-2px); }
  .stat-icon { width:48px; height:48px; border-radius:12px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
  .stat-info { flex:1; min-width:0; display:flex; flex-direction:column; }
  .stat-value { font-size:24px; font-weight:700; line-height:1.2; }
  .stat-label { font-size:13px; color:var(--crm-text-secondary); margin-top:2px; }
  .stat-trend { font-size:12px; margin-top:4px; &.up { color:var(--crm-success); } &.down { color:var(--crm-danger); } }
}

/* Card */
.card {
  background:var(--crm-bg-white); border-radius:var(--crm-radius-lg);
  border:1px solid var(--crm-border); padding:20px;
  .card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px;
    h3 { margin:0; font-size:15px; font-weight:600; color:var(--crm-text-primary); }
  }
  .card-subtitle { font-size:12px; color:var(--crm-text-secondary); }
}
.chart-body { width:100%; }

/* Ranking */
.rank-num {
  display:inline-flex; align-items:center; justify-content:center;
  width:28px; height:28px; border-radius:50%;
  font-size:13px; font-weight:600; color:var(--crm-text-secondary); background:var(--crm-bg-gray);
  &.rank-top { background:var(--crm-primary); color:#fff; }
}
.cell-name { font-weight:500; color:var(--crm-text-primary); }
</style>
