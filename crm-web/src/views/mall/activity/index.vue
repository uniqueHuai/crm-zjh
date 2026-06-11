<template>
  <div class="mall-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">营销活动</h2>
        <p class="page-subtitle">管理商城营销活动与促销策略</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增活动</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="活动名称"><el-input v-model="queryParams.keywords" placeholder="搜索活动" clearable style="width:180px" /></el-form-item>
            <el-form-item label="类型"><el-select v-model="queryParams.activityType" placeholder="全部" clearable style="width:130px">
              <el-option label="秒杀" value="seckill" /><el-option label="拼团" value="group" /><el-option label="砍价" value="cut" /><el-option label="积分兑换" value="points" />
            </el-select></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.activityStatus" placeholder="全部" clearable style="width:110px">
              <el-option label="草稿" :value="0" /><el-option label="已发布" :value="1" /><el-option label="已结束" :value="2" />
            </el-select></el-form-item>
            <el-form-item><el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <div class="toolbar">
        <div class="toolbar-left"><span class="result-count">共 <b>{{ total }}</b> 个活动</span></div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="activityList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column label="类型" width="100">
          <template #default="{row}"><el-tag size="small" effect="plain">{{ typeMap[row.type] || row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':row.status===0?'info':'danger'" size="small">
              {{ row.status===1?'已发布':row.status===0?'草稿':'已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170" prop="startTime" />
        <el-table-column label="结束时间" width="170" prop="endTime" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status===0" link type="success" size="small" @click="handleStart(row)">发布</el-button>
            <el-button v-if="row.status===1" link type="warning" size="small" @click="handleEnd(row)">结束</el-button>
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
        <el-form-item label="活动名称" prop="name" :rules="[{required:true,message:'请输入名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="活动类型" prop="type" :rules="[{required:true,message:'请选择类型'}]">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="秒杀" value="seckill" /><el-option label="拼团" value="group" />
            <el-option label="砍价" value="cut" /><el-option label="积分兑换" value="points" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动规则" prop="rules"><el-input v-model="form.rules" type="textarea" :rows="3" placeholder="JSON格式的活动规则" /></el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getActivityPage, createActivity, updateActivity, deleteActivity, updateActivityStatus } from '@/api/modules/mall'

interface Activity {
  id: number; name: string; type: string; status: number;
  startTime: string; endTime: string; rules?: string;
}

const typeMap: Record<string, string> = { seckill:'秒杀', group:'拼团', cut:'砍价', points:'积分兑换' }

const loading = ref(false)
const activityList = ref<Activity[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Activity | null>(null)
const queryParams = reactive({ page:1, size:20, keywords:'', activityType:undefined as string|undefined, activityStatus:undefined as number|undefined })
const dialogTitle = computed(() => editingRow.value ? '编辑活动' : '新增活动')

function handleSearch() { queryParams.page=1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords:'', activityType:undefined, activityStatus:undefined, page:1 }); fetchData() }
function openDialog(row?: Activity) {
  editingRow.value = row || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    const data = {
      name: formData.name,
      type: formData.type,
      startTime: formData.startTime,
      endTime: formData.endTime,
      rules: formData.rules ? formData.rules : null,
      status: editingRow.value ? editingRow.value.status : 0,
    }
    if (editingRow.value) {
      await updateActivity(editingRow.value.id, data)
    } else {
      await createActivity(data)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleStart(row: Activity) {
  try {
    await updateActivityStatus(row.id, 1)
    ElMessage.success(`活动「${row.name}」已发布`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleEnd(row: Activity) {
  try {
    await updateActivityStatus(row.id, 2)
    ElMessage.success(`活动「${row.name}」已结束`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: Activity) {
  try {
    await deleteActivity(row.id)
    ElMessage.success(`已删除活动「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: queryParams.page, size: queryParams.size }
    if (queryParams.keywords) params.keywords = queryParams.keywords
    if (queryParams.activityType) params.type = queryParams.activityType
    if (queryParams.activityStatus !== undefined) params.status = queryParams.activityStatus

    const res = await getActivityPage(params)
    if (res?.data) {
      activityList.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        name: item.name || '',
        type: item.type || '',
        status: item.status ?? 0,
        startTime: item.startTime,
        endTime: item.endTime,
        rules: item.rules ? (typeof item.rules === 'string' ? item.rules : JSON.stringify(item.rules)) : '',
      }))
      total.value = Number(res.data.total ?? 0)
    }
  } catch (e) {
    console.error('活动数据加载失败:', e)
    ElMessage.error('活动数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.mall-page { max-width: 1400px; }
.page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px;
  .page-title { margin:0; font-size:20px; font-weight:700; color:var(--crm-text-primary); }
  .page-subtitle { margin:4px 0 0; font-size:13px; color:var(--crm-text-secondary); }
  &-right { display:flex; gap:8px; flex-shrink:0; }
}
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); padding:20px; }
.search-form { margin-bottom:16px; padding-bottom:16px; border-bottom:1px solid var(--crm-border-light);
  .el-form { display:flex; flex-wrap:wrap; gap:0; } :deep(.el-form-item) { margin-bottom:0; }
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b { color:var(--crm-text-primary); } }
  &-right { display:flex; align-items:center; gap:4px; }
}
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
