package com.crm.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmCoupon;
import com.crm.mall.entity.CrmCouponUser;

import java.util.List;

public interface ICrmCouponService extends IService<CrmCoupon> {

    IPage<CrmCoupon> selectPageWithCondition(Page<CrmCoupon> page, String keywords, Integer status);

    boolean distribute(Long couponId, List<Long> userIds);

    boolean receive(Long couponId, Long userId);

    IPage<CrmCouponUser> selectMyCoupons(Page<CrmCouponUser> page, Long userId);
}
