package com.crm.collaboration.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.collaboration.entity.CrmRefund;
import com.crm.collaboration.mapper.CrmRefundMapper;
import com.crm.collaboration.service.ICrmRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmRefundServiceImpl extends ServiceImpl<CrmRefundMapper, CrmRefund> implements ICrmRefundService {
}
