<template>
  <div class="page-container">
    <div class="page-header">
      <h3>客户列表</h3>
      <div>
        <el-button>导入</el-button>
        <el-button>导出</el-button>
        <el-button type="primary">新增客户</el-button>
      </div>
    </div>
    <el-table :data="[]" border stripe>
      <el-table-column prop="name" label="客户名称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="levelName" label="客户等级" />
      <el-table-column prop="ownerName" label="负责人" />
      <el-table-column prop="lastContactAt" label="最后联系" />
      <el-table-column label="操作" width="200" fixed="right" />
    </el-table>
  </div>
</template>
