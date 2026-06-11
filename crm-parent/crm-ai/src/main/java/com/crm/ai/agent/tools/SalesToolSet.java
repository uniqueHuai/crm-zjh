package com.crm.ai.agent.tools;

import com.crm.ai.export.service.ReportExportService;
import com.crm.ai.llm.dto.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SalesToolSet {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ReportExportService exportService;

    public List<ToolDefinition> getToolDefinitions() {
        return List.of(
                // === 数据查询工具 ===
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_my_customers")
                                .description("获取我负责的客户列表，可按状态/关键词筛选")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "page", ToolDefinition.ParameterProperty.builder().type("string").description("页码，默认1").build(),
                                                "size", ToolDefinition.ParameterProperty.builder().type("string").description("每页条数，默认20").build(),
                                                "status", ToolDefinition.ParameterProperty.builder().type("string").description("客户状态筛选").build(),
                                                "keyword", ToolDefinition.ParameterProperty.builder().type("string").description("搜索关键词（客户名/公司名）").build()
                                        ))
                                        .required(List.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_my_sales_summary")
                                .description("获取我的销售业绩汇总，包括合同金额、回款金额等")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "startDate", ToolDefinition.ParameterProperty.builder().type("string").description("开始日期 yyyy-MM-dd").build(),
                                                "endDate", ToolDefinition.ParameterProperty.builder().type("string").description("结束日期 yyyy-MM-dd").build()
                                        ))
                                        .required(List.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_product_ranking")
                                .description("获取产品销量排行")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "timeRange", ToolDefinition.ParameterProperty.builder().type("string").description("时间范围: month/quarter/year").build(),
                                                "limit", ToolDefinition.ParameterProperty.builder().type("string").description("返回条数，默认10").build()
                                        ))
                                        .required(List.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_customer_analysis")
                                .description("深度分析某个客户的信息，包括消费、联系人、商机等")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "customerId", ToolDefinition.ParameterProperty.builder().type("string").description("客户ID").build()
                                        ))
                                        .required(List.of("customerId"))
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_pipeline_analysis")
                                .description("分析我的商机管道概况，包括各阶段数量和金额")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build(),
                // === 报表导出工具 ===
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_customers_report")
                                .description("导出客户列表为 Excel 报表，可按关键词筛选")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "keyword", ToolDefinition.ParameterProperty.builder().type("string").description("搜索关键词（客户名/公司名）").build()
                                        ))
                                        .required(List.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_sales_report")
                                .description("导出销售业绩报表为 Excel")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "startDate", ToolDefinition.ParameterProperty.builder().type("string").description("开始日期 yyyy-MM-dd").build(),
                                                "endDate", ToolDefinition.ParameterProperty.builder().type("string").description("结束日期 yyyy-MM-dd").build()
                                        ))
                                        .required(List.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_product_report")
                                .description("导出产品销量排行报表为 Excel")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_pipeline_report")
                                .description("导出商机管道分析报表为 Excel")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build()
        );
    }

    // ========== 数据查询工具 ==========

    public String getMyCustomers(Long userId, Map<String, Object> args) {
        int page = args.containsKey("page") ? Integer.parseInt(args.get("page").toString()) : 1;
        int size = args.containsKey("size") ? Integer.parseInt(args.get("size").toString()) : 20;
        int offset = (page - 1) * size;
        String keyword = (String) args.get("keyword");

        String sql = "SELECT id, name, phone, company, province, city, total_consumption, order_count, " +
                "status, created_at FROM crm_customer WHERE deleted_at IS NULL AND owner_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (name ILIKE ? OR company ILIKE ?)";
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        sql += " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(size);
        params.add(offset);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, params.toArray());
        try { return objectMapper.writeValueAsString(list); } catch (Exception e) { return "[]"; }
    }

    public String getMySalesSummary(Long userId, Map<String, Object> args) {
        String startDate = (String) args.get("startDate");
        String endDate = (String) args.get("endDate");

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS contract_count, COALESCE(SUM(total_amount), 0) AS total_amount " +
                "FROM crm_contract WHERE deleted_at IS NULL AND created_by = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startDate != null) { sql.append(" AND created_at >= ?::timestamp"); params.add(startDate); }
        if (endDate != null) { sql.append(" AND created_at <= ?::timestamp"); params.add(endDate); }

        Map<String, Object> result = jdbcTemplate.queryForMap(sql.toString(), params.toArray());

        Integer oppCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crm_opportunity WHERE deleted_at IS NULL AND owner_id = ?",
                Integer.class, userId);
        result.put("opportunity_count", oppCount != null ? oppCount : 0);

        try { return objectMapper.writeValueAsString(result); } catch (Exception e) { return "{}"; }
    }

    public String getProductRanking(Map<String, Object> args) {
        int limit = args.containsKey("limit") ? Integer.parseInt(args.get("limit").toString()) : 10;

        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT sp.id, sp.name, sp.standard_price, " +
                "COALESCE(SUM(ci.quantity), 0) AS total_quantity, " +
                "COALESCE(SUM(ci.subtotal), 0) AS total_amount " +
                "FROM crm_sales_product sp " +
                "LEFT JOIN crm_contract_item ci ON ci.product_name = sp.name " +
                "WHERE sp.deleted_at IS NULL AND sp.status = 1 " +
                "GROUP BY sp.id, sp.name, sp.standard_price " +
                "ORDER BY total_amount DESC LIMIT ?", limit);

        try { return objectMapper.writeValueAsString(list); } catch (Exception e) { return "[]"; }
    }

    public String getCustomerAnalysis(Map<String, Object> args) {
        Long customerId = Long.valueOf(args.get("customerId").toString());

        Map<String, Object> customer = jdbcTemplate.queryForMap(
                "SELECT c.*, cl.name AS level_name FROM crm_customer c " +
                "LEFT JOIN crm_customer_level cl ON c.level_id = cl.id " +
                "WHERE c.id = ? AND c.deleted_at IS NULL", customerId);

        List<Map<String, Object>> opportunities = jdbcTemplate.queryForList(
                "SELECT o.id, o.name, o.expected_amount, o.expected_close_date, os.name AS stage_name " +
                "FROM crm_opportunity o LEFT JOIN crm_opportunity_stage os ON o.stage_id = os.id " +
                "WHERE o.customer_id = ? AND o.deleted_at IS NULL ORDER BY o.created_at DESC", customerId);

        List<Map<String, Object>> followUps = jdbcTemplate.queryForList(
                "SELECT id, type, content, created_at FROM crm_follow_up " +
                "WHERE customer_id = ? AND deleted_at IS NULL ORDER BY created_at DESC LIMIT 5", customerId);

        Map<String, Object> result = new HashMap<>();
        result.put("customer", customer);
        result.put("opportunities", opportunities);
        result.put("recent_follow_ups", followUps);

        try { return objectMapper.writeValueAsString(result); } catch (Exception e) { return "{}"; }
    }

    public String getPipelineAnalysis(Long userId) {
        List<Map<String, Object>> stages = jdbcTemplate.queryForList(
                "SELECT os.name AS stage_name, os.probability, " +
                "COUNT(o.id) AS opportunity_count, " +
                "COALESCE(SUM(o.expected_amount), 0) AS total_amount " +
                "FROM crm_opportunity_stage os " +
                "LEFT JOIN crm_opportunity o ON o.stage_id = os.id " +
                "  AND o.deleted_at IS NULL AND o.owner_id = ? " +
                "GROUP BY os.id, os.name, os.probability, os.sort_order " +
                "ORDER BY os.sort_order", userId);

        try { return objectMapper.writeValueAsString(stages); } catch (Exception e) { return "[]"; }
    }

    // ========== 报表导出工具 ==========

    public String exportCustomersReport(Long userId, Map<String, Object> args) {
        String keyword = (String) args.get("keyword");

        String sql = "SELECT id, name, phone, company, province, city, total_consumption, order_count, " +
                "status, created_at FROM crm_customer WHERE deleted_at IS NULL AND owner_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (name ILIKE ? OR company ILIKE ?)";
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql += " ORDER BY created_at DESC";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        List<String> headers = List.of("编号", "客户名称", "电话", "公司", "省份", "城市", "消费总额", "订单数", "状态", "创建时间");
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            data.add(List.of(
                    row.get("id"), row.get("name"), row.get("phone"), row.get("company"),
                    row.get("province"), row.get("city"), row.get("total_consumption"),
                    row.get("order_count"), row.get("status"), row.get("created_at")
            ));
        }

        String token = exportService.exportExcel("客户列表", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }

    public String exportSalesReport(Long userId, Map<String, Object> args) {
        String startDate = (String) args.get("startDate");
        String endDate = (String) args.get("endDate");

        StringBuilder sql = new StringBuilder(
                "SELECT id, name, customer_name, total_amount, status, created_at " +
                "FROM crm_contract WHERE deleted_at IS NULL AND created_by = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startDate != null) { sql.append(" AND created_at >= ?::timestamp"); params.add(startDate); }
        if (endDate != null) { sql.append(" AND created_at <= ?::timestamp"); params.add(endDate); }
        sql.append(" ORDER BY created_at DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        List<String> headers = List.of("合同编号", "合同名称", "客户名称", "合同金额", "状态", "创建时间");
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            data.add(List.of(
                    row.get("id"), row.get("name"), row.get("customer_name"),
                    row.get("total_amount"), row.get("status"), row.get("created_at")
            ));
        }

        String token = exportService.exportExcel("销售业绩报表", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }

    public String exportProductReport() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT sp.id, sp.name, sp.standard_price, " +
                "COALESCE(SUM(ci.quantity), 0) AS total_quantity, " +
                "COALESCE(SUM(ci.subtotal), 0) AS total_amount " +
                "FROM crm_sales_product sp " +
                "LEFT JOIN crm_contract_item ci ON ci.product_name = sp.name " +
                "WHERE sp.deleted_at IS NULL AND sp.status = 1 " +
                "GROUP BY sp.id, sp.name, sp.standard_price " +
                "ORDER BY total_amount DESC");

        List<String> headers = List.of("产品编号", "产品名称", "标准单价", "总销量", "总金额");
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            data.add(List.of(
                    row.get("id"), row.get("name"), row.get("standard_price"),
                    row.get("total_quantity"), row.get("total_amount")
            ));
        }

        String token = exportService.exportExcel("产品销量排行", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }

    public String exportPipelineReport(Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT os.name AS stage_name, os.probability, " +
                "COUNT(o.id) AS opportunity_count, " +
                "COALESCE(SUM(o.expected_amount), 0) AS total_amount " +
                "FROM crm_opportunity_stage os " +
                "LEFT JOIN crm_opportunity o ON o.stage_id = os.id " +
                "  AND o.deleted_at IS NULL AND o.owner_id = ? " +
                "GROUP BY os.id, os.name, os.probability, os.sort_order " +
                "ORDER BY os.sort_order", userId);

        List<String> headers = List.of("阶段名称", "胜率", "商机数", "预计金额");
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            data.add(List.of(
                    row.get("stage_name"), row.get("probability"),
                    row.get("opportunity_count"), row.get("total_amount")
            ));
        }

        String token = exportService.exportExcel("商机管道分析", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }
}
