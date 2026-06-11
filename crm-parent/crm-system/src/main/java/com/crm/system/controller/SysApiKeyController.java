package com.crm.system.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysApiKey;
import com.crm.system.service.ISysApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "开放接口管理", description = "API密钥创建、管理")
@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
public class SysApiKeyController {

    private final ISysApiKeyService apiKeyService;

    @Operation(summary = "创建 API 密钥")
    @PostMapping
    @PreAuthorize("hasAuthority('system:api-key:create')")
    @OperationLog(module = "system", action = "create", description = "创建API密钥")
    public R<SysApiKey> create(@RequestBody SysApiKey apiKey) {
        return R.ok(apiKeyService.createApiKey(apiKey));
    }

    @Operation(summary = "API 密钥列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:api-key:list')")
    public R<List<SysApiKey>> list() {
        return R.ok(apiKeyService.selectList());
    }

    @Operation(summary = "禁用/启用密钥")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:api-key:edit')")
    @OperationLog(module = "system", action = "update", description = "变更API密钥状态")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        apiKeyService.updateStatus(id, body.get("status"));
        return R.ok();
    }

    @Operation(summary = "删除密钥")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:api-key:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除API密钥")
    public R<Void> delete(@PathVariable Long id) {
        apiKeyService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "重新生成密钥")
    @PostMapping("/{id}/regenerate")
    @PreAuthorize("hasAuthority('system:api-key:edit')")
    @OperationLog(module = "system", action = "update", description = "重新生成API密钥")
    public R<SysApiKey> regenerate(@PathVariable Long id) {
        return R.ok(apiKeyService.regenerateSecret(id));
    }
}
