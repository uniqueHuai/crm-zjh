<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理系统用户账号与权限分配</p>
      </div>
      <div class="page-header-right">
        <el-button @click="handleImport"><el-icon><Upload /></el-icon>导入</el-button>
        <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增用户</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="用户名"><el-input v-model="queryParams.keywords" placeholder="搜索用户" clearable style="width:160px" /></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
              <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
            </el-select></el-form-item>
            <el-form-item label="部门"><el-select v-model="queryParams.deptId" placeholder="全部" clearable style="width:150px">
              <el-option label="技术部" :value="1" /><el-option label="销售部" :value="2" /><el-option label="财务部" :value="3" />
            </el-select></el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <b>{{ total }}</b> 个用户</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="userList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="username" label="用户名" width="120"><template #default="{row}"><el-tag size="small" effect="plain">{{ row.username }}</el-tag></template></el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-switch :model-value="row.status === 1" :loading="row._statusLoading" @click="handleToggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="170">
          <template #default="{row}">{{ row.lastLoginAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link type="primary" size="small" @click="handleAssignRole(row)">分配角色</el-button>
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
        <el-form-item label="用户名" prop="username" :rules="[{required:true,message:'请输入用户名'}]"><el-input v-model="form.username" /></el-form-item>
        <el-form-item v-if="!editingRow" label="密码" prop="password" :rules="[{required:true,message:'请输入密码'},{min:4,message:'密码至少4位'}]"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="真实姓名" prop="realName" :rules="[{required:true,message:'请输入姓名'}]"><el-input v-model="form.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="邮箱" prop="email" :rules="[{type:'email',message:'格式不正确',trigger:'blur'}]"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="部门" prop="deptId"><el-select v-model="form.deptId" style="width:100%"><el-option label="技术部" :value="1" /><el-option label="销售部" :value="2" /><el-option label="财务部" :value="3" /></el-select></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Upload, Download } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getUserPage, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/modules/system/user'
import type { SysUser } from '@/api/modules/system/user'

const loading = ref(false)
const userList = ref<SysUser[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<SysUser | null>(null)

const queryParams = reactive({ page:1, size:20, keywords:'', status:undefined as number|undefined, deptId:undefined as number|undefined })
const dialogTitle = computed(() => editingRow.value ? '编辑用户' : '新增用户')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords:'', status:undefined, deptId:undefined, page:1 }); fetchData() }
function openDialog(row?: SysUser) { editingRow.value = row || null; dialogVisible.value = true }

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateUser(editingRow.value.id, formData)
    } else {
      await createUser(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

function handleImport() { ElMessage.info('功能开发中') }
function handleExport() { ElMessage.info('功能开发中') }

async function handleToggleStatus(row: SysUser) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch { /* handled by interceptor */ }
}

async function handleResetPwd(row: SysUser) {
  try {
    await ElMessageBox.confirm(`确认重置用户「${row.username}」的密码？`, '提示', { confirmButtonText:'确定', cancelButtonText:'取消', type:'warning' })
    await resetUserPassword(row.id, '123456')
    ElMessage.success('密码已重置为 123456')
  } catch { /* cancelled or error */ }
}

function handleAssignRole(row: SysUser) {
  ElMessage.info('功能开发中')
}

async function handleDelete(row: SysUser) {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { confirmButtonText:'确定', cancelButtonText:'取消', type:'warning' })
    await deleteUser(row.id)
    ElMessage.success(`已删除用户「${row.username}」`)
    fetchData()
  } catch { /* cancelled or error */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.data.records
    total.value = Number(res.data.total)
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
  .el-form { display:flex; flex-wrap:wrap; gap:0; }
  :deep(.el-form-item) { margin-bottom:0; }
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b{color:var(--crm-text-primary);} }
  &-right { display:flex; align-items:center; gap:4px; }
}
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
