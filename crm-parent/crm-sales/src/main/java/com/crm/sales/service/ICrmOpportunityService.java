package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmOpportunity;

import java.util.List;
import java.util.Map;

public interface ICrmOpportunityService extends IService<CrmOpportunity> {

    IPage<CrmOpportunity> selectPageWithCondition(Page<CrmOpportunity> page, Long stageId, Long customerId,
                                                   Long ownerId, String keywords, Boolean isExpired);

    CrmOpportunity selectWithDetail(Long id);

    boolean updateStage(Long id, Long stageId, String remark);

    boolean win(Long id, Map<String, Object> params);

    boolean lose(Long id, Map<String, Object> params);

    List<Map<String, Object>> pipeline();

    boolean addParticipants(Long id, List<Long> userIds);

    boolean removeParticipants(Long id, List<Long> userIds);
}
