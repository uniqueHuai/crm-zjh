<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">0</div>
            <div class="stat-label">今日新增客户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">0</div>
            <div class="stat-label">跟进中商机</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">0</div>
            <div class="stat-label">待办事项</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">0</div>
            <div class="stat-label">本月成交额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>今日跟进提醒</span>
          </template>
          <el-empty description="暂无待办事项" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-links">
            <el-button type="primary" @click="$router.push('/customer/lead')">新增线索</el-button>
            <el-button type="success" @click="$router.push('/customer/list')">客户列表</el-button>
            <el-button type="warning" @click="$router.push('/sales/pipeline')">商机看板</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    text-align: center;
    padding: 8px 0;
    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;
    }
    .stat-label {
      font-size: 14px;
      color: #666;
      margin-top: 8px;
    }
  }
  .quick-links {
    display: flex;
    flex-direction: column;
    gap: 12px;
    .el-button {
      width: 100%;
    }
  }
}
</style>
