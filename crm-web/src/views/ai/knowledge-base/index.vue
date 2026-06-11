<template>
  <page-container title="知识库管理">
    <template #action>
      <el-button type="primary" @click="openCreateDialog">新建知识库</el-button>
    </template>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type) as any" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'info'" size="small">
            {{ row.status === 'enabled' ? '已启用' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openImportDialog(row)">导入文档</el-button>
          <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 16px; justify-content: flex-end;"
      @change="loadData"
    />

    <!-- 新建/编辑 对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑知识库' : '新建知识库'" width="500px">
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="知识库名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="常见问题" value="faq" />
            <el-option label="商品信息" value="product" />
            <el-option label="售后政策" value="policy" />
            <el-option label="操作手册" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="知识库描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入文档对话框 -->
    <el-dialog v-model="importVisible" title="导入文档" width="600px">
      <el-tabs v-model="importTab">
        <el-tab-pane label="粘贴文本" name="text">
          <el-form :model="importForm" label-width="60px">
            <el-form-item label="标题">
              <el-input v-model="importForm.title" placeholder="文档标题" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input v-model="importForm.content" type="textarea" :rows="12" placeholder="粘贴文档内容..." />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="上传文件" name="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            accept=".txt,.md,.doc,.docx,.pdf"
            @change="handleFileChange"
          >
            <el-button type="primary" plain>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 .txt、.md、.doc/.docx 和 .pdf 文件</div>
            </template>
          </el-upload>
          <el-form v-if="selectedFile" style="margin-top:16px" label-width="60px">
            <el-form-item label="标题">
              <el-input v-model="fileTitle" :placeholder="selectedFile.name" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="import-hint">系统会自动将文档分块并向量化，用于智能客服问答检索。</div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">
          {{ importing ? '导入中...' : '导入并向量化' }}
        </el-button>
      </template>
    </el-dialog>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getKnowledgeBasePage,
  createKnowledgeBase,
  updateKnowledgeBase,
  deleteKnowledgeBase,
  importDocument,
  importFile as importFileApi,
} from '@/api/modules/ai'
import type { KnowledgeBase } from '@/types'
import PageContainer from '@/components/common/PageContainer.vue'

const loading = ref(false)
const list = ref<KnowledgeBase[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keywords: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const form = reactive({ id: 0, name: '', type: 'faq', description: '', status: 'enabled' })
const formRef = ref<any>(null)
const rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }], type: [{ required: true, message: '请选择类型', trigger: 'change' }] }

const importVisible = ref(false)
const importing = ref(false)
const importTab = ref('text')
const importForm = reactive({ title: '', content: '' })
const currentKbId = ref(0)
const selectedFile = ref<File | null>(null)
const fileTitle = ref('')

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const res = await getKnowledgeBasePage({ page: query.page, size: query.size, keywords: query.keywords || undefined })
    list.value = res.data?.records || []
    total.value = Number(res.data?.total ?? 0)
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function typeTag(type: string) {
  const map: Record<string, string> = { faq: 'primary', product: 'success', policy: 'warning', manual: 'info' }
  return map[type] || 'default'
}
function typeLabel(type: string) {
  const map: Record<string, string> = { faq: '常见问题', product: '商品信息', policy: '售后政策', manual: '操作手册' }
  return map[type] || type
}

function openCreateDialog() {
  isEdit.value = false
  form.id = 0; form.name = ''; form.type = 'faq'; form.description = ''
  dialogVisible.value = true
}

function openEditDialog(row: KnowledgeBase) {
  isEdit.value = true
  form.id = row.id; form.name = row.name; form.type = row.type; form.description = row.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateKnowledgeBase({ id: form.id, name: form.name, type: form.type, description: form.description })
      ElMessage.success('更新成功')
    } else {
      await createKnowledgeBase({ name: form.name, type: form.type, description: form.description })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* ignore */ }
  finally { submitting.value = false }
}

async function handleDelete(row: KnowledgeBase) {
  try {
    await ElMessageBox.confirm(`确定删除知识库「${row.name}」吗？`, '确认')
    await deleteKnowledgeBase(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* cancelled */ }
}

function openImportDialog(row: KnowledgeBase) {
  currentKbId.value = row.id
  importForm.title = ''
  importForm.content = ''
  importTab.value = 'text'
  selectedFile.value = null
  fileTitle.value = ''
  importVisible.value = true
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw
  fileTitle.value = file.name?.replace(/\.(txt|md|doc|docx|pdf)$/, '') || ''
}

async function handleImport() {
  importing.value = true
  try {
    if (importTab.value === 'file' && selectedFile.value) {
      const formData = new FormData()
      formData.append('file', selectedFile.value)
      if (fileTitle.value) formData.append('title', fileTitle.value)
      await importFileApi(currentKbId.value, formData)
    } else {
      if (!importForm.content.trim()) { ElMessage.warning('请输入文档内容'); return }
      await importDocument(currentKbId.value, importForm.title, importForm.content)
    }
    ElMessage.success('导入成功，文档已向量化')
    importVisible.value = false
  } catch { /* ignore */ }
  finally { importing.value = false }
}
</script>

<style scoped>
.import-hint {
  font-size: 12px;
  color: var(--crm-text-secondary);
  padding: 0 20px 12px;
}
</style>
