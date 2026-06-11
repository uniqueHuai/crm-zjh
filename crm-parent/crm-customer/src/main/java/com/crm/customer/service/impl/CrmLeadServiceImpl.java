package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.customer.entity.CrmCustomer;
import com.crm.customer.entity.CrmLead;
import com.crm.customer.mapper.CrmLeadMapper;
import com.crm.customer.service.ICrmCustomerService;
import com.crm.customer.service.ICrmLeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmLeadServiceImpl extends ServiceImpl<CrmLeadMapper, CrmLead> implements ICrmLeadService {

    private final CrmLeadMapper leadMapper;
    private final ICrmCustomerService customerService;

    @Override
    public IPage<CrmLead> selectPageWithCondition(Page<CrmLead> page, String keywords, String status,
                                                   String sourceChannel, Long ownerId, String assignType,
                                                   String province, String city, String industry,
                                                   String startDate, String endDate, Boolean poolReturn) {
        return leadMapper.selectPageWithCondition(page, keywords, status, sourceChannel, ownerId,
                assignType, province, city, industry, startDate, endDate, poolReturn);
    }

    @Override
    public CrmLead selectWithDedupInfo(Long id) {
        CrmLead lead = getById(id);
        if (lead == null) throw new BizException(404, "线索不存在");
        return lead;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean convertLead(Long id, Map<String, Object> params) {
        CrmLead lead = getById(id);
        if (lead == null) throw new BizException(404, "线索不存在");
        if (!"following".equals(lead.getStatus())) {
            throw new BizException(409001, "仅跟进中的线索可转换");
        }
        CrmCustomer customer = new CrmCustomer();
        customer.setName(lead.getName());
        customer.setPhone(lead.getPhone());
        customer.setCompany(lead.getCompany());
        customer.setPosition(lead.getPosition());
        customer.setProvince(lead.getProvince());
        customer.setCity(lead.getCity());
        customer.setSourceChannel(lead.getSourceChannel());
        customer.setOwnerId(lead.getOwnerId());
        customer.setRemark(lead.getRemark());
        customer.setLevelId(1L); // 默认为"普通客户"
        customerService.save(customer);

        lambdaUpdate().eq(CrmLead::getId, id)
                .set(CrmLead::getStatus, "converted")
                .set(CrmLead::getConvertCustomerId, customer.getId())
                .update();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean distributeLeads(List<Long> leadIds, Long ownerId, String assignType) {
        for (Long id : leadIds) {
            lambdaUpdate().eq(CrmLead::getId, id)
                    .set(CrmLead::getStatus, "following")
                    .set(CrmLead::getOwnerId, ownerId)
                    .set(CrmLead::getAssignType, assignType)
                    .set(CrmLead::getAssignedAt, LocalDateTime.now())
                    .update();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean poolReturn(Long id) {
        return lambdaUpdate().eq(CrmLead::getId, id)
                .set(CrmLead::getPoolReturnAt, LocalDateTime.now())
                .set(CrmLead::getOwnerId, null)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchOperate(List<Long> leadIds, String action, Map<String, Object> payload) {
        switch (action) {
            case "distribute" -> distributeLeads(leadIds, Long.valueOf(payload.get("ownerId").toString()), "manual");
            case "delete" -> removeByIds(leadIds);
            case "pool-return" -> leadIds.forEach(this::poolReturn);
            default -> throw new BizException(400001, "不支持的操作: " + action);
        }
        return true;
    }

    @Override
    public Map<String, Object> importLeads(org.springframework.web.multipart.MultipartFile file) {
        try {
            com.crm.customer.dto.LeadImportListener listener = new com.crm.customer.dto.LeadImportListener(this);
            com.alibaba.excel.EasyExcel.read(file.getInputStream(), com.crm.customer.dto.LeadImportRow.class, listener)
                    .sheet().doRead();
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("totalCount", listener.getSuccessCount() + listener.getFailCount());
            result.put("successCount", listener.getSuccessCount());
            result.put("failCount", listener.getFailCount());
            result.put("errors", listener.getErrorMessages());
            return result;
        } catch (java.io.IOException e) {
            throw new com.crm.common.exception.BizException(400001, "文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> dedupCheck(String phone, String wechatUnionId) {
        // TODO: implement dedup logic
        return Map.of("isDuplicate", false, "matchedLeads", List.of(), "matchedCustomers", List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mergeLeads(Long mainId, List<Long> mergeIds, Map<String, String> fieldStrategy) {
        // TODO: implement merge logic
        mergeIds.forEach(id -> lambdaUpdate().eq(CrmLead::getId, id)
                .set(CrmLead::getStatus, "merged").update());
        return true;
    }
}
