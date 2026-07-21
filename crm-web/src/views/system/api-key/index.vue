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
            <el-form-item label="关键字"><el-input v-model="queryParams.keywords" placeholder="搜索密钥名称" clearable style="width:180px" /></el-form-item>
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
          <span class="result-count">共 <b>{{ keyList.length }}</b> 个密钥</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="keyList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="appName" label="密钥名称" width="150" />
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
        <el-table-column label="AppSecret" min-width="200">
          <template #default="{row}">
            <span class="api-key-text">
              <span v-if="!row._showSecret">{{ maskKey(row.appSecret) }}</span>
              <span v-else style="font-family:monospace;">{{ row.appSecret }}</span>
              <el-button link type="primary" size="small" @click="row._showSecret = !row._showSecret">
                <el-icon><View v-if="!row._showSecret" /><Hide v-else /></el-icon>
              </el-button>
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-switch :model-value="row.status===1" :loading="row._statusLoading" @click="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="权限" min-width="180">
          <template #default="{row}">
            <el-tag v-for="p in (row.permissions || [])" :key="p" size="small" style="margin-right:4px">{{ p }}</el-tag>
            <span v-if="!row.permissions?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="170">
          <template #default="{row}">
            <span :style="isExpired(row.expireTime) ? 'color:var(--el-color-danger)' : ''">{{ row.expireTime || '永不过期' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后使用" width="170">
          <template #default="{row}">{{ row.lastUsedAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-popconfirm title="确认重置密钥?重置后将立即生效，原有密钥将失效" @confirm="handleResetKey(row)">
              <template #reference><el-button link type="primary" size="small">重置密钥</el-button></template>
            </el-popconfirm>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
              <template #reference><el-button link type="danger" size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <FormDialog v-model:visible="dialogVisible" title="新增密钥" :initial-data="{ appName:'', permissions:[], expireDays:30, ipWhitelist:'' }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="密钥名称" prop="appName" :rules="[{required:true,message:'请输入密钥名称'}]">
          <el-input v-model="form.appName" placeholder="请输入密钥名称" />
        </el-form-item>
        <el-form-item label="权限" prop="permissions">
          <el-select v-model="form.permissions" multiple placeholder="选择权限(可选)" style="width:100%">
            <el-option label="全部权限" value="*" />
            <el-option label="只读" value="read" />
            <el-option label="读写" value="write" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期天数" prop="expireDays">
          <el-input-number v-model="form.expireDays" :min="1" :max="3650" style="width:100%" />
        </el-form-item>
        <el-form-item label="IP白名单" prop="ipWhitelist">
          <el-input v-model="form.ipWhitelist" placeholder="多个IP用逗号分隔(可选)" />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, View, Hide } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getApiKeyList, createApiKey, updateApiKeyStatus, deleteApiKey, regenerateApiKey } from '@/api/modules/system'

interface ApiKey {
  id: number
  appName: string
  apiKey: string
  appSecret: string
  status: number
  expireTime?: string
  lastUsedAt?: string
  createdAt: string
  permissions?: string[]
  ipWhitelist?: string[]
  _showKey?: boolean
  _showSecret?: boolean
  _statusLoading?: boolean
}

const loading = ref(false)
const keyList = ref<ApiKey[]>([])
const showSearch = ref(true)
const dialogVisible = ref(false)

const queryParams = reactive({
  keywords: '',
  status: undefined as number | undefined,
})

function maskKey(key: string): string {
  if (!key) return ''
  if (key.length <= 8) return '****'
  return key.slice(0, 6) + '****' + key.slice(-4)
}

function isExpired(expireTime?: string): boolean {
  if (!expireTime) return false
  return new Date(expireTime) < new Date()
}

function handleSearch() { fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', status: undefined })
  fetchData()
}
function openDialog() { dialogVisible.value = true }

async function handleSubmit(formData: any, done: () => void) {
  try {
    const payload = {
      appName: formData.appName,
      permissions: formData.permissions || [],
      expireDays: formData.expireDays || 30,
      ipWhitelist: formData.ipWhitelist
        ? (formData.ipWhitelist as string).split(',').map((s: string) => s.trim()).filter(Boolean)
        : undefined,
    }
    const res = await createApiKey(payload)
    ElMessage.success('新增成功')
    done()
    ElMessageBox.alert(
      `<div style="line-height:2">
        <p><b>ApiKey：</b><code style="user-select:all;background:#f5f5f5;padding:2px 6px;border-radius:3px">${res.data.apiKey}</code></p>
        <p><b>AppSecret：</b><code style="user-select:all;background:#f5f5f5;padding:2px 6px;border-radius:3px">${res.data.appSecret}</code></p>
        <p style="color:#e6a23c;font-size:12px;margin-top:8px">密钥仅创建时可见，请立即妥善保管</p>
      </div>`,
      '密钥创建成功',
      { dangerouslyUseHTMLString: true, confirmButtonText: '已妥善保管', type: 'success' }
    )
    fetchData()
  } catch { done() }
}

async function handleToggle(row: ApiKey) {
  row._statusLoading = true
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateApiKeyStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch { /* handled by interceptor */ }
  finally { row._statusLoading = false }
}

async function handleResetKey(row: ApiKey) {
  try {
    const res = await regenerateApiKey(row.id)
    row.apiKey = res.data.apiKey
    row.appSecret = res.data.appSecret
    ElMessage.success('密钥已重置，请妥善保管新密钥')
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: ApiKey) {
  try {
    await deleteApiKey(row.id)
    ElMessage.success(`已删除密钥「${row.appName}」`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getApiKeyList()
    let list = (res.data || []).map((item: any) => ({
      ...item,
      _showKey: false,
      _showSecret: false,
      _statusLoading: false,
    })) as ApiKey[]
    const kw = queryParams.keywords?.toLowerCase()
    if (kw) list = list.filter(i => i.appName?.toLowerCase().includes(kw))
    if (queryParams.status !== undefined) list = list.filter(i => i.status === queryParams.status)
    keyList.value = list
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
.search-form { margin-bottom:16px; padding-bottom:16px; border-bottom:1px solid var(--crm-border-light);
  .el-form{display:flex;flex-wrap:wrap;gap:0} :deep(.el-form-item){margin-bottom:0}
}
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;
  .result-count { font-size:13px; color:var(--crm-text-secondary); b{color:var(--crm-text-primary);} }
  &-right { display:flex; align-items:center; gap:4px; }
}
.api-key-text { display:inline-flex; align-items:center; gap:4px; font-family:monospace; font-size:13px; }
</style>
