package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.sales.entity.CrmAppointment;
import com.crm.sales.mapper.CrmAppointmentMapper;
import com.crm.sales.service.ICrmAppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmAppointmentServiceImpl extends ServiceImpl<CrmAppointmentMapper, CrmAppointment>
        implements ICrmAppointmentService {

    private final CrmAppointmentMapper appointmentMapper;

    @Override
    public IPage<CrmAppointment> selectPageWithCondition(Page<CrmAppointment> page, String startDate, String endDate,
                                                          String status, Long ownerId) {
        return appointmentMapper.selectPage(page, new LambdaQueryWrapper<CrmAppointment>()
                .ge(startDate != null, CrmAppointment::getAppointmentDate, startDate)
                .le(endDate != null, CrmAppointment::getAppointmentDate, endDate)
                .eq(status != null, CrmAppointment::getStatus, status)
                .eq(ownerId != null, CrmAppointment::getOwnerId, ownerId)
                .orderByAsc(CrmAppointment::getAppointmentDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long id, Map<String, Object> params) {
        CrmAppointment apt = getById(id);
        if (apt == null) throw new BizException(404, "日程不存在");
        return lambdaUpdate().eq(CrmAppointment::getId, id)
                .set(CrmAppointment::getCheckInTime, LocalDateTime.now())
                .set(CrmAppointment::getCheckInLocation, params.get("address"))
                .set(CrmAppointment::getPhotoUrls, params.get("photoUrls"))
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(Long id, Map<String, Object> params) {
        CrmAppointment apt = getById(id);
        if (apt == null) throw new BizException(404, "日程不存在");
        return lambdaUpdate().eq(CrmAppointment::getId, id)
                .set(CrmAppointment::getStatus, "completed")
                .set(CrmAppointment::getSummary, params.get("summary"))
                .set(CrmAppointment::getNextStep, params.get("nextStep"))
                .update();
    }

    @Override
    public List<Map<String, Object>> calendar(String startDate, String endDate, Long ownerId) {
        List<CrmAppointment> list = appointmentMapper.selectList(new LambdaQueryWrapper<CrmAppointment>()
                .ge(startDate != null, CrmAppointment::getAppointmentDate, startDate)
                .le(endDate != null, CrmAppointment::getAppointmentDate, endDate)
                .eq(ownerId != null, CrmAppointment::getOwnerId, ownerId)
                .orderByAsc(CrmAppointment::getAppointmentDate));
        Map<String, List<CrmAppointment>> grouped = list.stream()
                .collect(Collectors.groupingBy(a -> a.getAppointmentDate().toString()));
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((date, items) -> {
            Map<String, Object> day = new HashMap<>();
            day.put("date", date);
            day.put("items", items);
            result.add(day);
        });
        return result;
    }
}
