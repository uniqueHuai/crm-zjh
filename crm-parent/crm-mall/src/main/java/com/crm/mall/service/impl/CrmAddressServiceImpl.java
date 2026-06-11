package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.mall.entity.CrmAddress;
import com.crm.mall.mapper.CrmAddressMapper;
import com.crm.mall.service.ICrmAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmAddressServiceImpl extends ServiceImpl<CrmAddressMapper, CrmAddress> implements ICrmAddressService {

    @Override
    public List<CrmAddress> listByCustomerId(Long customerId) {
        return lambdaQuery()
                .eq(CrmAddress::getCustomerId, customerId)
                .orderByDesc(CrmAddress::getIsDefault)
                .orderByDesc(CrmAddress::getUpdatedAt)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long id, Long customerId) {
        lambdaUpdate().eq(CrmAddress::getCustomerId, customerId)
                .set(CrmAddress::getIsDefault, false)
                .update();
        return lambdaUpdate().eq(CrmAddress::getId, id)
                .eq(CrmAddress::getCustomerId, customerId)
                .set(CrmAddress::getIsDefault, true)
                .update();
    }
}
