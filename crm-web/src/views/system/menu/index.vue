<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">菜单管理</h2>
        <p class="page-subtitle">配置系统菜单与权限标识，支持动态路由</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增菜单</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="menuList" v-loading="loading" stripe row-key="id" default-expand-all :tree-props="{children:'children'}" border max-height="700" indent="28" :row-class-name="rowClassName">
        <el-table-column label="菜单名称" min-width="300">
          <template #default="{row}">
            <div class="menu-name-cell" :class="'depth-' + row.menuType">
              <div class="menu-visual">
                <div class="depth-line" v-if="row.menuType!=='M'"></div>
                <div class="depth-dot" :class="'dot-' + row.menuType"></div>
              </div>
              <el-icon :size="row.menuType==='M'?18:16" class="menu-icon" :class="'icon-' + row.menuType">
                <component :is="row.icon || 'Menu'" />
              </el-icon>
              <span class="menu-label" :class="'label-' + row.menuType">{{ row.name }}</span>
              <el-tag :type="row.menuType==='M'?null:row.menuType==='C'?'success':'warning'" size="small" effect="plain" class="type-tag">
                {{ row.menuType==='M'?'目录':row.menuType==='C'?'菜单':'按钮' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{row}"><el-icon v-if="row.icon" :size="18"><component :is="row.icon" /></el-icon><span v-else class="text-muted">-</span></template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{row}">
            <el-tag :type="row.menuType==='M'?'primary':row.menuType==='C'?'success':'warning'" size="small">
              {{ row.menuType==='M'?'目录':row.menuType==='C'?'菜单':'按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由/权限标识" min-width="220">
          <template #default="{row}">{{ row.menuType==='F' ? row.permissionCode : row.routePath }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status!==0?'success':'danger'" size="small">{{ row.status!==0?'启用':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">新增子菜单</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="有子菜单会一并删除，确认?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" width="550px" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="menuOptions" :props="{label:'name',value:'id',children:'children'}" placeholder="顶级菜单" clearable check-strictly style="width:100%" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name" :rules="[{required:true,message:'请输入名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item v-if="form.menuType!=='F'" label="路由路径" prop="routePath"><el-input v-model="form.routePath" placeholder="如 /customer/lead" /></el-form-item>
        <el-form-item v-if="form.menuType!=='F'" label="组件路径" prop="component"><el-input v-model="form.component" placeholder="如 customer/lead/index" /></el-form-item>
        <el-form-item v-if="form.menuType==='F'" label="权限标识" prop="permissionCode"><el-input v-model="form.permissionCode" placeholder="如 customer:create" /></el-form-item>
        <el-form-item v-if="form.menuType!=='F'" label="图标" prop="icon"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="排序号" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Menu } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/modules/system/menu'
import type { SysMenuNode } from '@/api/modules/system/menu'

const loading = ref(false)
const menuList = ref<SysMenuNode[]>([])
const dialogVisible = ref(false)
const editingRow = ref<SysMenuNode | null>(null)
const parentRow = ref<SysMenuNode | null>(null)
const dialogTitle = computed(() => parentRow.value ? `新增子菜单 - ${parentRow.value.name}` : editingRow.value ? '编辑菜单' : '新增菜单')

const menuOptions = computed(() => [{ id:0, name:'顶级菜单', children: menuList.value }])

function rowClassName({ row }: { row: SysMenuNode }) {
  return 'menu-row-' + row.menuType
}

function openDialog(parent?: SysMenuNode) {
  parentRow.value = parent || null
  editingRow.value = null
  dialogVisible.value = true
}
function handleEdit(row: SysMenuNode) {
  parentRow.value = null
  editingRow.value = row
  dialogVisible.value = true
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateMenu(editingRow.value.id, formData)
    } else {
      await createMenu(formData)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { done() }
}

async function handleDelete(row: SysMenuNode) {
  try {
    await deleteMenu(row.id)
    ElMessage.success(`已删除「${row.name}」及其子菜单`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuList.value = res.data.records
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
.card { background:var(--crm-bg-white); border-radius:var(--crm-radius-lg); border:1px solid var(--crm-border); overflow:hidden; }
.menu-name-cell { display:flex; align-items:center; gap:8px; font-weight:500; }
.menu-icon { flex-shrink:0; }
.icon-M { color:var(--crm-primary,#409eff); }
.icon-C { color:var(--crm-text-primary); }
.icon-F { color:var(--crm-text-secondary); }
.menu-label { font-size:14px; }
.label-M { font-weight:600; font-size:14px; }
.label-C { font-weight:500; }
.label-F { font-size:13px; color:var(--crm-text-secondary); }
.type-tag { flex-shrink:0; }
.menu-visual { display:flex; align-items:center; gap:2px; flex-shrink:0; }
.depth-line { width:12px; height:1px; background:var(--crm-border,#dcdfe6); }
.depth-dot { width:4px; height:4px; border-radius:50%; }
.dot-C { background:var(--crm-primary,#409eff); }
.dot-F { background:var(--crm-text-placeholder,#c0c4cc); }
.text-muted { color:var(--crm-text-placeholder); }

:deep(.menu-row-M) { background:var(--crm-bg-gray,#f5f7f8); }
:deep(.menu-row-M > td) { border-bottom:1px solid var(--crm-border-light,#e8eaed) !important; }
:deep(.menu-row-C > td) { border-bottom-color:var(--crm-border-light,#e8eaed); }
</style>
