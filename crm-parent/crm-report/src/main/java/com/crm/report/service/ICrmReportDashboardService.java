package com.crm.report.service;

import java.util.Map;

public interface ICrmReportDashboardService {

    Map<String, Object> salesFunnel(String startDate, String endDate, Long ownerId, Long deptId);

    Map<String, Object> customerAnalysis(String startDate, String endDate);

    Map<String, Object> employeePerformance(String startDate, String endDate, Long deptId);

    Map<String, Object> mall(String startDate, String endDate);

    Object templates();

    boolean saveLayout(Long userId, java.util.List<Object> cards);

    Map<String, Object> summary();
}
