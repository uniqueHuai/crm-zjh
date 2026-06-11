package com.crm.customer.dto;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crm.customer.entity.CrmLead;
import com.crm.customer.service.ICrmLeadService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class LeadImportListener implements ReadListener<LeadImportRow> {

    private final ICrmLeadService leadService;
    private static final int BATCH_SIZE = 100;

    private final List<CrmLead> batch = new ArrayList<>();

    @Getter
    private int successCount = 0;
    @Getter
    private int failCount = 0;
    @Getter
    private final List<String> errorMessages = new ArrayList<>();

    private static final Map<String, String> SOURCE_MAP = Map.of(
            "小程序注册", "mini_program",
            "广告落地页", "ad_landing",
            "线下活动", "offline",
            "手动录入", "manual_input",
            "朋友推荐", "referral",
            "手工导入", "manual_import"
    );

    @Override
    public void invoke(LeadImportRow row, AnalysisContext context) {
        List<String> errors = new ArrayList<>();
        if (row.getName() == null || row.getName().isBlank()) {
            errors.add("姓名为空");
        }
        if (row.getPhone() == null || row.getPhone().isBlank()) {
            errors.add("手机号为空");
        }

        if (!errors.isEmpty()) {
            failCount++;
            errorMessages.add("第" + (successCount + failCount) + "行: " + String.join("; ", errors));
            return;
        }

        CrmLead lead = new CrmLead();
        lead.setName(row.getName().trim());
        lead.setPhone(row.getPhone().trim());
        lead.setCompany(row.getCompany() != null ? row.getCompany().trim() : null);
        lead.setPosition(row.getPosition() != null ? row.getPosition().trim() : null);
        lead.setProvince(row.getProvince() != null ? row.getProvince().trim() : null);
        lead.setCity(row.getCity() != null ? row.getCity().trim() : null);
        lead.setRemark(row.getRemark() != null ? row.getRemark().trim() : null);

        String rawSource = row.getSourceChannel() != null ? row.getSourceChannel().trim() : "";
        lead.setSourceChannel(SOURCE_MAP.getOrDefault(rawSource, rawSource));
        lead.setStatus("pending");

        batch.add(lead);
        if (batch.size() >= BATCH_SIZE) {
            saveBatch();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!batch.isEmpty()) {
            saveBatch();
        }
    }

    private void saveBatch() {
        try {
            leadService.saveBatch(batch);
            successCount += batch.size();
        } catch (Exception e) {
            log.error("批量导入失败: {}", e.getMessage());
            failCount += batch.size();
            errorMessages.add("批量保存失败: " + e.getMessage());
        }
        batch.clear();
    }
}
