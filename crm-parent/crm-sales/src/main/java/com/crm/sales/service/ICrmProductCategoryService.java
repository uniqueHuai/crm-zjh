package com.crm.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.CrmProductCategory;

import java.util.List;

public interface ICrmProductCategoryService extends IService<CrmProductCategory> {

    List<CrmProductCategory> selectTree();
}
