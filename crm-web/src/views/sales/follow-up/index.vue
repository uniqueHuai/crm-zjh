<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">跟进记录</h2>
        <p class="page-subtitle">查看所有客户的跟进历史与沟通记录</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增跟进</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词"><el-input v-model="queryParams.keywords" placeholder="客户名称/跟进内容" clearable style="width:180px" /></el-form-item>
            <el-form-item label="类型"><el-select v-model="queryParams.type" placeholder="全部" clearable style="width:110px">
              <el-option label="电话" value="call" /><el-option label="拜访" value="visit" /><el-option label="邮件" value="mail" /><el-option label="会议" value="meeting" />
            </el-select></el-form-item>
            <el-form-item label="跟进日期">
              <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:220px" value-format="YYYY-MM-DD" />
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
          <span class="result-count">共 <b>{{ total }}</b> 条跟进记录</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="followUpList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="customerName" label="客户名称" width="130" />
        <el-table-column prop="content" label="跟进内容" min-width="300" show-overflow-tooltip>
          <template #default="{row}">
            <span class="content-cell">{{ row.content.length > 60 ? row.content.slice(0, 60) + '...' : row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{row}">
            <el-tag :type="typeTagType(row.type)" size="small" effect="dark">{{ typeMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="跟进人" width="90" />
        <el-table-column prop="createdAt" label="跟进时间" width="160" />
        <el-table-column prop="nextContactAt" label="下次联系" width="110">
          <template #default="{row}">{{ row.nextContactAt || '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
// Note: follow-up is view-only, so FormDialog is not needed

interface FollowUp {
  id: number
  customerName: string
  content: string
  type: string
  creatorName: string
  createdAt: string
  nextContactAt?: string
}

const typeMap: Record<string, string> = {
  call: '电话', visit: '拜访', mail: '邮件', meeting: '会议',
}

function typeTagType(type: string) {
  const map: Record<string, string> = { call: 'primary', visit: 'success', mail: 'info', meeting: 'warning' }
  return map[type] || 'info'
}

const loading = ref(false)
const followUpList = ref<FollowUp[]>([])
const total = ref(0)
const showSearch = ref(true)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  type: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
})

const mockData: FollowUp[] = [
  { id: 1, customerName: '华为技术', content: '与王总沟通CRM系统升级需求，客户对数据安全模块非常关注，要求提供等保三级认证方案。已约定下周三进行产品演示。', type: 'visit', creatorName: '张销售', createdAt: '2026-05-28 14:20', nextContactAt: '2026-06-03' },
  { id: 2, customerName: '阿里巴巴', content: '电话沟通年度运维合同续签事宜，客户对当前服务表示满意，希望价格上给予一定折扣。已申请销售总监特批。', type: 'call', creatorName: '张销售', createdAt: '2026-05-27 09:10', nextContactAt: '2026-06-01' },
  { id: 3, customerName: '腾讯科技', content: '发送数据中台解决方案白皮书及成功案例集，客户表示会组织内部评审。预计两周内反馈初步意见。', type: 'mail', creatorName: '李销售', createdAt: '2026-05-26 16:30', nextContactAt: '2026-06-09' },
  { id: 4, customerName: '字节跳动', content: '参加客户组织的CRM选型交流会，会上介绍了我们的产品优势和实施案例。客户对移动端功能很感兴趣。', type: 'meeting', creatorName: '李销售', createdAt: '2026-05-25 11:00', nextContactAt: '2026-06-02' },
  { id: 5, customerName: '小米科技', content: '拜访客户IT总监，演示了智能硬件管理平台的最新功能。客户提出了一些定制化需求，已记录并反馈给产品团队。', type: 'visit', creatorName: '王销售', createdAt: '2026-05-24 15:45', nextContactAt: '2026-06-05' },
  { id: 6, customerName: '比亚迪', content: '回访客户了解系统使用情况，客户反馈整体满意度较高，希望增加供应链管理模块。已记录需求。', type: 'call', creatorName: '张销售', createdAt: '2026-05-23 10:30', nextContactAt: '2026-06-10' },
  { id: 7, customerName: '京东集团', content: '发送产品报价方案及技术参数说明，客户表示需要与财务部门确认预算，预计下周回复。', type: 'mail', creatorName: '王销售', createdAt: '2026-05-22 14:00', nextContactAt: '2026-05-29' },
  { id: 8, customerName: '华为技术', content: '电话确认下周产品演示的时间安排，客户协调了CTO和业务总监共同参加。演示环境已准备就绪。', type: 'call', creatorName: '张销售', createdAt: '2026-05-21 09:30', nextContactAt: '2026-06-03' },
]

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', type: undefined, dateRange: undefined, page: 1 }); fetchData() }
function handleAdd() { ElMessage.info('功能开发中') }

async function fetchData() {
  loading.value = true
  try {
    await new Promise(r => setTimeout(r, 300))
    let list = [...mockData]
    if (queryParams.keywords) {
      const kw = queryParams.keywords!
      list = list.filter(i => i.customerName.includes(kw) || i.content.includes(kw))
    }
    if (queryParams.type) list = list.filter(i => i.type === queryParams.type)
    if (queryParams.dateRange && queryParams.dateRange.length === 2) {
      const [start, end] = queryParams.dateRange
      list = list.filter(i => i.createdAt.slice(0, 10) >= start && i.createdAt.slice(0, 10) <= end)
    }
    followUpList.value = list
    total.value = list.length
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.sales-page { max-width: 1400px; }
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
.content-cell { font-size: 13px; color: var(--crm-text-primary); line-height: 1.6; }
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
