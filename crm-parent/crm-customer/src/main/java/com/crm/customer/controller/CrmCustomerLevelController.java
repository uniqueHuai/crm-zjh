package com.crm.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.exception.BizException;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.customer.entity.CrmCustomerLevel;
import com.crm.customer.entity.CrmCustomerLevelRule;
import com.crm.customer.service.ICrmCustomerLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "客户分级", description = "客户等级 CRUD、升降级规则、手动评估")
@RestController
@RequestMapping("/customer-levels")
@RequiredArgsConstructor
public class CrmCustomerLevelController {

    private final ICrmCustomerLevelService levelService;

    @Operation(summary = "等级列表")
    @GetMapping
    @PreAuthorize("hasAuthority('customer:level:list')")
    public R<List<CrmCustomerLevel>> list() {
        return R.ok(levelService.lambdaQuery()
                .orderByAsc(CrmCustomerLevel::getSortOrder)
                .list());
    }

    @Operation(summary = "新增等级")
    @PostMapping
    @PreAuthorize("hasAuthority('customer:level:create')")
    @OperationLog(module = "customer", action = "create", description = "新增客户等级")
    public R<Void> create(@RequestBody CrmCustomerLevel level) {
        levelService.save(level);
        return R.ok();
    }

    @Operation(summary = "更新等级")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:level:edit')")
    @OperationLog(module = "customer", action = "update", description = "更新客户等级")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmCustomerLevel level) {
        level.setId(id);
        levelService.updateById(level);
        return R.ok();
    }

    @Operation(summary = "删除等级")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:level:delete')")
    @OperationLog(module = "customer", action = "delete", description = "删除客户等级")
    public R<Void> delete(@PathVariable Long id) {
        CrmCustomerLevel level = levelService.getById(id);
        if (level == null) throw new BizException(404, "等级不存在");
        // TODO: check if any customers are associated with this level
        levelService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "设置升降级规则")
    @PutMapping("/{id}/rules")
    @PreAuthorize("hasAuthority('customer:level:edit')")
    @OperationLog(module = "customer", action = "update", description = "设置升降级规则")
    public R<Void> setRules(@PathVariable Long id, @RequestBody Map<String, Object> rules) {
        levelService.setLevelRules(id, rules);
        return R.ok();
    }

    @Operation(summary = "获取升降级规则")
    @GetMapping("/{id}/rules")
    @PreAuthorize("hasAuthority('customer:level:query')")
    public R<List<CrmCustomerLevelRule>> getRules(@PathVariable Long id) {
        return R.ok(levelService.getLevelRules(id));
    }

    @Operation(summary = "手动执行升降级评估")
    @PostMapping("/evaluate")
    @PreAuthorize("hasAuthority('customer:level:evaluate')")
    @OperationLog(module = "customer", action = "execute", description = "执行升降级评估")
    public R<Void> evaluate() {
        levelService.evaluateAll();
        return R.ok();
    }
}
