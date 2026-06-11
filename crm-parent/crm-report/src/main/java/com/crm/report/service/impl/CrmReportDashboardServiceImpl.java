package com.crm.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.report.entity.CrmReportCardConfig;
import com.crm.report.mapper.CrmReportCardConfigMapper;
import com.crm.report.mapper.CrmReportDashboardMapper;
import com.crm.report.service.ICrmReportDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmReportDashboardServiceImpl implements ICrmReportDashboardService {

    private final CrmReportCardConfigMapper cardConfigMapper;
    private final CrmReportDashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> salesFunnel(String startDate, String endDate, Long ownerId, Long deptId) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> stages = dashboardMapper.selectOpportunitiesByStage(startDate, endDate);
        Map<String, Object> counts = dashboardMapper.selectOpportunityCounts(startDate, endDate);

        long totalCount = toLong(counts.get("total_count"));
        long wonCount = toLong(counts.get("won_count"));
        long lostCount = toLong(counts.get("lost_count"));
        BigDecimal totalExpected = (BigDecimal) counts.get("total_expected_amount");

        // Build stage list with proper types
        List<Map<String, Object>> stageList = stages.stream().map(s -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stageId", s.get("stage_id"));
            item.put("stageName", s.get("stage_name"));
            item.put("count", toInt(s.get("count")));
            item.put("amount", toBigDecimal(s.get("amount")));
            return item;
        }).collect(Collectors.toList());

        double winRate = totalCount > 0 ? roundDouble(wonCount * 100.0 / totalCount, 1) : 0.0;

        // Forecast revenue: sum of expected amounts from open stages only
        BigDecimal forecastRevenue = BigDecimal.ZERO;
        for (Map<String, Object> s : stages) {
            Integer probability = s.get("probability") instanceof Number
                    ? ((Number) s.get("probability")).intValue() : 0;
            if (probability > 0 && probability < 100) {
                forecastRevenue = forecastRevenue.add(toBigDecimal(s.get("amount")));
            }
        }

        result.put("stages", stageList);
        result.put("winRate", winRate);
        result.put("avgDealCycle", 45); // TODO: calculate from stage log data
        result.put("forecastRevenue", forecastRevenue);
        result.put("comparison", Map.of("winRateChange", 0.0, "amountChange", 0.0));

        return result;
    }

    @Override
    public Map<String, Object> customerAnalysis(String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Customer trend (supports date range filtering)
        List<Map<String, Object>> trend = dashboardMapper.selectCustomerTrend(startDate, endDate);
        List<Map<String, Object>> newCustomers = trend.stream().map(t -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", t.get("date"));
            item.put("count", toInt(t.get("count")));
            return item;
        }).collect(Collectors.toList());

        result.put("trend", Map.of("newCustomers", newCustomers, "lostCustomers", Collections.emptyList()));

        // Level distribution
        List<Map<String, Object>> levelDist = dashboardMapper.selectCustomerLevelDistribution();
        List<Map<String, Object>> levels = levelDist.stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("levelName", l.get("level_name"));
            item.put("count", toInt(l.get("count")));
            item.put("percentage", toDouble(l.get("percentage")));
            return item;
        }).collect(Collectors.toList());
        result.put("levelDistribution", levels);

        // Sleeping customers
        Integer sleepingCount = dashboardMapper.selectSleepingCustomerCount();
        Integer totalCustomers = dashboardMapper.selectTotalCustomers();
        double sleepingRate = totalCustomers > 0 ? roundDouble(sleepingCount * 100.0 / totalCustomers, 1) : 0.0;

        result.put("sleepingCount", sleepingCount);
        result.put("sleepingRate", sleepingRate);

        // RFM
        Map<String, Object> rfm = dashboardMapper.selectCustomerRfmStats();
        result.put("rfm", Map.of(
                "avgRecency", toInt(rfm.get("avg_recency")),
                "avgFrequency", toDouble(rfm.get("avg_frequency")),
                "avgMonetary", toDouble(rfm.get("avg_monetary"))
        ));

        return result;
    }

    @Override
    public Map<String, Object> employeePerformance(String startDate, String endDate, Long deptId) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> rankings = dashboardMapper.selectEmployeeRanking(startDate, endDate);

        // Filter by deptId if specified
        if (deptId != null) {
            rankings = rankings.stream()
                    .filter(r -> deptId.equals(r.get("dept_id")))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (Map<String, Object> r : rankings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", toLong(r.get("user_id")));
            item.put("realName", r.get("real_name"));
            item.put("deptName", r.get("dept_name"));
            item.put("newCustomerCount", toInt(r.get("new_customer_count")));
            item.put("dealAmount", toBigDecimal(r.get("deal_amount")));
            item.put("dealCount", toInt(r.get("deal_count")));
            item.put("followUpCount", 0);
            item.put("winRate", 0.0);
            rankingList.add(item);
        }

        result.put("rankings", rankingList);
        result.put("avgResponseTime", 0);
        result.put("avgFollowUpPerDay", 0);

        return result;
    }

    @Override
    public Map<String, Object> mall(String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gmv", 0);
        result.put("avgOrderAmount", 0);
        result.put("repurchaseRate", 0);
        result.put("topProducts", Collections.emptyList());
        result.put("conversionRate", Map.of("visitToCart", 0, "cartToPay", 0, "visitToPay", 0));
        return result;
    }

    @Override
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCustomers", dashboardMapper.selectTotalCustomers());
        Map<String, Object> oppCounts = dashboardMapper.selectOpportunityCounts(null, null);
        result.put("totalOppAmount", oppCounts.get("total_expected_amount"));
        result.put("activeOppCount", oppCounts.get("open_count"));
        result.put("totalContracts", dashboardMapper.selectTotalContracts());
        result.put("monthlyNewCustomers", dashboardMapper.selectMonthlyNewCustomers());
        result.put("signedContractAmount", dashboardMapper.selectSignedContractSummary().get("total_amount"));
        return result;
    }

    @Override
    public Object templates() {
        return List.of(
                Map.of("cardType", "sales_funnel", "name", "销售漏斗", "defaultWidth", 6, "defaultHeight", 2),
                Map.of("cardType", "customer_trend", "name", "客户趋势", "defaultWidth", 6, "defaultHeight", 2),
                Map.of("cardType", "employee_performance", "name", "员工效能", "defaultWidth", 6, "defaultHeight", 2),
                Map.of("cardType", "mall_overview", "name", "商城经营", "defaultWidth", 6, "defaultHeight", 2)
        );
    }

    @Override
    public boolean saveLayout(Long userId, List<Object> cards) {
        CrmReportCardConfig config = cardConfigMapper.selectOne(
                new LambdaQueryWrapper<CrmReportCardConfig>()
                        .eq(CrmReportCardConfig::getUserId, userId));
        if (config == null) {
            config = new CrmReportCardConfig();
            config.setUserId(userId);
            config.setCards(cards);
            cardConfigMapper.insert(config);
        } else {
            config.setCards(cards);
            cardConfigMapper.updateById(config);
        }
        return true;
    }

    // ---- helper methods ----

    private int toInt(Object v) {
        if (v == null) return 0;
        return ((Number) v).intValue();
    }

    private long toLong(Object v) {
        if (v == null) return 0L;
        return ((Number) v).longValue();
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        return ((Number) v).doubleValue();
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        return BigDecimal.valueOf(((Number) v).doubleValue());
    }

    private double roundDouble(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
