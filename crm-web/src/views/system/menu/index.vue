<template>
  <div class="page-container">
    <div class="page-header">
      <h3>菜单管理</h3>
      <el-button type="primary">新增菜单</el-button>
    </div>
    <el-table :data="[]" border stripe row-key="id" default-expand-all>
      <el-table-column prop="name" label="菜单名称" />
      <el-table-column prop="icon" label="图标" />
      <el-table-column prop="menuType" label="类型" />
      <el-table-column prop="routePath" label="路由路径" />
      <el-table-column prop="permissionCode" label="权限标识" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="200" fixed="right" />
    </el-table>
  </div>
</template>
