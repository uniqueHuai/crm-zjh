<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">角色管理</h2>
        <p class="page-subtitle">管理系统角色与权限分配</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增角色</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="角色名称"><el-input v-model="queryParams.keywords" placeholder="搜索角色" clearable style="width:200px" /></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
              <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
            </el-select></el-form-item>
            <el-form-item><el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <div class="toolbar">
        <div class="toolbar-left"><span class="result-count">共 <b>{{ total }}</b> 个角色</span></div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="roleList" v-loading="loading" stripe row-key="id" max-height="600">
        <el-table-column prop="name" label="角色名称" width="160" />
        <el-table-column prop="roleCode" label="角色编码" width="150"><template #default="{row}"><el-tag size="small" effect="plain">{{ row.roleCode }}</el-tag></template></el-table-column>
        <el-table-column label="数据范围" width="120">
          <template #default="{row}">
            <el-tag size="small" :type="row.dataScope===4?'danger':row.dataScope===1?'info':'primary'">{{ scopeMap[row.dataScope] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}"><el-switch :model-value="row.status===1" @click="handleToggle(row)" /></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="openMenuDialog(row)">菜单权限</el-button>
            <el-button link type="primary" size="small" @click="openScopeDialog(row)">数据权限</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap"><el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.size" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next, jumper" background @change="fetchData" /></div>
    </div>

    <!-- Role Form Dialog -->
    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="角色名称" prop="name" :rules="[{required:true,message:'请输入名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色编码" prop="roleCode" :rules="[{required:true,message:'请输入编码'}]"><el-input v-model="form.roleCode" /></el-form-item>
        <el-form-item label="数据范围" prop="dataScope"><el-select v-model="form.dataScope" style="width:100%">
          <el-option label="仅本人数据" :value="1" /><el-option label="本部门数据" :value="2" /><el-option label="本部门及下属" :value="3" /><el-option label="全部数据" :value="4" /><el-option label="自定义" :value="5" />
        </el-select></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </template>
    </FormDialog>

    <!-- Menu Permission Dialog -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="500px" :close-on-click-modal="false" @opened="onMenuDialogOpened">
      <el-tree ref="menuTreeRef" :data="menuTreeData" show-checkbox node-key="id" :props="{label:'name',children:'children'}" default-expand-all :check-strictly="false" />
      <template #footer>
        <el-button @click="menuDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenuPerm">保存</el-button>
      </template>
    </el-dialog>

    <!-- Data Scope Dialog -->
    <el-dialog v-model="scopeDialogVisible" title="分配数据权限" width="450px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="数据范围"><el-radio-group v-model="scopeForm.dataScope">
          <el-radio :value="1">仅本人数据</el-radio>
          <el-radio :value="2">本部门数据</el-radio>
          <el-radio :value="4">全部数据</el-radio>
        </el-radio-group></el-form-item>
        <el-form-item label="指定部门(可选)"><el-select v-model="scopeForm.deptIds" multiple placeholder="选择部门" style="width:100%">
          <el-option label="技术部" :value="1" /><el-option label="销售部" :value="2" /><el-option label="财务部" :value="3" />
        </el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="scopeDialogVisible=false">取消</el-button><el-button type="primary" @click="handleSaveScope">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import type { TreeInstance } from 'element-plus'
import { getRolePage, createRole, updateRole, deleteRole, assignRoleMenus, assignRoleDataScope, getRoleDetail } from '@/api/modules/system/role'
import { getMenuTree } from '@/api/modules/system/menu'
import type { SysRole } from '@/api/modules/system/role'
import type { SysMenuNode } from '@/api/modules/system/menu'

const scopeMap: Record<number, string> = { 1:'仅本人', 2:'本部门', 3:'部门及下属', 4:'全部', 5:'自定义' }

const loading = ref(false)
const roleList = ref<SysRole[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const scopeDialogVisible = ref(false)
const editingRow = ref<SysRole | null>(null)
const menuTreeRef = ref<TreeInstance>()

const queryParams = reactive({ page:1, size:20, keywords:'', status:undefined as number|undefined })
const dialogTitle = computed(() => editingRow.value ? '编辑角色' : '新增角色')

const scopeForm = reactive({ dataScope:2, deptIds:[] as number[] })
const menuTreeData = ref<SysMenuNode[]>([])
const checkedMenuIds = ref<number[]>([])

function handleSearch() { queryParams.page=1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords:'', status:undefined, page:1 }); fetchData() }
function openDialog(row?: SysRole) { editingRow.value=row||null; dialogVisible.value=true }

async function handleSubmit(formData:any, done:()=>void) {
  try {
    if (editingRow.value) {
      await updateRole(editingRow.value.id, formData)
    } else {
      await createRole(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleToggle(row: SysRole) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateRole(row.id, { status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: SysRole) {
  try {
    await deleteRole(row.id)
    ElMessage.success(`已删除角色「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function openMenuDialog(row: SysRole) {
  editingRow.value = row
  if (menuTreeData.value.length === 0) {
    try {
      const res = await getMenuTree()
      menuTreeData.value = res.data.records
    } catch { /* handled by interceptor */ }
  }
  // Load checked menu ids
  try {
    const res = await getRoleDetail(row.id)
    checkedMenuIds.value = res.data.menuIds || []
  } catch { /* handled by interceptor */ }
  menuDialogVisible.value = true
}

function onMenuDialogOpened() {
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys(checkedMenuIds.value)
  })
}

async function handleSaveMenuPerm() {
  if (!editingRow.value) return
  const ids = menuTreeRef.value?.getCheckedKeys(false) || []
  try {
    await assignRoleMenus(editingRow.value.id, ids as number[])
    ElMessage.success('菜单权限已更新')
    menuDialogVisible.value = false
  } catch { /* handled by interceptor */ }
}

function openScopeDialog(row: SysRole) {
  editingRow.value = row
  scopeForm.dataScope = row.dataScope
  scopeForm.deptIds = row.deptIds || []
  scopeDialogVisible.value = true
}

async function handleSaveScope() {
  if (!editingRow.value) return
  try {
    await assignRoleDataScope(editingRow.value.id, scopeForm.dataScope, scopeForm.deptIds)
    ElMessage.success('数据权限已更新')
    scopeDialogVisible.value = false
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    roleList.value = res.data.records
    total.value = Number(res.data.total ?? 0)
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
</style>
