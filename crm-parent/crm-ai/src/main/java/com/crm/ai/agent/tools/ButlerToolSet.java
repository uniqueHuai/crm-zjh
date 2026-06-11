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
public class ButlerToolSet {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ReportExportService exportService;

    public List<ToolDefinition> getToolDefinitions() {
        return List.of(
                // === 数据查询工具 ===
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_company_overview")
                                .description("公司经营概览：客户数、商机数、合同金额、回款等核心指标")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "timeRange", ToolDefinition.ParameterProperty.builder().type("string").description("时间范围: month/quarter/year").build()
                                        ))
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_sales_trend")
                                .description("销售趋势分析，按日/周/月维度返回合同金额趋势")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "timeRange", ToolDefinition.ParameterProperty.builder().type("string").description("时间范围: month/quarter/year").build(),
                                                "granularity", ToolDefinition.ParameterProperty.builder().type("string").description("粒度: day/week/month").build()
                                        ))
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_department_performance")
                                .description("各部门业绩排行和分析")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "timeRange", ToolDefinition.ParameterProperty.builder().type("string").description("时间范围: month/quarter/year").build()
                                        ))
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_customer_structure")
                                .description("客户结构分析：按等级/行业/地域分布统计")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("get_product_analysis")
                                .description("产品线分析：各产品的销量、合同金额排行")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build(),
                // === 报表导出工具 ===
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_company_report")
                                .description("导出公司经营概览报表为 Excel")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_sales_trend_report")
                                .description("导出销售趋势分析报表为 Excel")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of(
                                                "granularity", ToolDefinition.ParameterProperty.builder().type("string").description("粒度: day/week/month，默认month").build()
                                        ))
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_department_report")
                                .description("导出各部门业绩排行报表为 Excel")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build(),
                ToolDefinition.builder().type("function")
                        .function(ToolDefinition.Function.builder()
                                .name("export_customer_structure_report")
                                .description("导出客户结构分析报表为 Excel（含等级分布和地域分布两个工作表）")
                                .parameters(ToolDefinition.Parameters.builder()
                                        .type("object")
                                        .properties(Map.of())
                                        .build())
                                .build())
                        .build()
        );
    }

    // ========== 数据查询工具 ==========

    public String getCompanyOverview(Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();

        Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crm_customer WHERE deleted_at IS NULL", Integer.class);
        result.put("total_customers", customerCount);

        Integer oppCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crm_opportunity WHERE deleted_at IS NULL " +
                "AND stage_id IN (SELECT id FROM crm_opportunity_stage WHERE category = 'open')",
                Integer.class);
        result.put("active_opportunities", oppCount);

        Map<String, Object> contractStats = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) AS total_count, COALESCE(SUM(total_amount), 0) AS total_amount " +
                "FROM crm_contract WHERE deleted_at IS NULL AND status IN ('signed', 'pending_sign')");
        result.putAll(contractStats);

        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE deleted_at IS NULL", Integer.class);
        result.put("total_users", userCount);

        try { return objectMapper.writeValueAsString(result); } catch (Exception e) { return "{}"; }
    }

    public String getSalesTrend(Map<String, Object> args) {
        String granularity = args.getOrDefault("granularity", "month").toString();
        String interval;
        switch (granularity) {
            case "day" -> interval = "yyyy-MM-dd";
            case "week" -> interval = "yyyy-WW";
            default -> interval = "yyyy-MM";
        }

        List<Map<String, Object>> trend = jdbcTemplate.queryForList(
                "SELECT TO_CHAR(created_at, ?) AS period, " +
                "COUNT(*) AS contract_count, COALESCE(SUM(total_amount), 0) AS total_amount " +
                "FROM crm_contract WHERE deleted_at IS NULL AND status IN ('signed', 'pending_sign') " +
                "GROUP BY period ORDER BY period DESC LIMIT 12", interval);

        try { return objectMapper.writeValueAsString(trend); } catch (Exception e) { return "[]"; }
    }

    public String getDepartmentPerformance(Map<String, Object> args) {
        List<Map<String, Object>> deptStats = jdbcTemplate.queryForList(
                "SELECT d.id, d.name AS dept_name, " +
                "COUNT(DISTINCT u.id) AS user_count, " +
                "COUNT(DISTINCT c.id) AS customer_count, " +
                "COUNT(DISTINCT o.id) AS opportunity_count " +
                "FROM sys_dept d " +
                "LEFT JOIN sys_user u ON u.dept_id = d.id AND u.deleted_at IS NULL " +
                "LEFT JOIN crm_customer c ON c.owner_id = u.id AND c.deleted_at IS NULL " +
                "LEFT JOIN crm_opportunity o ON o.owner_id = u.id AND o.deleted_at IS NULL " +
                "WHERE d.deleted_at IS NULL AND d.status = 1 " +
                "GROUP BY d.id, d.name ORDER BY customer_count DESC");

        try { return objectMapper.writeValueAsString(deptStats); } catch (Exception e) { return "[]"; }
    }

    public String getCustomerStructure() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> levelDist = jdbcTemplate.queryForList(
                "SELECT cl.name AS level_name, COUNT(c.id) AS customer_count " +
                "FROM crm_customer_level cl " +
                "LEFT JOIN crm_customer c ON c.level_id = cl.id AND c.deleted_at IS NULL " +
                "GROUP BY cl.id, cl.name ORDER BY cl.sort_order");
        result.put("by_level", levelDist);

        List<Map<String, Object>> regionDist = jdbcTemplate.queryForList(
                "SELECT COALESCE(province, '未知') AS province, COUNT(*) AS customer_count " +
                "FROM crm_customer WHERE deleted_at IS NULL " +
                "GROUP BY province ORDER BY customer_count DESC LIMIT 10");
        result.put("by_region", regionDist);

        try { return objectMapper.writeValueAsString(result); } catch (Exception e) { return "{}"; }
    }

    public String getProductAnalysis() {
        List<Map<String, Object>> products = jdbcTemplate.queryForList(
                "SELECT sp.id, sp.name, sp.standard_price, " +
                "COUNT(ci.id) AS order_count, COALESCE(SUM(ci.quantity), 0) AS total_quantity, " +
                "COALESCE(SUM(ci.subtotal), 0) AS total_amount " +
                "FROM crm_sales_product sp " +
                "LEFT JOIN crm_contract_item ci ON ci.product_name = sp.name " +
                "WHERE sp.deleted_at IS NULL AND sp.status = 1 " +
                "GROUP BY sp.id, sp.name, sp.standard_price " +
                "ORDER BY total_amount DESC");

        try { return objectMapper.writeValueAsString(products); } catch (Exception e) { return "[]"; }
    }

    // ========== 报表导出工具 ==========

    public String exportCompanyReport() {
        Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crm_customer WHERE deleted_at IS NULL", Integer.class);

        Integer oppCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM crm_opportunity WHERE deleted_at IS NULL " +
                "AND stage_id IN (SELECT id FROM crm_opportunity_stage WHERE category = 'open')", Integer.class);

        Map<String, Object> contractStats = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) AS total_count, COALESCE(SUM(total_amount), 0) AS total_amount " +
                "FROM crm_contract WHERE deleted_at IS NULL AND status IN ('signed', 'pending_sign')");

        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE deleted_at IS NULL", Integer.class);

        List<String> headers = List.of("指标", "数值");
        List<List<Object>> data = new ArrayList<>();
        data.add(List.of("客户总数", customerCount));
        data.add(List.of("活跃商机数", oppCount));
        data.add(List.of("合同总数", contractStats.get("total_count")));
        data.add(List.of("合同总金额", contractStats.get("total_amount")));
        data.add(List.of("系统用户数", userCount));

        String token = exportService.exportExcel("公司经营概览", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }

    public String exportSalesTrendReport(Map<String, Object> args) {
        String granularity = args.getOrDefault("granularity", "month").toString();
        String interval;
        switch (granularity) {
            case "day" -> interval = "yyyy-MM-dd";
            case "week" -> interval = "yyyy-WW";
            default -> interval = "yyyy-MM";
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT TO_CHAR(created_at, ?) AS period, " +
                "COUNT(*) AS contract_count, COALESCE(SUM(total_amount), 0) AS total_amount " +
                "FROM crm_contract WHERE deleted_at IS NULL AND status IN ('signed', 'pending_sign') " +
                "GROUP BY period ORDER BY period DESC LIMIT 12", interval);

        List<String> headers = List.of("期间", "合同数", "合同金额");
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            data.add(List.of(row.get("period"), row.get("contract_count"), row.get("total_amount")));
        }

        String granularityLabel = switch (granularity) { case "day" -> "日"; case "week" -> "周"; default -> "月"; };
        String token = exportService.exportExcel("销售趋势分析(" + granularityLabel + ")", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }

    public String exportDepartmentReport() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT d.name AS dept_name, " +
                "COUNT(DISTINCT u.id) AS user_count, " +
                "COUNT(DISTINCT c.id) AS customer_count, " +
                "COUNT(DISTINCT o.id) AS opportunity_count " +
                "FROM sys_dept d " +
                "LEFT JOIN sys_user u ON u.dept_id = d.id AND u.deleted_at IS NULL " +
                "LEFT JOIN crm_customer c ON c.owner_id = u.id AND c.deleted_at IS NULL " +
                "LEFT JOIN crm_opportunity o ON o.owner_id = u.id AND o.deleted_at IS NULL " +
                "WHERE d.deleted_at IS NULL AND d.status = 1 " +
                "GROUP BY d.id, d.name ORDER BY customer_count DESC");

        List<String> headers = List.of("部门名称", "人数", "客户数", "商机数");
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            data.add(List.of(row.get("dept_name"), row.get("user_count"),
                    row.get("customer_count"), row.get("opportunity_count")));
        }

        String token = exportService.exportExcel("部门业绩排行", headers, data);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }

    public String exportCustomerStructureReport() {
        List<Map<String, Object>> levelRows = jdbcTemplate.queryForList(
                "SELECT cl.name AS level_name, COUNT(c.id) AS customer_count " +
                "FROM crm_customer_level cl " +
                "LEFT JOIN crm_customer c ON c.level_id = cl.id AND c.deleted_at IS NULL " +
                "GROUP BY cl.id, cl.name ORDER BY cl.sort_order");

        List<Map<String, Object>> regionRows = jdbcTemplate.queryForList(
                "SELECT COALESCE(province, '未知') AS province, COUNT(*) AS customer_count " +
                "FROM crm_customer WHERE deleted_at IS NULL " +
                "GROUP BY province ORDER BY customer_count DESC LIMIT 10");

        List<ReportExportService.SheetData> sheets = new ArrayList<>();

        List<List<Object>> levelData = new ArrayList<>();
        for (Map<String, Object> row : levelRows) {
            levelData.add(List.of(row.get("level_name"), row.get("customer_count")));
        }
        sheets.add(new ReportExportService.SheetData("等级分布",
                List.of("客户等级", "客户数"), levelData));

        List<List<Object>> regionData = new ArrayList<>();
        for (Map<String, Object> row : regionRows) {
            regionData.add(List.of(row.get("province"), row.get("customer_count")));
        }
        sheets.add(new ReportExportService.SheetData("地域分布",
                List.of("省份", "客户数"), regionData));

        String token = exportService.exportExcelWithSheets("客户结构分析", sheets);
        return "报表已生成，请在浏览器中打开以下链接下载：\n/ai/export/" + token;
    }
}
