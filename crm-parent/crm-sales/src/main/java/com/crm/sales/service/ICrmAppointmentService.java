package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmAppointment;

import java.util.List;
import java.util.Map;

public interface ICrmAppointmentService extends IService<CrmAppointment> {

    IPage<CrmAppointment> selectPageWithCondition(Page<CrmAppointment> page, String startDate, String endDate,
                                                   String status, Long ownerId);

    boolean checkIn(Long id, Map<String, Object> params);

    boolean complete(Long id, Map<String, Object> params);

    List<Map<String, Object>> calendar(String startDate, String endDate, Long ownerId);
}
