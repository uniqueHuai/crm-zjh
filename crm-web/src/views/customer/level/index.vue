<template>
  <div class="level-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">客户等级管理</h2>
        <p class="page-subtitle">设置客户等级体系，支持升降级规则与自动评估</p>
      </div>
      <div class="page-header-right">
        <el-button @click="openBatchDialog"><el-icon><Discount /></el-icon>批量设置等级</el-button>
        <el-popconfirm title="确认执行等级评估?" @confirm="handleEvaluate">
          <template #reference><el-button><el-icon><DataAnalysis /></el-icon>执行评估</el-button></template>
        </el-popconfirm>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增等级</el-button>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ levelList.length }}</b> 个等级</span>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="levelList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="等级名称" min-width="140">
          <template #default="{row}">
            <div class="cell-name-row">
              <span v-if="row.icon" class="cell-icon">{{ row.icon }}</span>
              <span class="cell-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column label="消费区间" width="200">
          <template #default="{row}">
            <span class="amount-range">¥{{ row.minAmount ?? 0 }} ~ ¥{{ row.maxAmount ?? '不限' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minOrderCount" label="最低订单数" width="110" align="center" />
        <el-table-column prop="benefits" label="权益" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="openRuleDialog(row)">设置规则</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="{ status: 1, sortOrder: 0 }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="等级名称" prop="name" :rules="[{required:true,message:'请输入等级名称'}]">
          <el-input v-model="form.name" placeholder="等级名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="图标文字或 Emoji" />
          <template #help><span style="font-size:12px;color:var(--crm-text-secondary)">输入图标文字或 Emoji 符号</span></template>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="最低消费" prop="minAmount">
            <el-input-number v-model="form.minAmount" :min="0" :precision="2" placeholder="0" style="width:160px" />
          </el-form-item>
          <el-form-item label="最高消费" prop="maxAmount">
            <el-input-number v-model="form.maxAmount" :min="0" :precision="2" placeholder="不限" style="width:160px" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="最低订单数" prop="minOrderCount">
            <el-input-number v-model="form.minOrderCount" :min="0" :step="1" style="width:160px" />
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width:160px" />
          </el-form-item>
        </div>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权益" prop="benefits">
          <el-input v-model="form.benefits" type="textarea" :rows="3" placeholder="等级权益描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </template>
    </FormDialog>

    <el-dialog v-model="batchDialogVisible" title="批量设置等级" :width="520" :close-on-click-modal="false" :destroy-on-close="true">
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="100px">
        <el-form-item label="客户ID" prop="customerIds" :rules="[{required:true,type:'array',min:1,message:'请选择客户'}]">
          <el-input v-model="batchForm.customerIdsStr" type="textarea" :rows="3" placeholder="输入客户ID，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="目标等级" prop="levelId" :rules="[{required:true,message:'请选择目标等级'}]">
          <el-select v-model="batchForm.levelId" placeholder="选择等级" style="width:100%">
            <el-option v-for="item in levelList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason" :rules="[{required:true,message:'请输入设置原因'}]">
          <el-input v-model="batchForm.reason" type="textarea" :rows="2" placeholder="设置等级的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="handleBatchSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ruleDialogVisible" :title="'规则配置 - ' + ruleLevelName" :width="600" :close-on-click-modal="false" :destroy-on-close="true">
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRules" label-width="120px">
        <el-form-item label="自动升级条件" prop="upgradeRules">
          <el-input v-model="ruleForm.upgradeRules" type="textarea" :rows="4" placeholder="配置自动升级规则条件，如: 累计消费满 ¥5000 且订单数 ≥ 10" />
        </el-form-item>
        <el-form-item label="自动降级条件" prop="degradeRules">
          <el-input v-model="ruleForm.degradeRules" type="textarea" :rows="4" placeholder="配置自动降级规则条件，如: 连续 90 天无消费" />
        </el-form-item>
        <el-form-item label="评估周期" prop="evalCycle">
          <el-select v-model="ruleForm.evalCycle" placeholder="选择周期" style="width:200px">
            <el-option label="每天" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
            <el-option label="每季度" value="quarterly" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSubmitting" @click="handleRuleSubmit">保存规则</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Discount, DataAnalysis } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { FormDialog } from '@/components/common'
import {
  getCustomerLevels, createCustomerLevel, updateCustomerLevel,
  deleteCustomerLevel, evaluateLevels, batchSetLevel,
} from '@/api/modules/customer'

interface LevelItem {
  id: number; name: string; icon?: string; minAmount?: number; maxAmount?: number;
  minOrderCount?: number; benefits?: string; sortOrder: number;
  status: number; remark?: string; createTime: string;
}

const loading = ref(false)
const levelList = ref<LevelItem[]>([])
const dialogVisible = ref(false)
const editingRow = ref<LevelItem | null>(null)

const dialogTitle = computed(() => editingRow.value ? '编辑等级' : '新增等级')

function openDialog(row?: LevelItem) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateCustomerLevel(editingRow.value.id, formData)
    } else {
      await createCustomerLevel(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: LevelItem) {
  try {
    await deleteCustomerLevel(row.id)
    ElMessage.success(`已删除等级「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleEvaluate() {
  try {
    await evaluateLevels()
    ElMessage.success('等级评估已触发，请稍后查看结果')
    fetchData()
  } catch { /* handled by interceptor */ }
}

// --- 设置规则 ---
const ruleDialogVisible = ref(false)
const ruleLevelId = ref<number | null>(null)
const ruleLevelName = ref('')
const ruleSubmitting = ref(false)
const ruleFormRef = ref<FormInstance>()

const ruleForm = ref({
  upgradeRules: '',
  degradeRules: '',
  evalCycle: 'monthly',
})

const ruleRules: FormRules = {
  evalCycle: [{ required: true, message: '请选择评估周期' }],
}

function openRuleDialog(row: LevelItem) {
  ruleLevelId.value = row.id
  ruleLevelName.value = row.name
  ruleForm.value = { upgradeRules: '', degradeRules: '', evalCycle: 'monthly' }
  ruleDialogVisible.value = true
}

async function handleRuleSubmit() {
  const valid = await ruleFormRef.value?.validate().catch(() => false)
  if (!valid) return
  ruleSubmitting.value = true
  try {
    await setLevelRules(ruleLevelId.value!, { ...ruleForm.value })
    ElMessage.success(`等级「${ruleLevelName.value}」规则已保存`)
    ruleDialogVisible.value = false
  } finally { ruleSubmitting.value = false }
}

// --- 批量设置等级 ---
const batchDialogVisible = ref(false)
const batchSubmitting = ref(false)
const batchFormRef = ref<FormInstance>()

const batchForm = ref({
  customerIdsStr: '',
  levelId: undefined as number | undefined,
  reason: '',
})

const batchRules: FormRules = {
  levelId: [{ required: true, message: '请选择目标等级' }],
  reason: [{ required: true, message: '请输入设置原因' }],
}

function openBatchDialog() {
  batchForm.value = { customerIdsStr: '', levelId: undefined, reason: '' }
  batchDialogVisible.value = true
}

async function handleBatchSubmit() {
  const valid = await batchFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const ids = batchForm.value.customerIdsStr
    .split(/[,，\s]+/)
    .map(s => parseInt(s.trim(), 10))
    .filter(n => !isNaN(n))
  if (ids.length === 0) {
    ElMessage.warning('请输入有效的客户ID')
    return
  }
  batchSubmitting.value = true
  try {
    await batchSetLevel({
      customerIds: ids,
      levelId: batchForm.value.levelId!,
      reason: batchForm.value.reason,
    })
    ElMessage.success(`已为 ${ids.length} 个客户设置等级`)
    batchDialogVisible.value = false
  } finally { batchSubmitting.value = false }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCustomerLevels()
    levelList.value = (res.data || []) as LevelItem[]
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); padding:20px; }
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b { color:var(--crm-text-primary); } }
  &-right { display:flex; align-items:center; gap:4px; }
}
.cell-name-row { display:flex; align-items:center; gap:6px; }
.cell-icon { font-size:18px; line-height:1; }
.cell-name { font-weight:500; color:var(--crm-text-primary); }
.amount-range { font-size:13px; color:var(--crm-text-primary); }
.form-row { display:flex; gap:16px;
  :deep(.el-form-item) { flex:1; }
}
</style>
