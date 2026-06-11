package com.crm.ai.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentRouter {

    private final CustomerServiceAgent customerServiceAgent;
    private final SalesAssistantAgent salesAssistantAgent;
    private final ButlerAgent butlerAgent;

    public BaseAgent getAgent(String agentType) {
        return switch (agentType) {
            case "customer_service" -> customerServiceAgent;
            case "sales_assistant" -> salesAssistantAgent;
            case "butler" -> butlerAgent;
            default -> throw new IllegalArgumentException("Unknown agent type: " + agentType);
        };
    }
}
