package com.crm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmLead;

import java.util.List;
import java.util.Map;

public interface ICrmLeadService extends IService<CrmLead> {

    IPage<CrmLead> selectPageWithCondition(Page<CrmLead> page, String keywords, String status,
                                            String sourceChannel, Long ownerId, String assignType,
                                            String province, String city, String industry,
                                            String startDate, String endDate, Boolean poolReturn);

    CrmLead selectWithDedupInfo(Long id);

    boolean convertLead(Long id, Map<String, Object> params);

    boolean distributeLeads(List<Long> leadIds, Long ownerId, String assignType);

    boolean poolReturn(Long id);

    boolean batchOperate(List<Long> leadIds, String action, Map<String, Object> payload);

    Map<String, Object> dedupCheck(String phone, String wechatUnionId);

    boolean mergeLeads(Long mainId, List<Long> mergeIds, Map<String, String> fieldStrategy);

    Map<String, Object> importLeads(org.springframework.web.multipart.MultipartFile file);
}
