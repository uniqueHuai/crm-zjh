package com.crm.ai.agent;

import com.crm.ai.agent.tools.ToolExecutor;
import com.crm.ai.conversation.service.ConversationService;
import com.crm.ai.llm.LlmService;
import com.crm.ai.llm.dto.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 销售助手 Agent — 帮助销售人员分析客户和业绩
 */
@Component
public class SalesAssistantAgent extends BaseAgent {

    public SalesAssistantAgent(LlmService llmService,
                               ConversationService conversationService,
                               ToolExecutor toolExecutor,
                               ObjectMapper objectMapper) {
        super(llmService, conversationService, toolExecutor, objectMapper);
    }

    @Override
    public String getAgentType() {
        return "sales_assistant";
    }

    @Override
    protected String getSystemPrompt() {
        return "你是 CRM 销售助手，帮助销售人员分析客户和业绩。\n\n" +
                "你可以通过工具查询：\n" +
                "1. 你负责的客户列表和详情\n" +
                "2. 你的销售业绩和趋势\n" +
                "3. 产品销量排行\n" +
                "4. 客户深度分析\n" +
                "5. 商机管道概况\n\n" +
                "你也可以将查询结果导出为 Excel 报表：\n" +
                "6. 导出客户列表到 Excel (export_customers_report)\n" +
                "7. 导出销售业绩报表到 Excel (export_sales_report)\n" +
                "8. 导出产品销量排行到 Excel (export_product_report)\n" +
                "9. 导出商机管道分析到 Excel (export_pipeline_report)\n\n" +
                "用户需要导出报表时，调用对应的 export_* 工具即可。\n\n" +
                "规则：\n" +
                "1. 数据回答要有具体数字支撑\n" +
                "2. 分析结果要给出可操作的建议\n" +
                "3. 可以对比历史数据说明趋势\n" +
                "4. 涉及客户隐私时注意数据脱敏\n" +
                "5. 鼓励销售人员积极跟进高意向客户\n" +
                "6. 用户要求导出或下载报表时，调用对应的 export_* 工具\n\n" +
                "请根据用户的问题，主动调用工具获取数据并进行分析。";
    }

    @Override
    protected List<ToolDefinition> getTools() {
        return toolExecutor.getSalesTools();
    }
}
