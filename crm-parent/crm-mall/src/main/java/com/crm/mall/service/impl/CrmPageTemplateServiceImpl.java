package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.mall.entity.CrmPageTemplate;
import com.crm.mall.mapper.CrmPageTemplateMapper;
import com.crm.mall.service.ICrmPageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmPageTemplateServiceImpl extends ServiceImpl<CrmPageTemplateMapper, CrmPageTemplate> implements ICrmPageTemplateService {

    @Override
    public IPage<CrmPageTemplate> selectPageWithCondition(Page<CrmPageTemplate> page, String keywords, Integer status) {
        return baseMapper.selectPage(page, new LambdaQueryWrapper<CrmPageTemplate>()
                .like(keywords != null && !keywords.isEmpty(), CrmPageTemplate::getName, keywords)
                .eq(status != null, CrmPageTemplate::getStatus, status)
                .orderByDesc(CrmPageTemplate::getUpdatedAt));
    }

    @Override
    public boolean publish(Long id) {
        CrmPageTemplate template = getById(id);
        if (template == null) {
            throw new BizException(404, "页面模板不存在");
        }
        return lambdaUpdate().eq(CrmPageTemplate::getId, id).set(CrmPageTemplate::getStatus, 1).update();
    }
}
