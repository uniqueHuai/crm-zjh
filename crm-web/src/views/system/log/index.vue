<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">操作日志</h2>
        <p class="page-subtitle">查看系统操作记录，追踪用户行为与审计</p>
      </div>
      <div class="page-header-right">
        <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        <el-popconfirm title="确认清空所有操作日志？此操作不可恢复!" @confirm="handleClear">
          <template #reference><el-button type="danger"><el-icon><Delete /></el-icon>清空</el-button></template>
        </el-popconfirm>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键字"><el-input v-model="queryParams.keywords" placeholder="搜索操作/操作人" clearable style="width:180px" /></el-form-item>
            <el-form-item label="业务类型">
              <el-select v-model="queryParams.businessType" placeholder="全部" clearable style="width:130px">
                <el-option label="新增" value="insert" />
                <el-option label="修改" value="update" />
                <el-option label="删除" value="delete" />
                <el-option label="查询" value="select" />
                <el-option label="导出" value="export" />
                <el-option label="登录" value="login" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
                <el-option label="成功" :value="1" />
                <el-option label="失败" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:240px" />
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
          <span class="result-count">共 <b>{{ total }}</b> 条日志</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="logList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="operatorName" label="操作人" width="110" />
        <el-table-column label="业务类型" width="100">
          <template #default="{row}">
            <el-tag :type="businessTypeMap[row.businessType]?.type || 'info'" size="small">
              {{ businessTypeMap[row.businessType]?.label || row.businessType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求方法" min-width="260">
          <template #default="{row}">
            <span class="text-ellipsis" :title="row.method">{{ row.method }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求地址" min-width="200">
          <template #default="{row}">
            <span class="text-ellipsis" :title="row.requestUrl">{{ row.requestUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'成功':'失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{row}">
            <span>{{ row.duration }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" />
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px" :close-on-click-modal="false">
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="操作" span="1">{{ detailRow.operation }}</el-descriptions-item>
          <el-descriptions-item label="操作人" span="1">{{ detailRow.operatorName }}</el-descriptions-item>
          <el-descriptions-item label="业务类型" span="1">
            <el-tag :type="businessTypeMap[detailRow.businessType]?.type || 'info'" size="small">{{ businessTypeMap[detailRow.businessType]?.label || detailRow.businessType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态" span="1">
            <el-tag :type="detailRow.status===1?'success':'danger'" size="small">{{ detailRow.status===1?'成功':'失败' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址" span="1">{{ detailRow.ip }}</el-descriptions-item>
          <el-descriptions-item label="耗时" span="1">{{ detailRow.duration }}ms</el-descriptions-item>
          <el-descriptions-item label="操作时间" span="2">{{ detailRow.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="请求URL" span="2">
            <span style="word-break:break-all">{{ detailRow.requestUrl }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="请求方法" span="2">
            <span style="word-break:break-all">{{ detailRow.method }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h4 style="margin:0 0 8px;font-size:14px;">请求参数</h4>
        <pre class="code-block">{{ detailRow.requestParams || '-' }}</pre>
        <h4 style="margin:16px 0 8px;font-size:14px;">返回结果</h4>
        <pre class="code-block">{{ detailRow.responseResult || '-' }}</pre>
        <h4 v-if="detailRow.errorMsg" style="margin:16px 0 8px;font-size:14px;color:var(--el-color-danger)">错误信息</h4>
        <pre v-if="detailRow.errorMsg" class="code-block error">{{ detailRow.errorMsg }}</pre>
      </template>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Delete } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'

interface OperationLog {
  id: number; operation: string; operatorName: string; businessType: string;
  method: string; requestUrl: string; ip: string; status: number;
  duration: number; createdAt: string; requestParams?: string;
  responseResult?: string; errorMsg?: string;
}

const businessTypeMap: Record<string, {label:string,type:string}> = {
  insert: {label:'新增',type:'primary'},
  update: {label:'修改',type:'warning'},
  delete: {label:'删除',type:'danger'},
  select: {label:'查询',type:'info'},
  export: {label:'导出',type:'success'},
  login: {label:'登录',type:'primary'},
}

const loading = ref(false)
const logList = ref<OperationLog[]>([])
const total = ref(0)
const showSearch = ref(true)
const detailVisible = ref(false)
const detailRow = ref<OperationLog | null>(null)

const queryParams = reactive({
  page:1, size:20, keywords:'', businessType:undefined as string|undefined,
  status:undefined as number|undefined, dateRange:undefined as [string,string]|undefined
})

const mockData: OperationLog[] = Array.from({length:10}, (_,i) => {
  const ops = [
    { op:'用户登录', name:'admin', type:'login', method:'com.crm.system.controller.AuthController.login(String,String)', url:'/api/auth/login', params:'{"username":"admin","password":"******"}', result:'{"token":"eyJhbGciOiJ...","user":{"id":1}}' },
    { op:'新增客户', name:'张三', type:'insert', method:'com.crm.customer.controller.CustomerController.create(CustomerDTO)', url:'/api/customer/create', params:'{"name":"北京科技有限公司","phone":"138****9999"}', result:'{"id":1001,"msg":"操作成功"}' },
    { op:'编辑用户', name:'李四', type:'update', method:'com.crm.system.controller.SysUserController.update(Long,UserDTO)', url:'/api/system/user/update', params:'{"id":5,"realName":"王五","email":"wangwu@crm.com"}', result:'{"msg":"操作成功"}' },
    { op:'删除线索', name:'赵六', type:'delete', method:'com.crm.customer.controller.LeadController.delete(Long)', url:'/api/customer/lead/delete', params:'{"id":203}', result:'{"msg":"操作成功"}' },
    { op:'导出客户', name:'张三', type:'export', method:'com.crm.customer.controller.CustomerController.export(ExportDTO)', url:'/api/customer/export', params:'{"status":1,"startDate":"2026-01-01"}', result:'{"file":"/export/customer_20260529.xlsx"}' },
    { op:'查询商机', name:'李四', type:'select', method:'com.crm.sales.controller.PipelineController.list(QueryDTO)', url:'/api/sales/pipeline/list', params:'{"page":1,"size":20}', result:'{"total":45,"list":[]}' },
    { op:'分配角色', name:'admin', type:'update', method:'com.crm.system.controller.SysUserController.assignRole(Long,Long[])', url:'/api/system/user/assignRole', params:'{"userId":3,"roleIds":[2,3]}', result:'{"msg":"操作成功"}' },
    { op:'用户登录', name:'王五', type:'login', method:'com.crm.system.controller.AuthController.login(String,String)', url:'/api/auth/login', params:'{"username":"wangwu","password":"******"}', result:'{"msg":"密码错误"}' },
    { op:'新增合同', name:'赵六', type:'insert', method:'com.crm.sales.controller.ContractController.create(ContractDTO)', url:'/api/sales/contract/create', params:'{"customerId":1001,"amount":500000}', result:'{"id":3001,"msg":"操作成功"}' },
    { op:'系统配置', name:'admin', type:'update', method:'com.crm.system.controller.ConfigController.update(ConfigDTO)', url:'/api/system/config/update', params:'{"configKey":"site.name","configValue":"新CRM系统"}', result:'{"msg":"操作成功"}' },
  ]
  const op = ops[i % ops.length]
  const fail = op.op === '用户登录' && i === 7
  return {
    id: i+1, ...op, ip: ['192.168.1.100','192.168.1.101','10.0.0.1','172.16.0.1'][i%4],
    status: fail ? 0 : 1, duration: Math.floor(Math.random() * 500) + 10,
    createdAt: `2026-05-${String(20+i).padStart(2,'0')} ${String(8+Math.floor(i/2)).padStart(2,'0')}:${String(i*3%60).padStart(2,'0')}:${String(i*7%60).padStart(2,'0')}`,
    errorMsg: fail ? 'org.springframework.security.authentication.BadCredentialsException: 密码错误' : undefined,
  }
})

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords:'', businessType:undefined, status:undefined, dateRange:undefined, page:1 })
  fetchData()
}

function openDetail(row: OperationLog) {
  detailRow.value = row
  detailVisible.value = true
}

function handleExport() { ElMessage.info('正在导出操作日志...') }
function handleClear() { ElMessage.success('操作日志已清空'); fetchData() }

async function fetchData() {
  loading.value = true
  try {
    await new Promise(r => setTimeout(r, 300))
    let list = [...mockData]
    if (queryParams.keywords) {
      const kw = queryParams.keywords
      list = list.filter(i => i.operation.includes(kw!) || i.operatorName.includes(kw!))
    }
    if (queryParams.businessType) list = list.filter(i => i.businessType === queryParams.businessType)
    if (queryParams.status !== undefined) list = list.filter(i => i.status === queryParams.status)
    if (queryParams.dateRange) {
      const [start, end] = queryParams.dateRange
      list = list.filter(i => i.createdAt >= start && i.createdAt <= end + ' 23:59:59')
    }
    logList.value = list; total.value = list.length
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
.code-block { background:#f5f7fa; border-radius:4px; padding:12px; font-size:12px; line-height:1.6; overflow:auto; max-height:200px; margin:0; white-space:pre-wrap; word-break:break-all; &.error { background:#fef0f0; color:#e74c3c; } }
:deep(.el-descriptions__cell) { padding:8px 12px; }
</style>
