package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.customer.entity.CrmContact;
import com.crm.customer.mapper.CrmContactMapper;
import com.crm.customer.service.ICrmContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmContactServiceImpl extends ServiceImpl<CrmContactMapper, CrmContact> implements ICrmContactService {

    private final CrmContactMapper contactMapper;

    @Override
    public IPage<CrmContact> selectPageWithCondition(Page<CrmContact> page, String keywords, Long customerId, Boolean isDecisionMaker) {
        return contactMapper.selectPageWithCondition(page, keywords, customerId, isDecisionMaker);
    }

    @Override
    public List<CrmContact> selectByCustomerId(Long customerId) {
        return lambdaQuery().eq(CrmContact::getCustomerId, customerId)
                .orderByAsc(CrmContact::getSortOrder)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setPrimary(Long id) {
        CrmContact contact = getById(id);
        if (contact == null) throw new BizException(404, "联系人不存在");

        lambdaUpdate().eq(CrmContact::getCustomerId, contact.getCustomerId())
                .set(CrmContact::getIsPrimary, false).update();
        return lambdaUpdate().eq(CrmContact::getId, id)
                .set(CrmContact::getIsPrimary, true).update();
    }

    @Override
    public List<CrmContact> selectUpcomingBirthdays(int days) {
        LocalDate today = LocalDate.now();
        List<CrmContact> all = lambdaQuery()
                .isNotNull(CrmContact::getBirthday)
                .list();
        return all.stream()
                .filter(c -> {
                    LocalDate bd = c.getBirthday();
                    LocalDate next = bd.withYear(today.getYear());
                    if (next.isBefore(today)) next = next.plusYears(1);
                    return ChronoUnit.DAYS.between(today, next) <= days;
                })
                .collect(Collectors.toList());
    }
}
