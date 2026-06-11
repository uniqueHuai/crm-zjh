package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.customer.entity.CrmCustomer;
import com.crm.customer.entity.CrmSegment;
import com.crm.customer.entity.CrmSegmentMember;
import com.crm.customer.mapper.CrmCustomerMapper;
import com.crm.customer.mapper.CrmSegmentMapper;
import com.crm.customer.service.ICrmSegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmSegmentServiceImpl extends ServiceImpl<CrmSegmentMapper, CrmSegment> implements ICrmSegmentService {

    private final CrmSegmentMapper segmentMapper;
    private final CrmCustomerMapper customerMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refreshMembers(Long segmentId) {
        CrmSegment segment = getById(segmentId);
        if (segment == null) throw new RuntimeException("分群不存在");

        jdbcTemplate.update("DELETE FROM crm_segment_member WHERE segment_id = ? AND join_type = 'auto'", segmentId);

        if (Boolean.TRUE.equals(segment.getIsDynamic())) {
            // TODO: Execute segment conditions and insert matching customers
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crm_customer WHERE deleted_at IS NULL",
                    Integer.class);
            lambdaUpdate().eq(CrmSegment::getId, segmentId).set(CrmSegment::getMemberCount, count).update();
        }
        return true;
    }

    @Override
    public IPage<CrmCustomer> selectMembers(Page<CrmCustomer> page, Long segmentId) {
        return customerMapper.selectPageBySegment(page, segmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manuallyAdjustMembers(Long segmentId, List<Long> customerIds, String action) {
        if ("add".equals(action)) {
            for (Long customerId : customerIds) {
                try {
                    jdbcTemplate.update("INSERT INTO crm_segment_member(segment_id, customer_id, join_type) VALUES(?,?,?)",
                            segmentId, customerId, "manual");
                } catch (Exception ignored) {
                }
            }
        } else if ("remove".equals(action)) {
            for (Long customerId : customerIds) {
                jdbcTemplate.update("DELETE FROM crm_segment_member WHERE segment_id = ? AND customer_id = ? AND join_type = 'manual'",
                        segmentId, customerId);
            }
        }
        return true;
    }

    @Override
    public boolean createCampaign(Long segmentId, Map<String, Object> params) {
        // TODO: create marketing campaign for segment
        return true;
    }
}
