package com.crm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysUser;
import com.crm.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "系统用户 CRUD")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService userService;

    @Operation(summary = "用户分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public R<IPage<SysUser>> page(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "20") long size,
                                  @RequestParam(required = false) String keywords,
                                  @RequestParam(required = false) Long deptId,
                                  @RequestParam(required = false) Integer status) {
        return R.ok(userService.selectPageWithDept(new Page<>(page, size), keywords, deptId, status));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public R<SysUser> get(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:create')")
    @OperationLog(module = "system", action = "create", description = "新增用户")
    public R<Void> create(@RequestBody SysUser user) {
        userService.createUser(user);
        return R.ok();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @OperationLog(module = "system", action = "update", description = "更新用户")
    public R<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userService.updateUser(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除用户")
    public R<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Integer status) {
        userService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody String password) {
        userService.resetPassword(id, password);
        return R.ok();
    }
}
