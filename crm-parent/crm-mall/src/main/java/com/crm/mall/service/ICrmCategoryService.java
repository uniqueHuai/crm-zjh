package com.crm.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.mall.entity.CrmCategory;

import java.util.List;

public interface ICrmCategoryService extends IService<CrmCategory> {

    List<CrmCategory> selectTree();
}
