package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.common.model.PageResult;
import com.crm.customer.entity.*;
import com.crm.customer.mapper.*;
import com.crm.customer.service.ICrmCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmCustomerServiceImpl extends ServiceImpl<CrmCustomerMapper, CrmCustomer> implements ICrmCustomerService {

    private final CrmCustomerMapper customerMapper;
    private final CrmTagMapper tagMapper;
    private final CrmCustomerTagMapper customerTagMapper;
    private final CrmContactMapper contactMapper;
    private final CrmActivityLogMapper activityLogMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<CrmCustomer> selectPageWithCondition(Page<CrmCustomer> page, String keywords, Long levelId,
                                                       Long ownerId, String sourceChannel, String province,
                                                       String city, Boolean isSleeping, String startDate,
                                                       String endDate, String tagIds) {
        IPage<CrmCustomer> result = customerMapper.selectPageWithCondition(page, keywords, levelId, ownerId, sourceChannel,
                province, city, isSleeping, startDate, endDate);
        // batch populate tags for display
        if (!result.getRecords().isEmpty()) {
            List<Long> customerIds = result.getRecords().stream().map(CrmCustomer::getId).collect(Collectors.toList());
            List<CrmCustomerTag> allLinks = customerTagMapper.selectList(
                    new LambdaQueryWrapper<CrmCustomerTag>().in(CrmCustomerTag::getCustomerId, customerIds));
            Map<Long, List<Long>> tagIdsByCustomer = allLinks.stream()
                    .collect(Collectors.groupingBy(CrmCustomerTag::getCustomerId,
                            Collectors.mapping(CrmCustomerTag::getTagId, Collectors.toList())));
            List<Long> distinctTagIds = allLinks.stream().map(CrmCustomerTag::getTagId).distinct().collect(Collectors.toList());
            List<CrmTag> allTags = distinctTagIds.isEmpty() ? List.of() : tagMapper.selectBatchIds(distinctTagIds);
            Map<Long, CrmTag> tagMap = allTags.stream().collect(Collectors.toMap(CrmTag::getId, t -> t));
            for (CrmCustomer customer : result.getRecords()) {
                List<Long> ids = tagIdsByCustomer.getOrDefault(customer.getId(), List.of());
                customer.setTags(ids.stream().map(tagMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> selectDetail(Long id) {
        CrmCustomer customer = getById(id);
        if (customer == null) throw new BizException(404, "客户不存在");

        // 查标签
        List<CrmTag> tags = tagMapper.selectByCustomerId(id);
        customer.setTagIds(tags.stream().map(CrmTag::getId).collect(Collectors.toList()));

        // 补充等级名称和负责人名称（getById 只查单表不会 JOIN）
        try {
            Map<String, Object> extra = jdbcTemplate.queryForMap(
                    "SELECT l.name AS level_name, u.real_name AS owner_name " +
                    "FROM crm_customer c " +
                    "LEFT JOIN crm_customer_level l ON c.level_id = l.id " +
                    "LEFT JOIN sys_user u ON c.owner_id = u.id " +
                    "WHERE c.id = ?", id);
            customer.setLevelName((String) extra.get("level_name"));
            customer.setOwnerName((String) extra.get("owner_name"));
        } catch (Exception ignored) { }

        Map<String, Object> result = new HashMap<>();
        result.put("customer", customer);

        // 最近跟进记录（取最新5条，含创建人姓名）
        // 注：PostgreSQL jdbc 会将未加引号的别名转为小写，故用双引号保留驼峰
        String followUpSql = "SELECT f.id, f.customer_id AS \"customerId\", f.type, f.content, " +
                "f.is_important AS \"isImportant\", f.created_at AS \"createdAt\", " +
                "u.real_name AS \"creatorName\" " +
                "FROM crm_follow_up f " +
                "LEFT JOIN sys_user u ON f.creator_id = u.id " +
                "WHERE f.customer_id = ? ORDER BY f.created_at DESC LIMIT 5";
        List<Map<String, Object>> recentFollowUps = jdbcTemplate.queryForList(followUpSql, id);
        result.put("recentFollowUps", recentFollowUps);

        // 关联商机（含阶段名称和负责人姓名）
        String oppSql = "SELECT o.id, o.name, o.expected_amount AS \"expectedAmount\", " +
                "o.expected_close_date AS \"expectedCloseDate\", o.stage_id AS \"stageId\", " +
                "s.name AS \"stageName\", o.owner_id AS \"ownerId\", u.real_name AS \"ownerName\" " +
                "FROM crm_opportunity o " +
                "LEFT JOIN crm_opportunity_stage s ON o.stage_id = s.id " +
                "LEFT JOIN sys_user u ON o.owner_id = u.id " +
                "WHERE o.customer_id = ?";
        List<Map<String, Object>> opportunities = jdbcTemplate.queryForList(oppSql, id);
        result.put("opportunities", opportunities);

        // 联系人
        List<CrmContact> contacts = contactMapper.selectList(
                new LambdaQueryWrapper<CrmContact>().eq(CrmContact::getCustomerId, id));
        result.put("contacts", contacts);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchTag(List<Long> customerIds, List<Long> tagIds, String mode) {
        if ("overwrite".equals(mode)) {
            jdbcTemplate.update("DELETE FROM crm_customer_tag WHERE customer_id IN (" +
                    customerIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")");
        }
        for (Long customerId : customerIds) {
            for (Long tagId : tagIds) {
                try {
                    jdbcTemplate.update("INSERT INTO crm_customer_tag(customer_id, tag_id, tag_type) VALUES(?,?,?)",
                            customerId, tagId, "manual");
                } catch (Exception ignored) {
                }
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemoveTag(List<Long> customerIds, List<Long> tagIds) {
        for (Long customerId : customerIds) {
            for (Long tagId : tagIds) {
                jdbcTemplate.update("DELETE FROM crm_customer_tag WHERE customer_id = ? AND tag_id = ?",
                        customerId, tagId);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferOwner(Long id, Long newOwnerId, Boolean transferFollowUps) {
        CrmCustomer customer = getById(id);
        if (customer == null) throw new BizException(404, "客户不存在");
        return lambdaUpdate().eq(CrmCustomer::getId, id)
                .set(CrmCustomer::getOwnerId, newOwnerId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchChangeLevel(List<Long> customerIds, Long levelId, String reason) {
        for (Long id : customerIds) {
            CrmCustomer customer = getById(id);
            if (customer != null) {
                lambdaUpdate().eq(CrmCustomer::getId, id).set(CrmCustomer::getLevelId, levelId).update();
            }
        }
        return true;
    }
}
