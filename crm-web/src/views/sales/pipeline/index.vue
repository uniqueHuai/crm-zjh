<template>
  <div class="page-container">
    <div class="page-header">
      <h3>商机看板</h3>
      <el-button type="primary">新增商机</el-button>
    </div>
    <el-row :gutter="16">
      <el-col v-for="stage in stages" :key="stage.name" :span="6">
        <el-card>
          <template #header>{{ stage.name }}</template>
          <el-empty v-if="stage.count === 0" description="暂无商机" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
const stages = [
  { name: '初步接触', count: 0 },
  { name: '需求分析', count: 0 },
  { name: '方案报价', count: 0 },
  { name: '商务谈判', count: 0 }
]
</script>
