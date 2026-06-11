<template>
  <div class="page-template-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">小程序页面管理</h2>
        <p class="page-subtitle">管理商城小程序的页面模板，配置页面结构和展示内容</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增页面</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词">
              <el-input v-model="queryParams.keywords" placeholder="页面名称" clearable style="width:160px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个页面</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="pageList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="页面名称" min-width="160">
          <template #default="{row}"><span class="cell-name">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column label="页面类型" width="120">
          <template #default="{row}">
            <el-tag :type="typeMap[row.pageType]?.type || 'info'" size="small" effect="light">
              {{ typeMap[row.pageType]?.label || row.pageType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预览图" width="100" align="center">
          <template #default="{row}">
            <el-image
              v-if="row.previewImage"
              :src="row.previewImage"
              style="width:60px;height:80px"
              fit="cover"
              :preview-src-list="[row.previewImage]"
              preview-teleported
            >
              <template #error>
                <div class="image-placeholder">无预览</div>
              </template>
            </el-image>
            <span v-else class="no-image">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{row}">{{ row.updateTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status === 0" link type="primary" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="openPreview(row)">预览</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="formInitData" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="页面名称" prop="name" :rules="[{required:true,message:'请输入页面名称'}]">
          <el-input v-model="form.name" placeholder="例如：首页、分类页" maxlength="20" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="页面类型" prop="pageType" :rules="[{required:true,message:'请选择页面类型'}]">
              <el-radio-group v-model="form.pageType">
                <el-radio value="homepage">首页</el-radio>
                <el-radio value="category">分类页</el-radio>
                <el-radio value="product_list">商品列表</el-radio>
                <el-radio value="personal">个人中心</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预览图" prop="previewImage">
              <el-input v-model="form.previewImage" placeholder="图片URL" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="页面配置" prop="pageConfig">
          <el-input
            v-model="form.pageConfig"
            type="textarea"
            :rows="12"
            placeholder='模块配置 JSON 数组，支持的类型：search-搜索栏、banner-轮播图、notice-公告、category_grid-分类导航、product_list-商品列表、activity_list-活动列表、image_ad-图片广告'
          />
          <div class="config-tip">
            <p>模块格式示例：</p>
            <pre>[
  {"type":"search","props":{"placeholder":"搜索商品"}},
  {"type":"banner","props":{}},
  {"type":"notice","props":{"text":"五一特惠，全场满减","link":""}},
  {"type":"category_grid","props":{"title":"分类导航"}},
  {"type":"product_list","props":{"title":"热门推荐","count":6}},
  {"type":"activity_list","props":{"title":"限时活动"}},
  {"type":"image_ad","props":{"image":"https://...","link":""}}
]</pre>
          </div>
        </el-form-item>
      </template>
    </FormDialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="页面预览" width="420px" top="5vh">
      <div class="preview-container">
        <div class="phone-frame">
          <div class="phone-statusbar"></div>
          <div class="phone-content">
            <pre v-if="previewRow?.pageConfig" class="config-json">{{ formatJson(previewRow.pageConfig) }}</pre>
            <el-empty v-else description="暂无页面配置" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getPageTemplatePage, createPageTemplate, updatePageTemplate, deletePageTemplate, publishPageTemplate } from '@/api/modules/mall'

interface PageTemplate {
  id: number
  name: string
  pageType: string
  previewImage: string
  pageConfig: string
  status: number
  updateTime: string
}

const typeMap: Record<string, { label: string; type: string }> = {
  homepage: { label: '首页', type: 'primary' },
  category: { label: '分类页', type: 'success' },
  product_list: { label: '商品列表', type: 'warning' },
  personal: { label: '个人中心', type: 'info' },
}

const loading = ref(false)
const pageList = ref<PageTemplate[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const editingRow = ref<PageTemplate | null>(null)
const previewRow = ref<PageTemplate | null>(null)

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  status: undefined as number | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑页面' : '新增页面')
const formInitData = computed(() => {
  if (!editingRow.value) return {}
  return {
    name: editingRow.value.name,
    pageType: editingRow.value.pageType,
    previewImage: editingRow.value.previewImage,
    pageConfig: editingRow.value.pageConfig,
  }
})

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() {
  Object.assign(queryParams, { keywords: '', status: undefined, page: 1 })
  fetchData()
}
function openDialog(row?: PageTemplate) {
  editingRow.value = row || null
  dialogVisible.value = true
}
function openPreview(row: PageTemplate) {
  previewRow.value = row
  previewVisible.value = true
}
function formatJson(val: string) {
  try {
    return JSON.stringify(JSON.parse(val), null, 2)
  } catch {
    return val
  }
}

async function handleSubmit(formData: any, done: () => void) {
  try {
    const data = {
      name: formData.name,
      pageType: formData.pageType,
      previewImage: formData.previewImage || '',
      pageConfig: formData.pageConfig || '',
      status: editingRow.value ? editingRow.value.status : 0,
    }
    if (editingRow.value) {
      await updatePageTemplate(editingRow.value.id, data)
    } else {
      await createPageTemplate(data)
    }
    ElMessage.success(editingRow.value ? '修改成功' : '新增成功')
    done()
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handlePublish(row: PageTemplate) {
  try {
    await publishPageTemplate(row.id)
    ElMessage.success(`页面「${row.name}」已发布`)
    fetchData()
  } catch { /* handled by interceptor */ }
}

async function handleDelete(row: PageTemplate) {
  try {
    await deletePageTemplate(row.id)
    ElMessage.success(`已删除页面「${row.name}」`)
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
    if (queryParams.status !== undefined) params.status = queryParams.status

    const res = await getPageTemplatePage(params)
    if (res?.data) {
      pageList.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        name: item.name || '',
        pageType: item.pageType || 'homepage',
        previewImage: item.previewImage || '',
        pageConfig: item.pageConfig || '',
        status: item.status ?? 0,
        updateTime: item.updateTime || '',
      }))
      total.value = Number(res.data.total ?? 0)
    }
  } catch (e) {
    console.error('页面模板数据加载失败:', e)
    ElMessage.error('页面模板数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-template-page { max-width: 1400px; }
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
.no-image { color:var(--crm-text-tertiary); }
.image-placeholder { width:60px;height:80px;display:flex;align-items:center;justify-content:center;font-size:11px;color:var(--crm-text-tertiary);background:var(--crm-bg-secondary);border-radius:4px; }

.preview-container { display:flex;justify-content:center;padding:8px 0; }
.phone-frame { width:340px;background:#fff;border-radius:24px;border:2px solid #e5e5e5;overflow:hidden;box-shadow:0 8px 30px rgba(0,0,0,.12); }
.config-tip { margin-top:8px;padding:12px;background:#fafafa;border-radius:6px;border:1px solid #eee;font-size:12px;color:#666;line-height:1.6;
  p { margin:0 0 4px;font-weight:500; }
  pre { margin:0;font-size:11px;white-space:pre-wrap;word-break:break-all;color:#333;background:#f5f5f5;padding:8px;border-radius:4px; }
}
.phone-statusbar { height:30px;background:#f5f5f5; }
.phone-content { padding:16px;max-height:500px;overflow-y:auto; }
.config-json { margin:0;font-size:12px;line-height:1.6;white-space:pre-wrap;word-break:break-all;color:#333; }
</style>
