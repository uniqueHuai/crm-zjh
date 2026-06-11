package com.crm.ai.agent.tools;

import com.crm.ai.llm.dto.ToolDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final SalesToolSet salesToolSet;
    private final ButlerToolSet butlerToolSet;
    private final ObjectMapper objectMapper;

    public String execute(String toolName, String arguments, Long userId) {
        try {
            Map<String, Object> args;
            if (arguments == null || arguments.isBlank()) {
                args = new java.util.HashMap<>();
            } else {
                args = objectMapper.readValue(arguments,
                        new TypeReference<Map<String, Object>>() {});
            }

            return switch (toolName) {
                // ===== 销售助手 - 数据查询 =====
                case "get_my_customers" -> salesToolSet.getMyCustomers(userId, args);
                case "get_my_sales_summary" -> salesToolSet.getMySalesSummary(userId, args);
                case "get_product_ranking" -> salesToolSet.getProductRanking(args);
                case "get_customer_analysis" -> salesToolSet.getCustomerAnalysis(args);
                case "get_pipeline_analysis" -> salesToolSet.getPipelineAnalysis(userId);
                // ===== 销售助手 - 报表导出 =====
                case "export_customers_report" -> salesToolSet.exportCustomersReport(userId, args);
                case "export_sales_report" -> salesToolSet.exportSalesReport(userId, args);
                case "export_product_report" -> salesToolSet.exportProductReport();
                case "export_pipeline_report" -> salesToolSet.exportPipelineReport(userId);
                // ===== 智能管家 - 数据查询 =====
                case "get_company_overview" -> butlerToolSet.getCompanyOverview(args);
                case "get_sales_trend" -> butlerToolSet.getSalesTrend(args);
                case "get_department_performance" -> butlerToolSet.getDepartmentPerformance(args);
                case "get_customer_structure" -> butlerToolSet.getCustomerStructure();
                case "get_product_analysis" -> butlerToolSet.getProductAnalysis();
                // ===== 智能管家 - 报表导出 =====
                case "export_company_report" -> butlerToolSet.exportCompanyReport();
                case "export_sales_trend_report" -> butlerToolSet.exportSalesTrendReport(args);
                case "export_department_report" -> butlerToolSet.exportDepartmentReport();
                case "export_customer_structure_report" -> butlerToolSet.exportCustomerStructureReport();
                default -> "{\"error\": \"Unknown tool: " + toolName + "\"}";
            };
        } catch (Exception e) {
            log.error("Tool execution failed: {} {}", toolName, arguments, e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public List<ToolDefinition> getSalesTools() {
        return salesToolSet.getToolDefinitions();
    }

    public List<ToolDefinition> getButlerTools() {
        return butlerToolSet.getToolDefinitions();
    }
}
