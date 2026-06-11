package com.crm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysConfig;
import com.crm.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统配置", description = "系统参数配置管理")
@RestController
@RequestMapping("/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final ISysConfigService configService;

    @Operation(summary = "配置分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:config:list')")
    public R<IPage<SysConfig>> page(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "20") long size,
                                     @RequestParam(required = false) String keywords) {
        return R.ok(configService.selectPageWithCondition(new Page<>(page, size), keywords));
    }

    @Operation(summary = "获取单条配置")
    @GetMapping("/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public R<SysConfig> get(@PathVariable String configKey) {
        return R.ok(configService.selectByConfigKey(configKey));
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{configKey}")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @OperationLog(module = "system", action = "update", description = "更新系统配置")
    public R<Void> update(@PathVariable String configKey, @RequestBody SysConfig config) {
        configService.updateByConfigKey(configKey, config);
        return R.ok();
    }

    @Operation(summary = "新增配置")
    @PostMapping
    @PreAuthorize("hasAuthority('system:config:create')")
    @OperationLog(module = "system", action = "create", description = "新增系统配置")
    public R<Void> create(@RequestBody SysConfig config) {
        configService.createConfig(config);
        return R.ok();
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除系统配置")
    public R<Void> delete(@PathVariable Long id) {
        configService.removeConfig(id);
        return R.ok();
    }

    @Operation(summary = "获取所有公开配置")
    @GetMapping("/public")
    public R<List<SysConfig>> getPublic() {
        return R.ok(configService.selectPublicConfigs());
    }

    @Operation(summary = "刷新配置缓存")
    @PostMapping("/refresh")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @OperationLog(module = "system", action = "update", description = "刷新配置缓存")
    public R<Void> refresh() {
        configService.refreshCache();
        return R.ok();
    }
}
