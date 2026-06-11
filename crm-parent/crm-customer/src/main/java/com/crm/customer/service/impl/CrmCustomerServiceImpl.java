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
    public CrmCustomer selectDetail(Long id) {
        CrmCustomer customer = getById(id);
        if (customer == null) throw new BizException(404, "客户不存在");

        List<CrmTag> tags = tagMapper.selectByCustomerId(id);
        customer.setTagIds(tags.stream().map(CrmTag::getId).collect(Collectors.toList()));

        return customer;
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
