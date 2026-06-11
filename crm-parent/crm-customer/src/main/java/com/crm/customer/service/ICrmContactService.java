package com.crm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.CrmContact;

import java.time.LocalDate;
import java.util.List;

public interface ICrmContactService extends IService<CrmContact> {

    IPage<CrmContact> selectPageWithCondition(Page<CrmContact> page, String keywords, Long customerId, Boolean isDecisionMaker);

    List<CrmContact> selectByCustomerId(Long customerId);

    boolean setPrimary(Long id);

    List<CrmContact> selectUpcomingBirthdays(int days);
}
