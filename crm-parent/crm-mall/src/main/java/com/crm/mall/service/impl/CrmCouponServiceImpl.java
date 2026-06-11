package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.mall.entity.CrmCoupon;
import com.crm.mall.entity.CrmCouponUser;
import com.crm.mall.mapper.CrmCouponMapper;
import com.crm.mall.mapper.CrmCouponUserMapper;
import com.crm.mall.service.ICrmCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmCouponServiceImpl extends ServiceImpl<CrmCouponMapper, CrmCoupon> implements ICrmCouponService {

    private final CrmCouponMapper couponMapper;
    private final CrmCouponUserMapper couponUserMapper;

    @Override
    public IPage<CrmCoupon> selectPageWithCondition(Page<CrmCoupon> page, String keywords, Integer status) {
        return couponMapper.selectPage(page, new LambdaQueryWrapper<CrmCoupon>()
                .like(keywords != null && !keywords.isEmpty(), CrmCoupon::getName, keywords)
                .eq(status != null, CrmCoupon::getStatus, status)
                .orderByDesc(CrmCoupon::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean distribute(Long couponId, List<Long> userIds) {
        CrmCoupon coupon = getById(couponId);
        if (coupon == null) throw new BizException(404, "优惠券不存在");
        for (Long userId : userIds) {
            CrmCouponUser cu = new CrmCouponUser();
            cu.setCouponId(couponId);
            cu.setUserId(userId);
            cu.setStatus("unused");
            cu.setReceivedAt(LocalDateTime.now());
            couponUserMapper.insert(cu);
        }
        lambdaUpdate().eq(CrmCoupon::getId, couponId)
                .setSql("received_count = received_count + " + userIds.size())
                .update();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receive(Long couponId, Long userId) {
        CrmCoupon coupon = getById(couponId);
        if (coupon == null) throw new BizException(404, "优惠券不存在");
        if (coupon.getReceivedCount() != null && coupon.getTotalCount() != null
                && coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BizException(400, "优惠券已领完");
        }
        if (coupon.getPerLimit() != null && coupon.getPerLimit() > 0) {
            long count = couponUserMapper.selectCount(
                    new LambdaQueryWrapper<CrmCouponUser>()
                            .eq(CrmCouponUser::getCouponId, couponId)
                            .eq(CrmCouponUser::getUserId, userId));
            if (count >= coupon.getPerLimit()) {
                throw new BizException(400, "已达到领取上限");
            }
        }
        CrmCouponUser cu = new CrmCouponUser();
        cu.setCouponId(couponId);
        cu.setUserId(userId);
        cu.setStatus("unused");
        cu.setReceivedAt(LocalDateTime.now());
        couponUserMapper.insert(cu);
        lambdaUpdate().eq(CrmCoupon::getId, couponId)
                .setSql("received_count = received_count + 1")
                .update();
        return true;
    }

    @Override
    public IPage<CrmCouponUser> selectMyCoupons(Page<CrmCouponUser> page, Long userId) {
        return couponUserMapper.selectPage(page,
                new LambdaQueryWrapper<CrmCouponUser>()
                        .eq(CrmCouponUser::getUserId, userId)
                        .orderByDesc(CrmCouponUser::getReceivedAt));
    }
}
