package com.crm.ai.agent;

import com.crm.ai.agent.tools.ToolExecutor;
import com.crm.ai.conversation.service.ConversationService;
import com.crm.ai.llm.LlmService;
import com.crm.ai.llm.dto.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能管家 Agent — 管理者分析公司整体经营情况
 */
@Component
public class ButlerAgent extends BaseAgent {

    public ButlerAgent(LlmService llmService,
                       ConversationService conversationService,
                       ToolExecutor toolExecutor,
                       ObjectMapper objectMapper) {
        super(llmService, conversationService, toolExecutor, objectMapper);
    }

    @Override
    public String getAgentType() {
        return "butler";
    }

    @Override
    protected String getSystemPrompt() {
        return "你是企业管理智能助手，帮助管理者掌握公司运营全貌。\n\n" +
                "你可以：\n" +
                "1. 分析公司整体销售业绩和趋势\n" +
                "2. 分析各部门/团队绩效\n" +
                "3. 分析产品线和客户结构\n" +
                "4. 生成经营分析报告\n\n" +
                "你也可以将分析结果导出为 Excel 报表：\n" +
                "5. 导出公司经营概览到 Excel (export_company_report)\n" +
                "6. 导出销售趋势分析到 Excel (export_sales_trend_report)\n" +
                "7. 导出部门业绩排行到 Excel (export_department_report)\n" +
                "8. 导出客户结构分析到 Excel (export_customer_structure_report)\n\n" +
                "用户需要导出报表时，调用对应的 export_* 工具即可。\n\n" +
                "规则：\n" +
                "1. 数据必须准确，注明数据截止时间\n" +
                "2. 分析要有同比/环比对比\n" +
                "3. 发现问题时要指出具体原因和改进建议\n" +
                "4. 支持一键生成报告摘要\n" +
                "5. 用户要求导出或下载报表时，调用对应的 export_* 工具\n\n" +
                "请根据用户的问题，主动调用工具获取数据并进行深度分析。";
    }

    @Override
    protected List<ToolDefinition> getTools() {
        return toolExecutor.getButlerTools();
    }
}
