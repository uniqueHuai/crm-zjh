package com.crm.collaboration.controller;

import com.crm.common.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "企业微信集成", description = "JS-SDK配置、绑定、消息、行为推送")
@RestController
@RequestMapping("/wecom")
@RequiredArgsConstructor
public class CrmWecomController {

    @Operation(summary = "获取企微JS-SDK配置")
    @GetMapping("/js-config")
    public R<Map<String, Object>> jsConfig(@RequestParam String url) {
        // TODO: implement real WeCom JS-SDK config
        return R.ok(Map.of(
                "corpId", "wx...",
                "agentId", "1000001",
                "timestamp", System.currentTimeMillis() / 1000,
                "nonceStr", "random",
                "signature", "sign"
        ));
    }

    @Operation(summary = "绑定企微用户")
    @PostMapping("/bind")
    @PreAuthorize("hasAuthority('collaboration:wecom:bind')")
    public R<Void> bind(@RequestBody Map<String, Object> body) {
        // TODO: implement binding
        return R.ok();
    }

    @Operation(summary = "侧边栏客户详情")
    @GetMapping("/sidebar/customer")
    public R<Map<String, Object>> sidebarCustomer(@RequestParam Long customerId) {
        // TODO: return simplified customer info for WeCom sidebar
        return R.ok(Map.of(
                "customerId", customerId,
                "name", "客户名称",
                "phone", "138****8000"
        ));
    }

    @Operation(summary = "发送企微消息")
    @PostMapping("/messages/send")
    @PreAuthorize("hasAuthority('collaboration:wecom:send')")
    public R<Void> sendMessage(@RequestBody Map<String, Object> body) {
        // TODO: call WeCom API to send message
        return R.ok();
    }

    @Operation(summary = "推送客户行为到企微")
    @PostMapping("/notify/behavior")
    public R<Void> notifyBehavior(@RequestBody Map<String, Object> body) {
        // TODO: push customer behavior to WeCom
        return R.ok();
    }
}
