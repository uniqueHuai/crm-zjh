package com.crm.collaboration.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.collaboration.entity.CrmTicket;
import com.crm.collaboration.mapper.CrmTicketMapper;
import com.crm.collaboration.service.ICrmTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmTicketServiceImpl extends ServiceImpl<CrmTicketMapper, CrmTicket> implements ICrmTicketService {
}
