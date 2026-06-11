package com.crm.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.report.entity.CrmCustomReport;
import com.crm.report.mapper.CrmCustomReportMapper;
import com.crm.report.service.ICrmCustomReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CrmCustomReportServiceImpl extends ServiceImpl<CrmCustomReportMapper, CrmCustomReport>
        implements ICrmCustomReportService {

    private final CrmCustomReportMapper reportMapper;

    @Override
    public IPage<CrmCustomReport> selectPage(Page<CrmCustomReport> page, String keywords, String chartType) {
        return reportMapper.selectPage(page, new LambdaQueryWrapper<CrmCustomReport>()
                .like(keywords != null && !keywords.isEmpty(), CrmCustomReport::getName, keywords)
                .eq(chartType != null && !chartType.isEmpty(), CrmCustomReport::getChartType, chartType)
                .orderByDesc(CrmCustomReport::getCreatedAt));
    }

    @Override
    public Map<String, Object> getReportData(Long id, long page, long size) {
        // TODO: execute report query dynamically based on report definition
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("columns", List.of("dimension", "metric"));
        result.put("rows", List.of());
        result.put("page", page);
        result.put("size", size);
        result.put("total", 0L);
        return result;
    }

    @Override
    public void exportReport(Long id, String format, String startDate, String endDate) {
        // TODO: async export report data to Excel/PDF
    }
}
