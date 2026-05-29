<template>
  <div class="page-container">
    <div class="page-header">
      <h3>角色管理</h3>
      <el-button type="primary">新增角色</el-button>
    </div>
    <el-table :data="[]" border stripe>
      <el-table-column prop="name" label="角色名称" />
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column prop="dataScope" label="数据范围" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="250" fixed="right" />
    </el-table>
  </div>
</template>
