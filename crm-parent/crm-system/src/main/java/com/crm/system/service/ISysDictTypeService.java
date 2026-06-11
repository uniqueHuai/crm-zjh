package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysDictItem;
import com.crm.system.entity.SysDictType;

import java.util.List;
import java.util.Map;

public interface ISysDictTypeService extends IService<SysDictType> {

    IPage<SysDictType> selectPageWithCondition(Page<SysDictType> page, String keywords);

    List<SysDictItem> selectItemsByTypeCode(String typeCode);

    boolean createDictItem(SysDictItem item);

    boolean updateDictItem(SysDictItem item);

    boolean removeDictItem(Long id);

    boolean hasItems(String typeCode);

    Map<String, List<SysDictItem>> selectDictByCodes(String[] codes);
}
