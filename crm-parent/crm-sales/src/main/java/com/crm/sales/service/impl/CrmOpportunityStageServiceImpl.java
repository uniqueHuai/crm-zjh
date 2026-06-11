package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.sales.entity.CrmOpportunityStage;
import com.crm.sales.mapper.CrmOpportunityStageMapper;
import com.crm.sales.service.ICrmOpportunityStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmOpportunityStageServiceImpl extends ServiceImpl<CrmOpportunityStageMapper, CrmOpportunityStage>
        implements ICrmOpportunityStageService {
}
