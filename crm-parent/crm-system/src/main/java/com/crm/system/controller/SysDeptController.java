package com.crm.system.controller;

import com.crm.common.exception.BizException;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.system.entity.SysDept;
import com.crm.system.service.ISysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "部门管理", description = "系统部门树 CRUD")
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService deptService;

    @Operation(summary = "部门树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:dept:list')")
    public R<Map<String, List<SysDept>>> tree() {
        return R.ok(Map.of("records", deptService.selectDeptTree()));
    }

    @Operation(summary = "新增部门")
    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:create')")
    @OperationLog(module = "system", action = "create", description = "新增部门")
    public R<Void> create(@RequestBody SysDept dept) {
        deptService.createDept(dept);
        return R.ok();
    }

    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @OperationLog(module = "system", action = "update", description = "更新部门")
    public R<Void> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        deptService.updateDept(dept);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    @OperationLog(module = "system", action = "delete", description = "删除部门")
    public R<Void> delete(@PathVariable Long id) {
        if (deptService.hasChildrenOrUser(id)) {
            throw new BizException(409002, "存在子部门或关联用户，禁止删除");
        }
        deptService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "部门选项列表")
    @GetMapping("/options")
    public R<List<SysDept>> options() {
        return R.ok(deptService.lambdaQuery()
                .eq(SysDept::getStatus, 1)
                .orderByAsc(SysDept::getSortOrder)
                .list());
    }
}
