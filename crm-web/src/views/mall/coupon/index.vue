<template>
  <div class="coupon-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">优惠券管理</h2>
        <p class="page-subtitle">创建和管理优惠券，支持营销活动发放</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增优惠券</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="名称" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.couponStatus" placeholder="全部" clearable style="width:120px">
                <el-option label="未发布" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="已结束" :value="2" />
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
          <span class="result-count">共 <b>{{ total }}</b> 张优惠券</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="couponList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="优惠券名称" min-width="160">
          <template #default="{row}"><span class="cell-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{row}">
            <el-tag :type="typeMap[row.type]?.type || 'info'" size="small" effect="light">
              {{ typeMap[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面值" width="110">
          <template #default="{row}">
            <span v-if="row.type === 'full_reduce' || row.type === 'new_user'">¥{{ row.value }}</span>
            <span v-else-if="row.type === 'discount'">{{ row.value }}折</span>
            <span v-else>¥{{ row.value }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最低消费" width="110">
          <template #default="{row}">¥{{ (row.conditionAmount || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="总量" width="80" align="center">
          <template #default="{row}">{{ row.totalCount }}</template>
        </el-table-column>
        <el-table-column label="已使用" width="80" align="center">
          <template #default="{row}">{{ row.usedCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="有效期" min-width="210">
          <template #default="{row}">{{ row.validStart }} ~ {{ row.validEnd }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" effect="plain">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
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
        <el-form-item label="名称" prop="name" :rules="[{required:true,message:'请输入优惠券名称'}]">
          <el-input v-model="form.name" placeholder="优惠券名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="type" :rules="[{required:true,message:'请选择类型'}]">
              <el-radio-group v-model="form.type">
                <el-radio value="full_reduce">满减</el-radio>
                <el-radio value="discount">折扣</el-radio>
                <el-radio value="new_user">新人专享</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面值" prop="value" :rules="[{required:true,message:'请输入面值'}]">
              <el-input-number v-model="form.value" :min="1" :max="99999" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最低消费" prop="conditionAmount">
              <el-input-number v-model="form.conditionAmount" :min="0" :step="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发放总量" prop="totalCount" :rules="[{required:true,message:'请输入总数'}]">
              <el-input-number v-model="form.totalCount" :min="1" :max="999999" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="每人限领" prop="perLimit">
          <el-input-number v-model="form.perLimit" :min="1" :max="999" style="width:100%" />
        </el-form-item>
        <el-form-item label="有效期" prop="validPeriod" :rules="[{required:true,message:'请选择有效期'}]">
          <el-date-picker v-model="form.validPeriod" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:100%" />
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
import { getCouponPage, createCoupon, updateCoupon, deleteCoupon } from '@/api/modules/mall'

interface Coupon {
  id: number; name: string;
  type: string; value: number; conditionAmount: number;
  totalCount: number; usedCount: number; perLimit: number;
  validStart: string; validEnd: string; status: number;
}

const typeMap: Record<string, {label:string;type:string}> = {
  full_reduce: { label: '满减', type: 'success' },
  discount: { label: '折扣', type: 'primary' },
  new_user: { label: '新人专享', type: 'warning' },
}

const statusMap: Record<number, {label:string;type:string}> = {
  0: { label: '未发布', type: 'info' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已结束', type: 'danger' },
}

const loading = ref(false)
const couponList = ref<Coupon[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Coupon | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  couponStatus: undefined as number | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑优惠券' : '新增优惠券')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', couponStatus: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: Coupon) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    const data = {
      name: formData.name,
      type: formData.type,
      value: formData.value,
      conditionAmount: formData.conditionAmount || 0,
      totalCount: formData.totalCount,
      perLimit: formData.perLimit || 1,
      validStart: formData.validPeriod[0],
      validEnd: formData.validPeriod[1],
      status: editingRow.value ? editingRow.value.status : 0,
    }
    if (editingRow.value) {
      await updateCoupon(editingRow.value.id, data)
    } else {
      await createCoupon(data)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: Coupon) {
  try {
    await deleteCoupon(row.id)
    ElMessage.success(`已删除优惠券「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: queryParams.page,
      size: queryParams.size,
    }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.couponStatus !== undefined) params.status = queryParams.couponStatus

    const res = await getCouponPage(params)
    if (res?.data) {
      couponList.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        name: item.name || '',
        type: item.type || 'full_reduce',
        value: item.value || 0,
        conditionAmount: item.conditionAmount || 0,
        totalCount: item.totalCount || 0,
        usedCount: item.usedCount ?? 0,
        perLimit: item.perLimit ?? 1,
        validStart: item.validStart,
        validEnd: item.validEnd,
        status: item.status ?? 0,
      }))
      total.value = Number(res.data.total ?? 0)
    }
  } catch (e) {
    console.error('优惠券数据加载失败:', e)
    ElMessage.error('优惠券数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.coupon-page { max-width: 1400px; }
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
