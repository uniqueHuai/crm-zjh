package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.entity.CrmDistributor;
import com.crm.mall.entity.CrmCommission;
import com.crm.mall.mp.MpContext;
import com.crm.mall.service.ICrmDistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "小程序端分销")
@RestController
@RequestMapping("/app/distribution")
@RequiredArgsConstructor
public class CrmAppDistributionController {

    private final ICrmDistributionService distributionService;

    @Operation(summary = "我的分销概况")
    @GetMapping("/my")
    public R<Map<String, Object>> myProfile() {
        return R.ok(distributionService.getDistributorProfile(MpContext.getCustomerId()));
    }

    @Operation(summary = "我的团队")
    @GetMapping("/team")
    public R<List<CrmDistributor>> team() {
        CrmDistributor distributor = distributionService.getDistributorByUserId(MpContext.getCustomerId());
        if (distributor == null) return R.ok(List.of());
        return R.ok(distributionService.selectTeam(distributor.getId()));
    }

    @Operation(summary = "佣金记录")
    @GetMapping("/commission")
    public R<List<CrmCommission>> commissions() {
        CrmDistributor distributor = distributionService.getDistributorByUserId(MpContext.getCustomerId());
        if (distributor == null) return R.ok(List.of());
        return R.ok(distributionService.selectCommissions(distributor.getId()));
    }

    @Operation(summary = "绑定推荐关系（注册时调用）")
    @PostMapping("/bind")
    public R<Void> bind(@RequestBody Map<String, Long> body) {
        Long referrerId = body.get("referrerId");
        if (referrerId != null) {
            distributionService.bindReferral(MpContext.getCustomerId(), referrerId);
        }
        return R.ok();
    }

    @Operation(summary = "我的分销二维码/海报信息")
    @GetMapping("/share")
    public R<Map<String, Object>> shareInfo() {
        Long customerId = MpContext.getCustomerId();
        // return share url with referrer id
        String shareUrl = "/app/auth/login?referrerId=" + customerId;
        return R.ok(Map.of(
                "shareUrl", shareUrl,
                "customerId", customerId
        ));
    }
}
