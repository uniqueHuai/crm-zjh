package com.crm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmCustomer;
import com.crm.customer.entity.CrmSegment;

import java.util.List;
import java.util.Map;

public interface ICrmSegmentService extends IService<CrmSegment> {

    boolean refreshMembers(Long segmentId);

    IPage<CrmCustomer> selectMembers(Page<CrmCustomer> page, Long segmentId);

    boolean manuallyAdjustMembers(Long segmentId, List<Long> customerIds, String action);

    boolean createCampaign(Long segmentId, Map<String, Object> params);
}
