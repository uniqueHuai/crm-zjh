package com.crm.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.report.entity.CrmCustomReport;

import java.util.Map;

public interface ICrmCustomReportService extends IService<CrmCustomReport> {

    IPage<CrmCustomReport> selectPage(Page<CrmCustomReport> page, String keywords, String chartType);

    Map<String, Object> getReportData(Long id, long page, long size);

    void exportReport(Long id, String format, String startDate, String endDate);
}
