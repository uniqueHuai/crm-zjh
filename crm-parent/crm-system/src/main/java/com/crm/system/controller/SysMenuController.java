package com.crm.system.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.framework.security.SecurityUtils;
import com.crm.system.entity.SysMenu;
import com.crm.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "菜单管理", description = "系统菜单树、动态路由")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService menuService;

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public R<Map<String, List<SysMenu>>> tree() {
        return R.ok(Map.of("records", menuService.selectMenuTree()));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:create')")
    @OperationLog(module = "system", action = "create", description = "新增菜单")
    public R<Void> create(@RequestBody SysMenu menu) {
        menuService.createMenu(menu);
        return R.ok();
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @OperationLog(module = "system", action = "update", description = "更新菜单")
    public R<Void> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        menuService.updateMenu(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除菜单")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }

    @Operation(summary = "获取路由配置（前端动态路由）")
    @GetMapping("/routes")
    public R<List<SysMenu>> routes() {
        Long userId = SecurityUtils.getUserId();
        return R.ok(menuService.selectRoutesByUserId(userId));
    }
}
