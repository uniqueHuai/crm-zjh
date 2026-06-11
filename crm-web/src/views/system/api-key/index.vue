<template>
  <div class="sys-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">API密钥管理</h2>
        <p class="page-subtitle">管理第三方系统接口密钥，控制API访问权限</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增密钥</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键字"><el-input v-model="queryParams.keywords" placeholder="搜索名称/AppId" clearable style="width:180px" /></el-form-item>
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
          <span class="result-count">共 <b>{{ total }}</b> 个密钥</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="keyList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="密钥名称" width="150" />
        <el-table-column prop="appId" label="AppId" width="200">
          <template #default="{row}"><el-tag size="small" effect="plain">{{ row.appId }}</el-tag></template>
        </el-table-column>
        <el-table-column label="ApiKey" min-width="200">
          <template #default="{row}">
            <span class="api-key-text">
              <span v-if="!row._showKey">{{ maskKey(row.apiKey) }}</span>
              <span v-else style="font-family:monospace;">{{ row.apiKey }}</span>
              <el-button link type="primary" size="small" @click="row._showKey = !row._showKey">
                <el-icon><View v-if="!row._showKey" /><Hide v-else /></el-icon>
              </el-button>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-switch :model-value="row.status===1" :loading="row._statusLoading" @click="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="最后使用" width="170">
          <template #default="{row}">{{ row.lastUsedAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="过期时间" width="170">
          <template #default="{row}">
            <span :style="isExpired(row.expireAt) ? 'color:var(--el-color-danger)' : ''">{{ row.expireAt || '永不过期' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认重置密钥?重置后将立即生效，原有密钥将失效" @confirm="handleResetKey(row)">
              <template #reference><el-button link type="primary" size="small">重置密钥</el-button></template>
            </el-popconfirm>
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
        <el-form-item label="密钥名称" prop="name" :rules="[{required:true,message:'请输入密钥名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="过期时间" prop="expireAt">
          <el-date-picker v-model="form.expireAt" type="datetime" placeholder="选择过期时间(留空永不过期)" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" clearable />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, View, Hide } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'

interface ApiKey {
  id: number; name: string; appId: string; apiKey: string;
  status: number; lastUsedAt?: string; createdAt: string; expireAt?: string;
  _showKey?: boolean; _statusLoading?: boolean;
}

const loading = ref(false)
const keyList = ref<ApiKey[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<ApiKey | null>(null)

const queryParams = reactive({ page:1, size:20, keywords:'', status:undefined as number|undefined })
const dialogTitle = computed(() => editingRow.value ? '编辑密钥' : '新增密钥')

function maskKey(key: string): string {
  if (key.length <= 8) return '****'
  return key.slice(0, 6) + '****' + key.slice(-4)
}

function isExpired(expireAt?: string): boolean {
  if (!expireAt) return false
  return new Date(expireAt) < new Date()
}

function generateAppId(): string {
  return 'app_' + Math.random().toString(36).substring(2, 10)
}

function generateApiKey(): string {
  const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let key = 'sk_'
  for (let i = 0; i < 48; i++) key += chars.charAt(Math.floor(Math.random() * chars.length))
  return key
}

const mockData: ApiKey[] = [
  { id:1, name:'第三方集成密钥', appId:generateAppId(), apiKey:generateApiKey(), status:1, lastUsedAt:'2026-05-29 08:30:00', createdAt:'2024-06-01', expireAt:'2027-06-01 00:00:00' },
  { id:2, name:'移动端接口密钥', appId:generateAppId(), apiKey:generateApiKey(), status:1, lastUsedAt:'2026-05-28 18:20:00', createdAt:'2024-08-15', expireAt:'2026-12-31 23:59:59' },
  { id:3, name:'开放平台密钥', appId:generateAppId(), apiKey:generateApiKey(), status:0, lastUsedAt:undefined, createdAt:'2025-01-10', expireAt:undefined },
  { id:4, name:'数据分析接口密钥', appId:generateAppId(), apiKey:generateApiKey(), status:1, lastUsedAt:'2026-05-27 09:15:00', createdAt:'2025-03-20', expireAt:'2026-06-01 00:00:00' },
]

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords:'', status:undefined, page:1 }); fetchData() }
function openDialog(row?: ApiKey) { editingRow.value = row || null; dialogVisible.value = true }

function handleSubmit(formData: any, done: () => void) {
  ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
  done()
  fetchData()
}

function handleToggle(row: ApiKey) {
  row._statusLoading = true
  setTimeout(() => {
    row.status = row.status === 1 ? 0 : 1
    row._statusLoading = false
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  }, 500)
}

function handleResetKey(row: ApiKey) {
  ElMessageBox.confirm(
    `确认重置密钥「${row.name}」？重置后原有密钥将立即失效，需要更新集成配置。`,
    '重置确认',
    { confirmButtonText:'确认重置', cancelButtonText:'取消', type:'warning' }
  ).then(() => {
    row.apiKey = generateApiKey()
    ElMessage.success('密钥已重置，请妥善保管新密钥')
  }).catch(() => {})
}

function handleDelete(row: ApiKey) {
  ElMessage.success(`已删除密钥「${row.name}」`)
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    await new Promise(r => setTimeout(r, 300))
    let list = [...mockData]
    if (queryParams.keywords) {
      const kw = queryParams.keywords
      list = list.filter(i => i.name.includes(kw!) || i.appId.includes(kw!))
    }
    if (queryParams.status !== undefined) list = list.filter(i => i.status === queryParams.status)
    keyList.value = list; total.value = list.length
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
.api-key-text { display:inline-flex; align-items:center; gap:4px; font-family:monospace; font-size:13px; }
</style>
