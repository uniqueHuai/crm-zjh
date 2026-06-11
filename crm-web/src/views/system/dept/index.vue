<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">部门管理</h2>
        <p class="page-subtitle">管理组织架构与部门信息，支持多级树形结构</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增部门</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="部门名称"><el-input v-model="queryParams.keywords" placeholder="搜索部门" clearable style="width:180px" /></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
              <el-option label="启用" :value="1" /><el-option label="禁用" :value="0" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个部门</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="deptList" v-loading="loading" stripe row-key="id" default-expand-all :tree-props="{children:'children'}" max-height="600">
        <el-table-column label="部门名称" min-width="220">
          <template #default="{row}">
            <span style="font-weight:500">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(undefined, row)">新增子部门</el-button>
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除该部门?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="deptOptions" :props="{label:'name',value:'id',children:'children'}" placeholder="顶级部门" clearable check-strictly style="width:100%" />
        </el-form-item>
        <el-form-item label="部门名称" prop="name" :rules="[{required:true,message:'请输入部门名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="负责人" prop="leaderName"><el-input v-model="form.leaderName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="邮箱" prop="email" :rules="[{type:'email',message:'格式不正确',trigger:'blur'}]"><el-input v-model="form.email" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="排序号" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/modules/system/dept'
import type { SysDeptNode } from '@/api/modules/system/dept'

const loading = ref(false)
const deptList = ref<SysDeptNode[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<SysDeptNode | null>(null)
const parentRow = ref<SysDeptNode | null>(null)

const queryParams = reactive({ page:1, size:20, keywords:'', status:undefined as number|undefined })
const dialogTitle = computed(() => {
  if (parentRow.value) return `新增子部门 - ${parentRow.value.name}`
  return editingRow.value ? '编辑部门' : '新增部门'
})

const deptOptions = computed(() => [{ id:0, name:'顶级部门', children: deptList.value }])

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords:'', status:undefined, page:1 }); fetchData() }

function openDialog(row?: SysDeptNode, parent?: SysDeptNode) {
  editingRow.value = row || null
  parentRow.value = parent || null
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateDept(editingRow.value.id, formData)
    } else {
      await createDept(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: SysDeptNode) {
  try {
    await deleteDept(row.id)
    ElMessage.success(`已删除部门「${row.name}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDeptTree()
    let list = res.data.records
    // Client-side filter for search keywords and status
    if (queryParams.keywords || queryParams.status !== undefined) {
      const filterNodes = (nodes: SysDeptNode[]): SysDeptNode[] => {
        return nodes.filter(n => {
          const matchName = !queryParams.keywords || n.name.includes(queryParams.keywords!)
          const matchStatus = queryParams.status === undefined || n.status === queryParams.status
          const children = n.children ? filterNodes(n.children) : undefined
          if (children !== undefined && children.length === 0) return matchName && matchStatus
          return (matchName && matchStatus) || (children !== undefined && children.length > 0)
        }).map(n => ({ ...n, children: n.children ? filterNodes(n.children!) : undefined }))
      }
      list = filterNodes(list)
    }
    deptList.value = list
    const countNodes = (nodes: SysDeptNode[]): number => nodes.reduce((s, n) => s + 1 + (n.children ? countNodes(n.children) : 0), 0)
    total.value = countNodes(list)
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
</style>
