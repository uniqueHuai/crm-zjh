<template>
  <div class="user-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openDialog()">新增用户</el-button>
        </div>
      </template>

      <el-table :data="userList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, deleteUser, type SysUser } from '@/api/modules/user'

const loading = ref(false)
const userList = ref<SysUser[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage({ page: 1, size: 20 })
    userList.value = res.data.records
  } finally {
    loading.value = false
  }
}

function openDialog(row?: SysUser) {
  // TODO: 弹出新增/编辑对话框
  console.log('edit:', row)
}

async function handleDelete(row: SysUser) {
  try {
    await ElMessageBox.confirm('确认删除该用户？')
    await deleteUser(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
