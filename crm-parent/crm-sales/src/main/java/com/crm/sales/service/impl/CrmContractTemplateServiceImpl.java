package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.sales.entity.CrmContractTemplate;
import com.crm.sales.mapper.CrmContractTemplateMapper;
import com.crm.sales.service.ICrmContractTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmContractTemplateServiceImpl extends ServiceImpl<CrmContractTemplateMapper, CrmContractTemplate>
        implements ICrmContractTemplateService {
}
