-- ⚠️ 执行顺序: create.sql → seed-system.sql → seed-mock.sql
--
-- 第十部分：业务数据初始化（种子数据）
-- ============================================================================

-- 10.1 客户示例数据（owner_id=1 为 admin 用户）
INSERT INTO crm_customer (id, name, phone, company, position, province, city, source_channel, level_id, owner_id, last_contact_at, total_consumption, order_count, created_by, created_at) VALUES
(1, '华为技术有限公司', '13800138001', '华为技术有限公司', '企业客户', '广东省', '深圳市', 'offline', 3, 1, NOW() - INTERVAL '2 days', 158000.00, 12, 1, NOW() - INTERVAL '5 days'),
(2, '阿里巴巴集团', '13800138002', '阿里巴巴集团', '企业客户', '浙江省', '杭州市', 'referral', 3, 1, NOW() - INTERVAL '5 days', 220000.00, 18, 1, NOW() - INTERVAL '3 days'),
(3, '腾讯科技有限公司', '13800138003', '腾讯科技有限公司', '企业客户', '广东省', '深圳市', 'offline', 2, 1, NOW() - INTERVAL '30 days', 45000.00, 5, 1, NOW() - INTERVAL '60 days'),
(4, '字节跳动', '13800138004', '字节跳动', '企业客户', '北京市', '北京市', 'ad_landing', 2, 1, NOW() - INTERVAL '15 days', 32000.00, 3, 1, NOW() - INTERVAL '10 days'),
(5, '小米科技有限责任公司', '13800138005', '小米科技有限责任公司', '企业客户', '北京市', '北京市', 'referral', 1, 1, NOW() - INTERVAL '60 days', 8500.00, 2, 1, NOW() - INTERVAL '140 days'),
(6, '比亚迪股份有限公司', '13800138006', '比亚迪股份有限公司', '企业客户', '广东省', '深圳市', 'offline', 2, 1, NOW() - INTERVAL '7 days', 28000.00, 4, 1, NOW() - INTERVAL '130 days'),
(7, '京东集团', '13800138007', '京东集团', '企业客户', '北京市', '北京市', 'ad_landing', 1, 1, NOW() - INTERVAL '45 days', 5600.00, 1, 1, NOW() - INTERVAL '120 days'),
(8, '网易集团', '13800138008', '网易集团', '企业客户', '广东省', '广州市', 'referral', 4, 1, NOW() - INTERVAL '3 days', 350000.00, 25, 1, NOW() - INTERVAL '110 days');

-- 10.2 联系人示例数据
INSERT INTO crm_contact (customer_id, name, phone, email, position, department, is_decision_maker, is_primary, birthday, remark, created_by, created_at) VALUES
(1, '王建国', '138****8001', 'wangjg@huawei.com', 'CTO', '技术部', TRUE, TRUE, '1985-06-15', '技术决策者，对数据安全要求高', 1, NOW()),
(1, '陈静', '134****8006', 'chenj@huawei.com', '财务总监', '财务部', TRUE, FALSE, '1980-12-05', '预算审批关键人', 1, NOW()),
(2, '李明芳', '139****8002', 'limf@alibaba.com', '采购总监', '采购部', TRUE, TRUE, '1982-03-22', '负责IT类采购', 1, NOW()),
(2, '孙鹏', '133****8007', 'sunp@alibaba.com', '项目经理', '项目部', FALSE, FALSE, '1991-05-18', '项目执行层面对接', 1, NOW()),
(3, '张伟', '137****8003', 'zhangw@tencent.com', '技术经理', '技术部', FALSE, TRUE, '1990-11-08', '技术对接人', 1, NOW()),
(3, '周婷', '132****8008', 'zhout@tencent.com', '市场总监', '市场部', TRUE, FALSE, '1986-08-25', '市场合作决策人', 1, NOW()),
(4, '赵晓雪', '136****8004', 'zhaoxx@bytedance.com', '运营总监', '运营部', TRUE, TRUE, '1988-07-30', NULL, 1, NOW()),
(5, '刘洋', '135****8005', 'liuy@xiaomi.com', '产品经理', '产品部', FALSE, TRUE, '1992-09-12', '产品需求对接', 1, NOW()),
(6, '张明', '137****8010', 'zhangm@byd.com', 'IT总监', '信息部', TRUE, TRUE, '1983-04-20', '信息化建设负责人', 1, NOW()),
(7, '李华', '138****8011', 'lihua@jd.com', '运营经理', '运营部', FALSE, TRUE, '1993-11-01', NULL, 1, NOW()),
(8, '王芳', '139****8012', 'wangf@163.com', '副总裁', '总裁办', TRUE, TRUE, '1979-08-15', '高层决策人', 1, NOW());

-- 10.3 标签示例数据
INSERT INTO crm_tag (id, name, color, type, status, created_by, created_at) VALUES
(1, '高活跃', '#1890ff', 'auto', 1, 1, NOW()),
(2, '沉睡客户', '#faad14', 'auto', 1, 1, NOW()),
(3, '价格敏感', '#ff4d4f', 'auto', 1, 1, NOW()),
(4, '大客户', '#722ed1', 'auto', 1, 1, NOW()),
(5, 'VIP', '#eb2f96', 'manual', 1, 1, NOW()),
(6, '意向强烈', '#52c41a', 'manual', 1, 1, NOW()),
(7, '需要跟进', '#fa8c16', 'manual', 1, 1, NOW()),
(8, '已合作', '#13c2c2', 'manual', 1, 1, NOW());

-- 10.4 客户-标签关系示例
INSERT INTO crm_customer_tag (customer_id, tag_id, tag_type, created_by) VALUES
(1, 1, 'auto', 1), (1, 4, 'auto', 1), (1, 8, 'manual', 1),
(2, 1, 'auto', 1), (2, 5, 'manual', 1), (2, 8, 'manual', 1),
(3, 1, 'auto', 1), (3, 8, 'manual', 1),
(4, 6, 'manual', 1), (4, 7, 'manual', 1),
(5, 2, 'auto', 1), (5, 7, 'manual', 1),
(6, 1, 'auto', 1), (6, 8, 'manual', 1),
(7, 7, 'manual', 1),
(8, 1, 'auto', 1), (8, 4, 'auto', 1), (8, 5, 'manual', 1);

-- 10.5 客户分群示例数据
INSERT INTO crm_segment (id, name, conditions, is_dynamic, member_count, status, created_by, created_at) VALUES
(1, '高价值客户', '{"logic":"and","rules":[{"field":"total_consumption","operator":"gte","value":100000}]}', TRUE, 2, 1, 1, NOW()),
(2, '沉睡客户唤醒', '{"logic":"and","rules":[{"field":"last_contact_at","operator":"lte","value":-90},{"field":"total_consumption","operator":"gte","value":10000}]}', TRUE, 1, 1, 1, NOW()),
(3, '待跟进客户', '{"logic":"and","rules":[{"field":"last_contact_at","operator":"lte","value":-7}]}', TRUE, 3, 1, 1, NOW()),
(4, '本月新注册', '{"logic":"and","rules":[{"field":"created_at","operator":"gte","value":"first_day_of_month"}]}', TRUE, 0, 0, 1, NOW()),
(5, '深圳区域客户', '{"logic":"and","rules":[{"field":"province","operator":"eq","value":"广东省"},{"field":"city","operator":"eq","value":"深圳市"}]}', TRUE, 3, 1, 1, NOW());

-- 10.6 分群成员示例
INSERT INTO crm_segment_member (segment_id, customer_id, join_type) VALUES
(1, 1, 'auto'), (1, 8, 'auto'),
(2, 5, 'auto'),
(3, 3, 'auto'), (3, 5, 'auto'), (3, 7, 'auto'),
(5, 1, 'auto'), (5, 3, 'auto'), (5, 6, 'auto');

-- 10.7 线索示例数据
INSERT INTO crm_lead (id, name, phone, company, position, province, city, industry, source_channel, status, owner_id, remark, created_by, created_at) VALUES
(1, '中兴通讯', '13900010001', '中兴通讯股份有限公司', 'IT总监', '广东省', '深圳市', '通信', 'ad_landing', 'pending', 1, '官网留言咨询CRM系统', 1, NOW() - INTERVAL '3 days'),
(2, '美团', '13900010002', '北京三快在线科技有限公司', '技术VP', '北京市', '北京市', '互联网', 'referral', 'following', 1, '朋友推荐，需尽快联系', 1, NOW() - INTERVAL '7 days'),
(3, '中国平安', '13900010003', '中国平安保险集团', '采购经理', '广东省', '深圳市', '金融', 'mini_program', 'pending', 1, '小程序提交咨询', 1, NOW() - INTERVAL '1 days'),
(4, '格力电器', '13900010004', '珠海格力电器股份有限公司', '信息部长', '广东省', '珠海市', '制造', 'offline', 'following', 1, '行业展会收集的名片', 1, NOW() - INTERVAL '14 days'),
(5, '科大讯飞', '13900010005', '科大讯飞股份有限公司', '运营总监', '安徽省', '合肥市', 'AI', 'manual_input', 'following', 1, '主动来电咨询', 1, NOW() - INTERVAL '10 days'),
(6, '滴滴出行', '13900010006', '滴滴出行科技有限公司', 'CTO', '北京市', '北京市', '互联网', 'ad_landing', 'pending', 1, NULL, 1, NOW() - INTERVAL '2 days');

-- 10.8 商机示例数据
INSERT INTO crm_opportunity (id, customer_id, name, expected_amount, expected_close_date, stage_id, owner_id, pain_points, requirements, created_by, created_at) VALUES
(1, 1, '华为-CRM企业版采购', 150000.00, NOW() + INTERVAL '60 days', 3, 1, '现有系统无法满足销售管理需求，数据孤岛严重', '需要与企业微信集成，支持定制化报表', 1, NOW() - INTERVAL '30 days'),
(2, 2, '阿里-CRM标准版续费升级', 50000.00, NOW() + INTERVAL '30 days', 4, 1, '标准版功能不够用，需增加高级分析能力', '升级至企业版，增加API接口', 1, NOW() - INTERVAL '20 days'),
(3, 6, '比亚迪-实施服务项目', 90000.00, NOW() + INTERVAL '45 days', 2, 1, '新系统上线需要专业实施团队支持', '全流程实施+培训服务', 1, NOW() - INTERVAL '15 days'),
(4, 4, '字节跳动-产品咨询', 30000.00, NOW() + INTERVAL '90 days', 1, 1, '团队快速扩张，急需规范化客户管理', '先进行POC测试验证', 1, NOW() - INTERVAL '5 days');

-- 10.9 跟进记录示例数据
INSERT INTO crm_follow_up (id, customer_id, type, content, next_plan, next_plan_date, is_important, creator_id, created_by, created_at) VALUES
(1, 1, 'visit', '拜访华为CTO王建国，演示CRM企业版功能，客户对数据安全模块很感兴趣', '准备安全架构方案，下周二次演示', NOW() + INTERVAL '7 days', TRUE, 1, 1, NOW() - INTERVAL '10 days'),
(2, 1, 'call', '电话沟通了解具体需求，确认需要对接企业微信', '发送需求确认函', NOW() + INTERVAL '2 days', FALSE, 1, 1, NOW() - INTERVAL '3 days'),
(3, 2, 'visit', '拜访阿里采购总监李明芳，沟通续费升级方案', '准备对比方案', NOW() + INTERVAL '5 days', TRUE, 1, 1, NOW() - INTERVAL '8 days'),
(4, 3, 'online', '腾讯张伟在线咨询产品技术细节', '发送技术白皮书', NOW() + INTERVAL '3 days', FALSE, 1, 1, NOW() - INTERVAL '15 days'),
(5, 6, 'visit', '拜访比亚迪IT总监，现场调研现有系统使用情况', '输出调研报告', NOW() + INTERVAL '14 days', TRUE, 1, 1, NOW() - INTERVAL '30 days');

-- 10.10 系统消息示例数据
INSERT INTO sys_message (receiver_id, channel, title, content, biz_type, is_read, created_by, created_at) VALUES
(1, 'system', '欢迎使用CRM系统', '感谢您使用CRM系统，请及时完善个人资料。', 'system', TRUE, 1, NOW() - INTERVAL '180 days'),
(1, 'system', '审批待办提醒', '您有一条报销审批待处理，请前往审批中心处理。', 'approval', FALSE, 1, NOW() - INTERVAL '2 days'),
(1, 'system', '合同到期提醒', '合同「CRM企业版维护合同」将于30天后到期，请及时处理续签。', 'system', FALSE, 1, NOW() - INTERVAL '1 days'),
(1, 'system', '商机更新提醒', '商机「华为-CRM企业版采购」已进入方案报价阶段。', 'system', TRUE, 1, NOW() - INTERVAL '7 days'),
(1, 'system', '系统升级通知', '系统将于本周六凌晨2:00-4:00进行版本升级，届时将暂停服务。', 'system', FALSE, 1, NOW()),
(1, 'system', '客户生日提醒', '客户「小米科技」张国强的生日即将到来，建议发送祝福邮件。', 'system', FALSE, 1, NOW() + INTERVAL '28 days');

-- 10.11 系统公告示例数据
INSERT INTO sys_notice (id, title, content, notice_type, status, publish_at, created_by, created_at) VALUES
(1, 'CRM系统正式上线通知', '<p>各位同事，CRM系统已完成开发和测试，正式上线运行。如有问题请联系系统管理员。</p><p><b>主要功能：</b></p><ul><li>客户管理：线索、客户、联系人统一管理</li><li>销售管理：商机看板、跟进记录、报价合同</li><li>商城管理：商品、订单、优惠券</li><li>协同办公：审批管理、服务工单</li></ul>', 1, 1, NOW() - INTERVAL '180 days', 1, NOW() - INTERVAL '180 days'),
(2, '2024年Q2销售目标公告', '<p>Q2销售目标已下达，请各位销售经理查收。本季度重点开拓华南和华东市场。</p><p>整体目标：签约金额800万，回款600万。</p>', 1, 1, NOW() - INTERVAL '90 days', 1, NOW() - INTERVAL '90 days'),
(3, '关于启用审批流程的通知', '<p>自下周一起，所有报价单和合同审批需通过线上审批流程提交，纸质审批单将不再受理。</p>', 2, 1, NOW() - INTERVAL '30 days', 1, NOW() - INTERVAL '30 days');



-- 10.13 审批实例示例数据
INSERT INTO coll_approval_instance (id, define_id, biz_type, biz_id, form_data, status, applicant_id, applicant_name, created_by, created_at) VALUES
(1, 3, 'expense', 1, '{"title":"Q2差旅报销","amount":12800.00,"remark":"Q2拜访客户差旅费用","reason":"Q2出差拜访深圳客户产生的交通及住宿费"}', 'approved', 1, '系统管理员', 1, NOW() - INTERVAL '20 days'),
(2, 3, 'expense', 2, '{"title":"采购办公设备","amount":5600.00,"remark":"申请采购办公桌椅","reason":"部门新增员工需要补充办公设备"}', 'pending', 1, '系统管理员', 1, NOW() - INTERVAL '3 days'),
(3, 3, 'expense', 3, '{"title":"市场活动费用","amount":25000.00,"remark":"Q2行业展会参展费用","reason":"参加深圳电子展的展位费及物料费"}', 'pending', 1, '系统管理员', 1, NOW() - INTERVAL '1 days'),
(4, 1, 'quotation', 1, '{"title":"华为企业版报价","amount":150000.00,"remark":"含首年实施服务费"}', 'approved', 1, '系统管理员', 1, NOW() - INTERVAL '15 days');

-- 10.14 服务工单示例数据
INSERT INTO coll_service_ticket (id, ticket_no, customer_id, type, title, description, priority, source, assignee_id, status, created_by, created_at) VALUES
(1, 'TK-20250001', 1, 'install', 'CRM系统安装部署', '华为采购的CRM企业版需要上门安装部署，请安排工程师对接。', 'urgent', 'manual', 1, 'in_progress', 1, NOW() - INTERVAL '5 days'),
(2, 'TK-20250002', 2, 'complaint', '系统响应速度慢', '近期系统操作响应缓慢，页面加载时间较长，影响工作效率。', 'high', 'phone', 1, 'assigned', 1, NOW() - INTERVAL '2 days'),
(3, 'TK-20250003', 3, 'repair', '数据导出功能异常', '导出Excel时部分数据丢失，请求尽快修复。', 'urgent', 'wechat', NULL, 'pending', 1, NOW() - INTERVAL '1 days'),
(4, 'TK-20250004', 4, 'other', '新增用户权限配置', '需要为三个新员工配置CRM系统权限，附上权限清单。', 'low', 'manual', 1, 'completed', 1, NOW() - INTERVAL '10 days'),
(5, 'TK-20250005', 6, 'install', '服务器部署支持', '新采购的服务器需要部署CRM系统及数据库环境。', 'medium', 'manual', NULL, 'pending', 1, NOW());

-- 10.15 商城商品SKU示例数据
INSERT INTO mall_sku (product_id, specs, price, stock, sku_code, status, created_by, created_at) VALUES
(1, '[{"key":"版本","value":"标准版"},{"key":"年限","value":"1年"}]', 50000.00, 100, 'CRM-STD-1Y', 1, 1, NOW()),
(2, '[{"key":"版本","value":"企业版"},{"key":"年限","value":"1年"}]', 150000.00, 50, 'CRM-ENT-1Y', 1, 1, NOW()),
(4, '[{"key":"服务类型","value":"实施服务"},{"key":"天数","value":"1天"}]', 3000.00, 999, 'SVC-IMP-1D', 1, 1, NOW()),
(5, '[{"key":"服务类型","value":"培训服务"},{"key":"天数","value":"1天"}]', 2000.00, 999, 'SVC-TRN-1D', 1, 1, NOW());

-- 10.16 跟进日程示例
INSERT INTO crm_appointment (customer_id, title, description, appointment_date, start_time, end_time, location, type, status, owner_id, created_by, created_at) VALUES
(1, '华为-方案演示', '向华为CTO演示企业版安全方案', CURRENT_DATE + 3, '10:00:00', '11:30:00', '深圳市南山区华为基地', 'visit', 'pending', 1, 1, NOW()),
(2, '阿里-合同谈判', '续费合同条款沟通', CURRENT_DATE + 5, '14:00:00', '16:00:00', '杭州市余杭区阿里巴巴西溪园区', 'visit', 'pending', 1, 1, NOW()),
(6, '比亚迪-调研汇报', '提交实施调研报告', CURRENT_DATE + 7, '09:30:00', '11:00:00', '深圳市坪山区比亚迪总部', 'visit', 'pending', 1, 1, NOW()),
(4, '阿里-线上沟通', '确认升级需求细节', CURRENT_DATE + 1, '15:00:00', '15:30:00', NULL, 'call', 'pending', 1, 1, NOW());

-- 10.17 看板布局示例
INSERT INTO report_dashboard_layout (user_id, layout) VALUES
(1, '[{"cardType":"stats","position":1,"width":12,"height":1},{"cardType":"sales_funnel","position":2,"width":6,"height":2},{"cardType":"customer_analysis","position":3,"width":6,"height":2},{"cardType":"lead_trend","position":4,"width":12,"height":2},{"cardType":"recent_follow_up","position":5,"width":6,"height":2},{"cardType":"top_performers","position":6,"width":6,"height":2}]');

-- 10.18 商城初始商品分类数据（小程序商城）
INSERT INTO sale_product_category (id, name, parent_id, icon, sort_order, status, created_by, created_at) VALUES
(1, '软件产品', NULL, '/icons/software.png', 1, 1, 1, NOW()),
(2, '硬件设备', NULL, '/icons/hardware.png', 2, 1, 1, NOW()),
(3, '技术服务', NULL, '/icons/service.png', 3, 1, 1, NOW()),
(11, 'CRM系统', 1, NULL, 1, 1, 1, NOW()),
(12, '办公软件', 1, NULL, 2, 1, 1, NOW()),
(21, '服务器', 2, NULL, 1, 1, 1, NOW()),
(22, '网络设备', 2, NULL, 2, 1, 1, NOW()),
(31, '实施服务', 3, NULL, 1, 1, 1, NOW()),
(32, '培训服务', 3, NULL, 2, 1, 1, NOW());

-- 10.19 商城初始商品数据（小程序商城）
INSERT INTO sale_product (id, category_id, name, description, cover_image, standard_price, unit, sort_order, status, created_by, created_at) VALUES
(1, 11, 'CRM标准版', '标准版CRM系统，适合中小团队', '/images/crm-std.png', 50000.00, '套', 1, 1, 1, NOW()),
(2, 11, 'CRM企业版', '企业版CRM系统，适合大型组织', '/images/crm-ent.png', 150000.00, '套', 2, 1, 1, NOW()),
(3, 31, '实施服务', 'CRM系统上门实施部署服务', '/images/impl.png', 3000.00, '人/天', 3, 1, 1, NOW()),
(4, 32, '培训服务', 'CRM系统使用培训服务', '/images/train.png', 2000.00, '人/天', 4, 1, 1, NOW());

-- ====================================================================
-- 完成
-- ====================================================================