<template>
  <div class="distribution-rule-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">线索分配规则</h2>
        <p class="page-subtitle">配置线索自动分配规则，提高跟进效率</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增规则</el-button>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ ruleList.length }}</b> 条规则</span>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="ruleList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="规则名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="80" align="center" />
        <el-table-column label="分配策略" width="120" align="center">
          <template #default="{row}">
            <span>{{ strategyLabel(row.strategy) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="目标类型" width="100" align="center">
          <template #default="{row}">
            <span>{{ targetTypeLabel(row.targetType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标对象" width="120" show-overflow-tooltip />
        <el-table-column prop="maxDailyPerPerson" label="每日上限" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{row}">
            <el-tag v-if="row.status === 1" size="small" type="success" effect="plain">启用</el-tag>
            <el-tag v-else size="small" type="info" effect="plain">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleExecute(row)">执行</el-button>
            <el-button link type="primary" size="small" @click="openLogDrawer(row)">日志</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="formInitial" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="规则名称" prop="name" :rules="[{required:true,message:'请输入规则名称'}]">
          <el-input v-model="form.name" placeholder="规则名称" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="1" :max="999" style="width:120px" />
        </el-form-item>
        <el-form-item label="分配策略" prop="strategy">
          <el-select v-model="form.strategy" placeholder="请选择" style="width:200px">
            <el-option label="轮询" value="round_robin" />
            <el-option label="按负载" value="load_balance" />
            <el-option label="手动" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型" prop="targetType">
          <el-select v-model="form.targetType" placeholder="请选择" style="width:200px">
            <el-option label="员工" value="user" />
            <el-option label="部门" value="dept" />
            <el-option label="团队" value="team" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标对象" prop="targetId">
          <el-input v-model="form.targetId" placeholder="目标对象ID" />
        </el-form-item>
        <el-form-item label="每日上限" prop="maxDailyPerPerson">
          <el-input-number v-model="form.maxDailyPerPerson" :min="0" :max="99999" style="width:160px" />
        </el-form-item>
        <el-form-item label="条件配置" prop="conditions">
          <el-input v-model="form.conditions" type="textarea" :rows="3" placeholder="JSON格式的条件配置" />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">JSON格式，如：{"source":["online","manual"],"maxAmount":100000}</span></template>
        </el-form-item>
        <el-form-item label="策略配置" prop="strategyConfig">
          <el-input v-model="form.strategyConfig" type="textarea" :rows="3" placeholder="JSON格式的策略配置" />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">JSON格式的策略参数配置</span></template>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
    </FormDialog>

    <el-drawer v-model:visible="logDrawerVisible" :title="logDrawerTitle" size="600px" destroy-on-close>
      <div v-loading="logLoading">
        <div v-if="logList.length === 0" style="text-align:center;padding:40px 0;color:var(--crm-text-secondary)">暂无分配日志</div>
        <div v-for="log in logList" :key="log.id" class="log-item">
          <div class="log-header">
            <span class="log-time">{{ log.createTime || log.createdAt }}</span>
            <el-tag v-if="log.status === 'success'" size="small" type="success">成功</el-tag>
            <el-tag v-else-if="log.status === 'failed'" size="small" type="danger">失败</el-tag>
            <el-tag v-else size="small" type="warning">{{ log.status }}</el-tag>
          </div>
          <div class="log-body">
            <span class="log-desc">{{ log.description || log.message || `分配线索 ${log.leadCount || 0} 条` }}</span>
          </div>
        </div>
        <div v-if="logList.length > 0" class="pagination-wrap" style="padding-top:16px">
          <el-pagination v-model:current-page="logPage" v-model:page-size="logPageSize" :page-sizes="[10,20,50]" :total="logTotal" layout="total, sizes, prev, pager, next" background @change="fetchLogs" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import {
  getDistributionRules,
  createDistributionRule,
  updateDistributionRule,
  deleteDistributionRule,
  executeDistributionRule,
  getDistributionLogs,
} from '@/api/modules/customer'

interface DistributionRuleItem {
  id: number
  name: string
  priority?: number
  strategy: string
  strategyConfig?: string
  targetType: string
  targetId?: string
  timeRanges?: string
  maxDailyPerPerson?: number
  conditions?: string
  status: number
  createTime: string
}

const loading = ref(false)
const ruleList = ref<DistributionRuleItem[]>([])
const dialogVisible = ref(false)
const editingRow = ref<DistributionRuleItem | null>(null)

const formInitial: Record<string, any> = {
  name: '',
  priority: 1,
  strategy: 'round_robin',
  strategyConfig: '',
  targetType: 'user',
  targetId: '',
  timeRanges: '',
  maxDailyPerPerson: 0,
  conditions: '',
  status: 1,
}

const dialogTitle = computed(() => (editingRow.value ? '编辑分配规则' : '新增分配规则'))

const strategyMap: Record<string, string> = {
  round_robin: '轮询',
  load_balance: '按负载',
  manual: '手动',
}

const targetTypeMap: Record<string, string> = {
  user: '员工',
  dept: '部门',
  team: '团队',
}

function strategyLabel(val: string) {
  return strategyMap[val] || val
}

function targetTypeLabel(val: string) {
  return targetTypeMap[val] || val
}

function openDialog(row?: DistributionRuleItem) {
  editingRow.value = row || null
  dialogVisible.value = true
}

function prepareFormData(formData: any) {
  const data = { ...formData }
  if (data.conditions && typeof data.conditions === 'string') {
    try { data.conditions = JSON.parse(data.conditions) } catch { /* keep as string */ }
  }
  if (data.strategyConfig && typeof data.strategyConfig === 'string') {
    try { data.strategyConfig = JSON.parse(data.strategyConfig) } catch { /* keep as string */ }
  }
  if (data.timeRanges && typeof data.timeRanges === 'string') {
    try { data.timeRanges = JSON.parse(data.timeRanges) } catch { /* keep as string */ }
  }
  return data
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    const payload = prepareFormData(formData)
    if (editingRow.value) {
      await updateDistributionRule(editingRow.value.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createDistributionRule(payload)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    done()
  }
}

async function handleDelete(row: DistributionRuleItem) {
  try {
    await deleteDistributionRule(row.id)
    ElMessage.success(`已删除规则「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleExecute(row: DistributionRuleItem) {
  try {
    await ElMessageBox.confirm(`确认立即执行规则「${row.name}」?`, '执行确认', {
      confirmButtonText: '执行',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await executeDistributionRule(row.id)
    ElMessage.success(`规则「${row.name}」执行成功`)
  } catch { /* cancelled or handled by interceptor */ }
}

/* ====== 分配日志 ====== */
const logDrawerVisible = ref(false)
const logList = ref<any[]>([])
const logLoading = ref(false)
const logTotal = ref(0)
const logPage = ref(1)
const logPageSize = ref(10)
const currentLogRuleId = ref<number | null>(null)
const logDrawerTitle = ref('分配日志')

function openLogDrawer(row: DistributionRuleItem) {
  currentLogRuleId.value = row.id
  logDrawerTitle.value = `分配日志 - ${row.name}`
  logPage.value = 1
  logList.value = []
  logDrawerVisible.value = true
  fetchLogs()
}

async function fetchLogs() {
  if (currentLogRuleId.value === null) return
  logLoading.value = true
  try {
    const res = await getDistributionLogs(currentLogRuleId.value, {
      page: logPage.value,
      size: logPageSize.value,
    })
    logList.value = res.data.records || []
    logTotal.value = Number(res.data.total ?? 0)
  } catch { /* handled by interceptor */ }
  finally { logLoading.value = false }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDistributionRules()
    ruleList.value = (res.data || []) as DistributionRuleItem[]
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--crm-text-primary);
  }

  .page-subtitle {
    margin: 4px 0 0;
    font-size: 13px;
    color: var(--crm-text-secondary);
  }

  &-right {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.card {
  background: var(--crm-bg-white);
  border-radius: var(--crm-radius-lg);
  border: 1px solid var(--crm-border);
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .result-count {
    font-size: 13px;
    color: var(--crm-text-secondary);

    b {
      color: var(--crm-text-primary);
    }
  }

  &-right {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.log-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--crm-border-light);

  &:last-child {
    border-bottom: none;
  }

  .log-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }

  .log-time {
    font-size: 12px;
    color: var(--crm-text-secondary);
  }

  .log-body {
    font-size: 13px;
    color: var(--crm-text-primary);
  }
}
</style>
