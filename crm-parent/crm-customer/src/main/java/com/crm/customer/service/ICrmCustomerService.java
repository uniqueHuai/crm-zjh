package com.crm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmCustomer;

import java.util.List;
import java.util.Map;

public interface ICrmCustomerService extends IService<CrmCustomer> {

    IPage<CrmCustomer> selectPageWithCondition(Page<CrmCustomer> page, String keywords, Long levelId,
                                                Long ownerId, String sourceChannel, String province,
                                                String city, Boolean isSleeping, String startDate,
                                                String endDate, String tagIds);

    CrmCustomer selectDetail(Long id);

    boolean batchTag(List<Long> customerIds, List<Long> tagIds, String mode);

    boolean batchRemoveTag(List<Long> customerIds, List<Long> tagIds);

    boolean transferOwner(Long id, Long newOwnerId, Boolean transferFollowUps);

    boolean batchChangeLevel(List<Long> customerIds, Long levelId, String reason);
}
