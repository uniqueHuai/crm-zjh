package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmDistributionRule;
import com.crm.mall.service.ICrmDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分销管理", description = "分销规则设置、团队和佣金查询")
@RestController
@RequestMapping("/distribution")
@RequiredArgsConstructor
public class CrmDistributionController {

    private final ICrmDistributionService distributionService;

    @Operation(summary = "分销规则列表")
    @GetMapping("/rules")
    @PreAuthorize("hasAuthority('mall:distribution:list')")
    public R<List<CrmDistributionRule>> listRules() {
        return R.ok(distributionService.lambdaQuery()
                .eq(CrmDistributionRule::getStatus, 1)
                .list());
    }

    @Operation(summary = "设置分销规则")
    @PostMapping("/rules")
    @PreAuthorize("hasAuthority('mall:distribution:edit')")
    @OperationLog(module = "mall", action = "create", description = "设置分销规则")
    public R<Void> createRule(@RequestBody CrmDistributionRule rule) {
        distributionService.save(rule);
        return R.ok();
    }
}
