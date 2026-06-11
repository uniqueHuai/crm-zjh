<template>
  <div class="sales-page">
    <div class="page-header">
      <div class="page-header-left">
        <h2 class="page-title">产品管理</h2>
        <p class="page-subtitle">管理产品目录与定价信息</p>
      </div>
      <div class="page-header-right">
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增产品</el-button>
      </div>
    </div>

    <div class="card">
      <el-collapse-transition>
        <div v-if="showSearch" class="search-form">
          <el-form :model="queryParams" inline size="default" @keyup.enter="handleSearch">
            <el-form-item label="关键词"><el-input v-model="queryParams.keywords" placeholder="产品名称" clearable style="width:160px" /></el-form-item>
            <el-form-item label="分类"><el-select v-model="queryParams.categoryId" placeholder="全部" clearable style="width:130px">
              <el-option v-for="c in flatCategories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select></el-form-item>
            <el-form-item label="状态"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:110px">
              <el-option label="上架" :value="1" /><el-option label="下架" :value="0" />
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
          <span class="result-count">共 <b>{{ total }}</b> 个产品</span>
        </div>
        <div class="toolbar-right">
          <el-button text @click="showSearch = !showSearch"><el-icon><Search /></el-icon>{{ showSearch ? '隐藏' : '搜索' }}</el-button>
          <el-tooltip content="刷新"><el-button text @click="fetchData"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
        </div>
      </div>

      <el-table :data="productList" v-loading="loading" stripe max-height="600">
        <el-table-column prop="name" label="产品名称" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="100">
          <template #default="{row}">
            <el-tag size="small" effect="plain">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="standardPrice" label="售价" width="120">
          <template #default="{row}">¥{{ (row.standardPrice || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="costPrice" label="成本价" width="120">
          <template #default="{row}">¥{{ (row.costPrice || 0).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
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

    <FormDialog v-model:visible="dialogVisible" :title="dialogTitle" :initial-data="editingRow || { status: 1 }" @submit="handleSubmit">
      <template #default="{form}">
        <el-form-item label="产品名称" prop="name" :rules="[{required:true,message:'请输入产品名称'}]"><el-input v-model="form.name" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="分类" prop="categoryId"><el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in flatCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" style="width:100%">
            <el-option label="套" value="套" /><el-option label="个" value="个" /><el-option label="台" value="台" /><el-option label="次" value="次" /><el-option label="年" value="年" />
          </el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="售价" prop="standardPrice"><el-input-number v-model="form.standardPrice" :min="0" :step="100" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="成本价" prop="costPrice"><el-input-number v-model="form.costPrice" :min="0" :step="100" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="规格" prop="specifications"><el-input v-model="form.specifications" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio :value="1">上架</el-radio><el-radio :value="0">下架</el-radio></el-radio-group></el-form-item>
        <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import { FormDialog } from '@/components/common'
import { getProductPage, createProduct, updateProduct, deleteProduct, getProductCategories } from '@/api/modules/sales'

interface Product {
  id: number; name: string; categoryId?: number; categoryName?: string;
  unit: string; standardPrice: number; costPrice?: number;
  description?: string; specifications?: string; status: number;
  createdAt?: string;
}

interface CategoryNode {
  id: number; name: string; children?: CategoryNode[];
}

const loading = ref(false)
const productList = ref<Product[]>([])
const total = ref(0)
const showSearch = ref(true)
const dialogVisible = ref(false)
const editingRow = ref<Product | null>(null)
const categories = ref<CategoryNode[]>([])

const flatCategories = computed(() => {
  const flat: { id: number; name: string }[] = []
  function walk(list: CategoryNode[]) {
    for (const c of list) {
      flat.push({ id: c.id, name: c.name })
      if (c.children) walk(c.children)
    }
  }
  walk(categories.value)
  return flat
})

const queryParams = reactive({
  page: 1, size: 20, keywords: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
})

const dialogTitle = computed(() => editingRow.value ? '编辑产品' : '新增产品')

function handleSearch() { queryParams.page = 1; fetchData() }
function handleReset() { Object.assign(queryParams, { keywords: '', categoryId: undefined, status: undefined, page: 1 }); fetchData() }
function openDialog(row?: Product) {
  editingRow.value = row || null
  dialogVisible.value = true
}
async function handleSubmit(formData: any, done: () => void) {
  try {
    if (editingRow.value) {
      await updateProduct(editingRow.value.id, formData)
      ElMessage.success('修改成功')
    } else {
      await createProduct(formData)
      ElMessage.success('新增成功')
    }
    done()
    fetchData()
  } catch {
    ElMessage.error(editingRow.value ? '修改失败' : '新增失败')
  }
}
async function handleDelete(row: Product) {
  try {
    await deleteProduct(row.id)
    ElMessage.success(`已删除产品「${row.name}」`)
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getProductPage({ ...queryParams })
    productList.value = (res.data?.records || []) as Product[]
    total.value = Number(res.data?.total ?? 0)
  } finally { loading.value = false }
}

onMounted(() => {
  fetchData()
  getProductCategories().then(res => { categories.value = (res.data || []) as CategoryNode[] })
})
</script>

<style scoped lang="scss">
.sales-page { max-width: 1400px; }
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
.pagination-wrap { display:flex; justify-content:flex-end; padding-top:16px; }
</style>
