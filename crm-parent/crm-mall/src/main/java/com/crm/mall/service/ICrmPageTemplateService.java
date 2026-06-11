package com.crm.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmPageTemplate;

public interface ICrmPageTemplateService extends IService<CrmPageTemplate> {

    IPage<CrmPageTemplate> selectPageWithCondition(Page<CrmPageTemplate> page, String keywords, Integer status);

    boolean publish(Long id);
}
