package com.crm.ai.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmServiceFactory {

    private final SiliconFlowLlmService siliconFlowLlmService;

    public LlmService getService(String provider) {
        if ("siliconflow".equalsIgnoreCase(provider)) {
            return siliconFlowLlmService;
        }
        return siliconFlowLlmService; // 默认
    }

    public LlmService getDefault() {
        return siliconFlowLlmService;
    }
}
