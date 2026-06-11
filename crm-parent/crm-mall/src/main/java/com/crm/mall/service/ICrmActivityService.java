package com.crm.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmActivity;
import com.crm.mall.entity.CrmOrder;

import java.util.List;
import java.util.Map;

public interface ICrmActivityService extends IService<CrmActivity> {

    IPage<CrmActivity> selectPageWithCondition(Page<CrmActivity> page, String keywords, String type, Integer status);

    boolean updateStatus(Long id, Integer status);

    List<CrmActivity> selectActiveActivities(String type);

    List<Map<String, Object>> selectActivityProducts(Long activityId);

    Map<String, Object> selectActivityProductSku(Long activityId, Long skuId);

    CrmOrder createSeckillOrder(Long customerId, Long activityId, Long skuId, Integer quantity, Long addressId);

    CrmOrder createGroupBuyOrder(Long customerId, Long activityId, Long skuId, Long addressId, String groupId);
}
