package com.crm.collaboration.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.collaboration.entity.CrmApprovalDefine;
import com.crm.collaboration.mapper.CrmApprovalDefineMapper;
import com.crm.collaboration.service.ICrmApprovalDefineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmApprovalDefineServiceImpl extends ServiceImpl<CrmApprovalDefineMapper, CrmApprovalDefine>
        implements ICrmApprovalDefineService {
}
