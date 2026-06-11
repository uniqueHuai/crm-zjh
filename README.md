# CRM-ZJH

> 一站式客户关系管理系统，集成智能客服、小程序商城、协同办公与数据分析。

## 📋 项目概述

CRM-ZJH 是一款面向中小企业的全功能客户关系管理系统，覆盖 **客户管理、销售管理、智能客服、小程序商城、协同办公、数据分析** 六大核心模块，助力企业高效管理客户全生命周期。

## 🏗️ 技术栈

### 后端
| 技术 | 说明 |
|------|------|
| **Java 17** + **Spring Boot 3.2** | 核心框架 |
| **Spring Security** + **JWT** | 认证授权 |
| **MyBatis-Plus** | ORM 框架 |
| **PostgreSQL** + **pgvector** | 数据库与向量检索 |
| **Redis** (Redisson) | 缓存与分布式锁 |
| **MinIO** | 对象存储 |
| **SiliconFlow API** | Embedding 向量化 |

### 前端
| 技术 | 说明 |
|------|------|
| **Vue 3** + **TypeScript** | 前端框架 |
| **Element Plus** | UI 组件库 |
| **Vite** | 构建工具 |
| **Pinia** | 状态管理 |
| **ECharts** | 数据可视化 |

### 小程序
| 技术 | 说明 |
|------|------|
| **微信小程序原生** | 商城客户端 |
| **JWT** | 小程序端认证 |
| **pgvector** | 向量相似度搜索 |

## 🧩 功能模块

### 客户管理
- 客户信息管理（自定义字段 + 扩展字段）
- 客户标签系统（手动/自动打标）
- 客户分群与细分
- 线索管理（导入/分配/跟进）
- 公私海池机制
- 跟进记录（电话/拜访/消息）

### 销售管理
- 销售漏斗 / 管道视图
- 商机管理
- 报价单管理
- 合同管理
- 预约管理
- 产品/服务目录

### 智能客服 AI
- **智能管家** — 自然语言查询客户/销售数据
- **销售助手** — 销售话术推荐、跟进提醒
- **知识库管理** — 文档导入（TXT/MD/Word/PDF）→ 向量化 → 语义检索
- 支持多轮对话、上下文记忆

### 小程序商城
- 商品浏览与分类
- 购物车管理
- 订单管理与支付
- 优惠券（领取/使用）
- 秒杀与拼团活动
- 分销裂变体系
- 收货地址管理

### 协同办公
- 工单系统（服务工单）
- 审批流程
- 消息通知

### 系统管理
- 用户 / 角色 / 菜单 / 权限管理
- 部门管理
- 字典管理
- API Key 管理
- 操作日志审计
- 系统配置

### 数据分析
- 客户数据看板
- 销售数据统计
- 数据报表导出

## 🚀 快速开始

### 环境要求
- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 14+**（需安装 pgvector 扩展）
- **Redis 6+**
- **MinIO**（可选，用于文件存储）

### 1. 克隆项目

```bash
git clone https://github.com/uniqueHuai/crm-zjh.git
cd crm-zjh
```

### 2. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE crm_db;

-- 创建 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 导入初始化脚本
\i database/init.sql
\i database/init-mp.sql
```

### 3. 配置后端

编辑 `crm-parent/crm-admin/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/crm_db
    username: postgres
    password: your_password

redis:
  host: localhost
  port: 6379

ai:
  api-key: your_siliconflow_api_key   # Embedding 服务
  base-url: https://api.siliconflow.cn/v1
```

### 4. 启动后端

```bash
cd crm-parent/crm-admin
mvn spring-boot:run -Dspring-boot.run.main-class=com.crm.admin.CrmApplication
```

后端运行在 `http://localhost:8080/api/v1`

### 5. 启动前端

```bash
cd crm-web
npm install
npm run dev
```

前端运行在 `http://localhost:3000`

## 🧪 测试

```bash
# 后端测试
cd crm-parent
mvn test

# 前端测试
cd crm-web
npm run test

# 小程序端 API 集成测试（需前后端都已启动）
node mini-program-test.cjs
```

## 📁 项目结构

```
crm-zjh/
├── crm-parent/              # 后端 Maven 父工程
│   ├── crm-common/          # 公共组件
│   ├── crm-framework/       # 核心框架（安全、配置、异常处理）
│   ├── crm-system/          # 系统管理模块
│   ├── crm-customer/        # 客户管理模块
│   ├── crm-sales/           # 销售管理模块
│   ├── crm-mall/            # 小程序商城模块
│   ├── crm-collaboration/   # 协同办公模块
│   ├── crm-report/          # 数据分析模块
│   ├── crm-ai/              # AI 智能模块
│   └── crm-admin/           # 启动入口
├── crm-web/                 # 前端 Vue3 项目
├── mp-mall/                 # 微信小程序商城
├── database/                # 数据库初始化脚本
│   ├── init.sql             # 主库脚本
│   └── init-mp.sql          # 商城补充脚本
└── test-reports/            # 测试报告输出
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。

## 📄 许可

[MIT](LICENSE)
