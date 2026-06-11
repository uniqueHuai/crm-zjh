package com.crm.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmAddress;

import java.util.List;

public interface ICrmAddressService extends IService<CrmAddress> {

    List<CrmAddress> listByCustomerId(Long customerId);

    boolean setDefault(Long id, Long customerId);
}
