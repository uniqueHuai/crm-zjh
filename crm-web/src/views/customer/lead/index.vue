<template>
  <div class="page-container">
    <div class="page-header">
      <h3>线索管理</h3>
      <el-button type="primary">新增线索</el-button>
    </div>
    <el-table :data="[]" border stripe>
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="sourceChannel" label="来源" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="ownerName" label="负责人" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="200" fixed="right" />
    </el-table>
  </div>
</template>
