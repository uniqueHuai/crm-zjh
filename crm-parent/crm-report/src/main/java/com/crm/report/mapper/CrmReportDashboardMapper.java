package com.crm.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CrmReportDashboardMapper {

    @Select("<script>" +
            "SELECT o.stage_id AS stage_id, s.name AS stage_name, s.probability AS probability, " +
            "COUNT(*)::int AS count, COALESCE(SUM(o.expected_amount), 0) AS amount " +
            "FROM crm_opportunity o " +
            "LEFT JOIN crm_opportunity_stage s ON o.stage_id = s.id " +
            "WHERE o.deleted_at IS NULL AND s.deleted_at IS NULL " +
            "<if test='startDate != null'>AND o.created_at &gt;= #{startDate}::date</if>" +
            "<if test='endDate != null'>AND o.created_at &lt;= #{endDate}::date + interval '1 day'</if>" +
            "GROUP BY o.stage_id, s.name, s.probability, s.sort_order " +
            "ORDER BY s.sort_order" +
            "</script>")
    List<Map<String, Object>> selectOpportunitiesByStage(@Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);

    @Select("<script>" +
            "SELECT " +
            "COUNT(*)::int AS total_count, " +
            "COALESCE(SUM(CASE WHEN s.category = 'win' THEN 1 ELSE 0 END), 0)::int AS won_count, " +
            "COALESCE(SUM(CASE WHEN s.category = 'lose' THEN 1 ELSE 0 END), 0)::int AS lost_count, " +
            "COALESCE(SUM(CASE WHEN s.category = 'open' THEN 1 ELSE 0 END), 0)::int AS open_count, " +
            "COALESCE(SUM(o.expected_amount), 0) AS total_expected_amount " +
            "FROM crm_opportunity o " +
            "LEFT JOIN crm_opportunity_stage s ON o.stage_id = s.id " +
            "WHERE o.deleted_at IS NULL AND s.deleted_at IS NULL " +
            "<if test='startDate != null'>AND o.created_at &gt;= #{startDate}::date</if>" +
            "<if test='endDate != null'>AND o.created_at &lt;= #{endDate}::date + interval '1 day'</if>" +
            "</script>")
    Map<String, Object> selectOpportunityCounts(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate);

    @Select("<script>" +
            "SELECT TO_CHAR(created_at, <choose>" +
            "<when test='startDate != null'>'YYYY-MM-DD'</when>" +
            "<otherwise>'YYYY-MM'</otherwise>" +
            "</choose>) AS date, COUNT(*)::int AS count " +
            "FROM crm_customer " +
            "WHERE deleted_at IS NULL " +
            "<choose>" +
            "<when test='startDate != null'>AND created_at &gt;= #{startDate}::date</when>" +
            "<otherwise>AND created_at &gt;= (NOW() - INTERVAL '12 months')</otherwise>" +
            "</choose>" +
            "<if test='endDate != null'>AND created_at &lt;= #{endDate}::date + interval '1 day'</if>" +
            "GROUP BY date ORDER BY date" +
            "</script>")
    List<Map<String, Object>> selectCustomerTrend(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    @Select("SELECT c.level_id AS level_id, COALESCE(l.name, '未分类') AS level_name, " +
            "COUNT(*)::int AS count, " +
            "ROUND(100.0 * COUNT(*) / NULLIF(SUM(COUNT(*)) OVER(), 0), 1) AS percentage " +
            "FROM crm_customer c " +
            "LEFT JOIN crm_customer_level l ON c.level_id = l.id " +
            "WHERE c.deleted_at IS NULL " +
            "GROUP BY c.level_id, l.name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> selectCustomerLevelDistribution();

    @Select("SELECT COUNT(*)::int AS count " +
            "FROM crm_customer " +
            "WHERE deleted_at IS NULL " +
            "AND (last_contact_at IS NULL OR last_contact_at < NOW() - INTERVAL '90 days')")
    Integer selectSleepingCustomerCount();

    @Select("SELECT " +
            "COALESCE(ROUND(AVG(EXTRACT(DAY FROM (NOW() - last_contact_at)))::numeric), 0)::int AS avg_recency, " +
            "COALESCE(ROUND(AVG(order_count::numeric), 1), 0) AS avg_frequency, " +
            "COALESCE(ROUND(AVG(total_consumption::numeric), 0), 0) AS avg_monetary " +
            "FROM crm_customer " +
            "WHERE deleted_at IS NULL AND last_contact_at IS NOT NULL")
    Map<String, Object> selectCustomerRfmStats();

    @Select("<script>" +
            "SELECT u.id AS user_id, u.real_name AS real_name, d.name AS dept_name, " +
            "COUNT(DISTINCT c.id)::int AS new_customer_count, " +
            "COALESCE(SUM(ct.total_amount), 0) AS deal_amount, " +
            "COUNT(DISTINCT ct.id)::int AS deal_count " +
            "FROM sys_user u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "LEFT JOIN crm_customer c ON c.owner_id = u.id AND c.deleted_at IS NULL " +
            "<if test='startDate != null'>AND c.created_at &gt;= #{startDate}::date</if>" +
            "<if test='endDate != null'>AND c.created_at &lt;= #{endDate}::date + interval '1 day'</if>" +
            "LEFT JOIN crm_opportunity o ON o.owner_id = u.id AND o.deleted_at IS NULL " +
            "LEFT JOIN crm_contract ct ON ct.opportunity_id = o.id AND ct.status = 'signed' AND ct.deleted_at IS NULL " +
            "WHERE u.deleted_at IS NULL " +
            "GROUP BY u.id, u.real_name, d.name " +
            "ORDER BY deal_amount DESC" +
            "</script>")
    List<Map<String, Object>> selectEmployeeRanking(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    @Select("SELECT COUNT(*)::int AS total FROM crm_customer WHERE deleted_at IS NULL")
    Integer selectTotalCustomers();

    @Select("SELECT COUNT(*)::int AS total FROM crm_contract WHERE deleted_at IS NULL")
    Integer selectTotalContracts();

    @Select("SELECT COUNT(*)::int AS total FROM crm_opportunity WHERE deleted_at IS NULL")
    Integer selectTotalOpportunities();

    @Select("SELECT COUNT(*)::int AS total FROM crm_customer " +
            "WHERE deleted_at IS NULL " +
            "AND created_at >= DATE_TRUNC('month', NOW())")
    Integer selectMonthlyNewCustomers();

    @Select("SELECT COALESCE(SUM(ct.total_amount), 0) AS total_amount, COUNT(*)::int AS total_count " +
            "FROM crm_contract ct WHERE ct.deleted_at IS NULL AND ct.status = 'signed'")
    Map<String, Object> selectSignedContractSummary();
}
