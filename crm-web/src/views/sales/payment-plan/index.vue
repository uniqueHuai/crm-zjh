<template>
  <div class="payment-plan-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">回款计划</h2>
        <p class="page-subtitle">管理合同回款计划与回款登记</p>
      </div>
      <div class="page-header-right" />
    </div>

    <div class="card">
      <div class="search-form">
        <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
          <el-form-item label="合同编号">
            <el-input-number
              v-model="queryParams.contractId"
              :min="1"
              :controls="false"
              placeholder="请输入合同ID"
              clearable
              style="width:200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <template v-if="currentContractId">
        <div class="toolbar">
          <div class="toolbar-left">
            <span class="result-count">共 <b>{{ records.length }}</b> 条回款计划</span>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" @click="openCreateDialog">
              <el-icon><Plus /></el-icon>新增计划
            </el-button>
            <el-button @click="openBatchDialog">批量生成</el-button>
            <el-tooltip content="刷新">
              <el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
            </el-tooltip>
          </div>
        </div>

        <el-table :data="records" v-loading="loading" stripe max-height="600" style="width:100%">
          <el-table-column prop="stage" label="期次" width="80" align="center" />
          <el-table-column prop="stageName" label="阶段名称" min-width="120" />
          <el-table-column label="预计金额" width="140">
            <template #default="{row}">
              <span class="amount">{{ formatAmount(row.expectedAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已回金额" width="140">
            <template #default="{row}">
              <span v-if="row.actualAmount != null" class="amount amount--received">{{ formatAmount(row.actualAmount) }}</span>
              <span v-else class="amount-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="expectedDate" label="预计日期" width="110" />
          <el-table-column prop="paidDate" label="回款日期" width="110">
            <template #default="{row}">
              {{ row.paidDate || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{row}">
              <el-tag
                :type="statusMap[row.status]?.type || 'info'"
                size="small"
                effect="plain"
              >
                {{ statusMap[row.status]?.label || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="付款方式" width="110">
            <template #default="{row}">
              {{ row.paymentMethod || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{row}">
              <template v-if="row.status === 'pending'">
                <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
                <el-button link type="primary" size="small" @click="openSettleDialog(row)">回款登记</el-button>
                <el-popconfirm title="确认删除此回款计划?" @confirm="handleDelete(row)">
                  <template #reference><el-button link type="danger" size="small">删除</el-button></template>
                </el-popconfirm>
              </template>
              <template v-else-if="row.status === 'settled'">
                <el-button link type="primary" size="small" @click="openDetailDialog(row)">查看详情</el-button>
              </template>
              <template v-else>
                <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
                <el-button link type="primary" size="small" @click="openSettleDialog(row)">回款登记</el-button>
                <el-popconfirm title="确认删除此回款计划?" @confirm="handleDelete(row)">
                  <template #reference><el-button link type="danger" size="small">删除</el-button></template>
                </el-popconfirm>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <el-empty v-else description="请输入合同ID查询回款计划" />
    </div>

    <!-- 新增 / 编辑 -->
    <FormDialog
      v-model:visible="createDialogVisible"
      :title="createDialogTitle"
      :initial-data="createInitialData"
      @submit="handleCreateSubmit"
    >
      <template #default="{form}">
        <el-form-item label="期次" prop="stage" :rules="[{required:true,message:'请输入期次'}]">
          <el-input-number v-model="form.stage" :min="1" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="阶段名称" prop="stageName" :rules="[{required:true,message:'请输入阶段名称'}]">
          <el-input v-model="form.stageName" placeholder="例如: 首付款、进度款、尾款" />
        </el-form-item>
        <el-form-item label="预计金额" prop="expectedAmount" :rules="[{required:true,message:'请输入预计金额'}]">
          <el-input-number v-model="form.expectedAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="预计日期" prop="expectedDate" :rules="[{required:true,message:'请选择预计日期'}]">
          <el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息（可选）" />
        </el-form-item>
      </template>
    </FormDialog>

    <!-- 批量生成 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量生成回款计划"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="batchFormRef" :model="batchForm" label-width="100px">
        <el-form-item label="模板名称" prop="template" :rules="[{required:true,message:'请输入模板名称'}]">
          <el-input v-model="batchForm.template" placeholder="例如: 等额本息分期" />
        </el-form-item>
        <el-form-item label="总期数" prop="installments" :rules="[{required:true,message:'请输入总期数'}]">
          <el-input-number v-model="batchForm.installments" :min="1" :max="60" style="width:100%" />
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount" :rules="[{required:true,message:'请输入总金额'}]">
          <el-input-number v-model="batchForm.totalAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="首期日期" prop="firstDate" :rules="[{required:true,message:'请选择首期日期'}]">
          <el-date-picker v-model="batchForm.firstDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="间隔天数" prop="intervalDays" :rules="[{required:true,message:'请输入间隔天数'}]">
          <el-input-number v-model="batchForm.intervalDays" :min="1" :max="365" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="handleBatchSubmit">生成</el-button>
      </template>
    </el-dialog>

    <!-- 回款登记 -->
    <FormDialog
      v-model:visible="settleDialogVisible"
      title="回款登记"
      width="550px"
      :initial-data="{ actualAmount: null, paidDate: '', paymentMethod: '', voucherUrls: [], remark: '' }"
      @submit="handleSettleSubmit"
    >
      <template #default="{form}">
        <el-form-item label="实际金额" prop="actualAmount" :rules="[{required:true,message:'请输入实际回款金额'}]">
          <el-input-number v-model="form.actualAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="回款日期" prop="paidDate" :rules="[{required:true,message:'请选择回款日期'}]">
          <el-date-picker v-model="form.paidDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentMethod" :rules="[{required:true,message:'请选择付款方式'}]">
          <el-select v-model="form.paymentMethod" placeholder="选择付款方式" style="width:100%">
            <el-option label="银行转账" value="银行转账" />
            <el-option label="现金" value="现金" />
            <el-option label="支票" value="支票" />
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款凭证" prop="voucherUrls">
          <el-upload
            list-type="picture-card"
            :multiple="true"
            :limit="9"
            :http-request="(opts) => uploadVoucher(opts, form)"
            :on-remove="(uploadFile) => removeVoucher(uploadFile, form)"
            :file-list="voucherFileList"
            :on-exceed="() => ElMessage.warning('最多上传9张凭证')"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息（可选）" />
        </el-form-item>
      </template>
    </FormDialog>

    <!-- 回款详情 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="回款详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <template v-if="detailRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="期次" :span="1">{{ detailRow.stage }}</el-descriptions-item>
          <el-descriptions-item label="阶段名称" :span="1">{{ detailRow.stageName }}</el-descriptions-item>
          <el-descriptions-item label="预计金额" :span="1">{{ formatAmount(detailRow.expectedAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实际回款" :span="1">{{ formatAmount(detailRow.actualAmount) }}</el-descriptions-item>
          <el-descriptions-item label="预计日期" :span="1">{{ detailRow.expectedDate }}</el-descriptions-item>
          <el-descriptions-item label="回款日期" :span="1">{{ detailRow.paidDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag
              :type="statusMap[detailRow.status]?.type || 'info'"
              size="small"
              effect="plain"
            >
              {{ statusMap[detailRow.status]?.label || detailRow.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="付款方式" :span="1">{{ detailRow.paymentMethod || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailRow.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收款凭证" :span="2">
            <template v-if="voucherUrlsList.length">
              <div class="voucher-list">
                <el-image
                  v-for="(url, idx) in voucherUrlsList"
                  :key="idx"
                  :src="url"
                  :preview-src-list="voucherUrlsList"
                  :initial-index="idx"
                  class="voucher-thumb"
                  fit="cover"
                  preview-teleported
                />
              </div>
            </template>
            <span v-else class="amount-muted">无</span>
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { uploadFile } from '@/api/modules/system'
import {
  getPaymentPlans,
  createPaymentPlan,
  updatePaymentPlan,
  deletePaymentPlan,
  settlePaymentPlan,
  batchCreatePaymentPlans,
} from '@/api/modules/sales'

/* ========== 类型 ========== */

interface PaymentPlan {
  id: number
  contractId: number
  stage: number
  stageName: string
  expectedAmount: number
  actualAmount?: number
  expectedDate: string
  paidDate?: string
  status: string
  paymentMethod?: string
  voucherUrls?: string[]
  remark?: string
}

/* ========== 状态 ========== */

const loading = ref(false)
const records = ref<PaymentPlan[]>([])
const currentContractId = ref<number | null>(null)
const editingRow = ref<PaymentPlan | null>(null)

/* ========== 查询 ========== */

const queryParams = reactive({
  contractId: undefined as number | undefined,
})

function handleSearch() {
  if (queryParams.contractId == null) {
    ElMessage.warning('请输入合同ID')
    return
  }
  currentContractId.value = queryParams.contractId
  fetchData()
}

function handleReset() {
  queryParams.contractId = undefined
  currentContractId.value = null
  records.value = []
}

async function fetchData() {
  if (currentContractId.value == null) return
  loading.value = true
  try {
    const res = await getPaymentPlans(currentContractId.value)
    records.value = (res.data.records || []).map((item: any) => ({
      ...item,
      voucherUrls: typeof item.voucherUrls === 'string'
        ? (item.voucherUrls ? JSON.parse(item.voucherUrls) : [])
        : (item.voucherUrls || []),
    }))
  } finally {
    loading.value = false
  }
}

/* ========== 状态映射 ========== */

const statusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待回款', type: 'primary' },
  settled: { label: '已回款', type: 'success' },
  overdue: { label: '逾期', type: 'danger' },
}

/* ========== 新增 / 编辑 ========== */

const createDialogVisible = ref(false)

const createDialogTitle = computed(() => (editingRow.value ? '编辑回款计划' : '新增回款计划'))

const createInitialData = computed(() => {
  if (editingRow.value) {
    return {
      stage: editingRow.value.stage,
      stageName: editingRow.value.stageName,
      expectedAmount: editingRow.value.expectedAmount,
      expectedDate: editingRow.value.expectedDate,
      remark: editingRow.value.remark || '',
    }
  }
  return { stage: 1, stageName: '', expectedAmount: 0, expectedDate: '', remark: '' }
})

function openCreateDialog() {
  editingRow.value = null
  createDialogVisible.value = true
}

function openEditDialog(row: PaymentPlan) {
  editingRow.value = row
  createDialogVisible.value = true
}

async function handleCreateSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updatePaymentPlan(editingRow.value.id, formData)
      ElMessage.success('修改成功')
    } else {
      await createPaymentPlan(currentContractId.value!, formData)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    done()
  }
}

/* ========== 删除 ========== */

async function handleDelete(row: PaymentPlan) {
  try {
    await deletePaymentPlan(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    /* handled by interceptor */
  }
}

/* ========== 批量生成 ========== */

const batchDialogVisible = ref(false)
const batchFormRef = ref()
const batchSubmitting = ref(false)
const batchForm = reactive({
  template: '',
  installments: 1,
  totalAmount: 0,
  firstDate: '',
  intervalDays: 30,
})

function openBatchDialog() {
  if (currentContractId.value == null) {
    ElMessage.warning('请先查询合同回款计划')
    return
  }
  batchForm.template = ''
  batchForm.installments = 1
  batchForm.totalAmount = 0
  batchForm.firstDate = ''
  batchForm.intervalDays = 30
  batchDialogVisible.value = true
}

async function handleBatchSubmit() {
  const valid = await batchFormRef.value?.validate().catch(() => false)
  if (!valid) return
  batchSubmitting.value = true
  try {
    await batchCreatePaymentPlans(currentContractId.value!, { ...batchForm })
    ElMessage.success('批量生成成功')
    batchDialogVisible.value = false
    fetchData()
  } catch {
    /* handled by interceptor */
  } finally {
    batchSubmitting.value = false
  }
}

/* ========== 回款登记 ========== */

const settleDialogVisible = ref(false)
const settleRow = ref<PaymentPlan | null>(null)
const voucherFileList = ref<any[]>([])

watch(settleDialogVisible, (val) => {
  if (val) {
    voucherFileList.value = []
  }
})

function openSettleDialog(row: PaymentPlan) {
  settleRow.value = row
  settleDialogVisible.value = true
}

async function handleSettleSubmit(formData: any, done: () => void) {
  try {
    await settlePaymentPlan(settleRow.value!.id, {
      actualAmount: formData.actualAmount,
      paidDate: formData.paidDate,
      paymentMethod: formData.paymentMethod,
      voucherUrls: formData.voucherUrls?.length ? formData.voucherUrls : undefined,
      remark: formData.remark || undefined,
    })
    ElMessage.success('回款登记成功')
    done()
    fetchData()
  } catch {
    done()
  }
}

async function uploadVoucher(options: any, form: any) {
  const file = options.file as File
  try {
    const res = await uploadFile(file, 'voucher', true)
    const data = res as any
    const url = data.data?.fileUrl || data.fileUrl || data.url || data.path || ''
    if (url) {
      form.voucherUrls.push(url)
      voucherFileList.value.push({ name: file.name, url, uid: options.file.uid })
    } else {
      ElMessage.error('上传失败：未返回文件地址')
    }
  } catch {
    ElMessage.error('凭证上传失败')
  }
}

function removeVoucher(uploadFile: any, form: any) {
  if (uploadFile.url) {
    const idx = form.voucherUrls.indexOf(uploadFile.url)
    if (idx > -1) form.voucherUrls.splice(idx, 1)
  }
}

/* ========== 回款详情 ========== */

const detailDialogVisible = ref(false)
const detailRow = ref<PaymentPlan | null>(null)

const voucherUrlsList = computed(() => {
  const urls = detailRow.value?.voucherUrls
  return Array.isArray(urls) ? urls : []
})

function openDetailDialog(row: PaymentPlan) {
  detailRow.value = row
  detailDialogVisible.value = true
}

/* ========== 工具 ========== */

function formatAmount(val: number | undefined | null): string {
  if (val == null) return '-'
  return `¥${val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}
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

.search-form {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--crm-border-light);

  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 0;
  }
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
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

.amount {
  font-family: 'Helvetica Neue', 'PingFang SC', monospace;
  font-weight: 500;
  color: var(--crm-text-primary);

  &--received {
    color: var(--el-color-success);
  }
}

.amount-muted {
  color: var(--crm-text-tertiary);
}

.voucher-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .voucher-thumb {
    width: 72px;
    height: 72px;
    border-radius: 6px;
    border: 1px solid var(--crm-border-light);
    cursor: pointer;
    overflow: hidden;
    flex-shrink: 0;
  }
}
</style>
