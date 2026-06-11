package com.crm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.customer.entity.CrmSegment;
import com.crm.customer.entity.CrmSegmentMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CrmSegmentMapper extends BaseMapper<CrmSegment> {

    List<CrmSegmentMember> selectMembersBySegmentId(@Param("segmentId") Long segmentId);
}
