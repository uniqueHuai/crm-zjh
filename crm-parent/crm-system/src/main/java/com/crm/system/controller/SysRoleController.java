package com.crm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysRole;
import com.crm.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色管理", description = "系统角色 CRUD、菜单分配、数据权限分配")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService roleService;

    @Operation(summary = "角色分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list')")
    public R<IPage<SysRole>> page(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "20") long size,
                                   @RequestParam(required = false) String keywords,
                                   @RequestParam(required = false) Integer status) {
        return R.ok(roleService.selectPageWithCondition(new Page<>(page, size), keywords, status));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:create')")
    @OperationLog(module = "system", action = "create", description = "新增角色")
    public R<Void> create(@RequestBody SysRole role) {
        roleService.createRole(role);
        return R.ok();
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public R<SysRole> get(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        if (role == null) {
            return R.failed(404, "角色不存在");
        }
        role.setMenuIds(roleService.selectMenuIdsByRoleId(id));
        return R.ok(role);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @OperationLog(module = "system", action = "update", description = "更新角色")
    public R<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleService.updateRole(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除角色")
    public R<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "分配菜单权限")
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @OperationLog(module = "system", action = "update", description = "分配菜单权限")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleService.assignMenus(id, body.get("menuIds"));
        return R.ok();
    }

    @Operation(summary = "分配数据权限")
    @PutMapping("/{id}/data-scope")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @OperationLog(module = "system", action = "update", description = "分配数据权限")
    public R<Void> assignDataScope(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer dataScope = (Integer) body.get("dataScope");
        @SuppressWarnings("unchecked")
        List<Long> deptIds = (List<Long>) body.get("deptIds");
        roleService.assignDataScope(id, dataScope, deptIds);
        return R.ok();
    }

    @Operation(summary = "角色选项列表")
    @GetMapping("/options")
    public R<List<SysRole>> options() {
        return R.ok(roleService.lambdaQuery()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder)
                .list());
    }
}
