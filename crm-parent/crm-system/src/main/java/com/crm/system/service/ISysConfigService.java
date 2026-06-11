package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysConfig;

import java.util.List;

public interface ISysConfigService extends IService<SysConfig> {

    IPage<SysConfig> selectPageWithCondition(Page<SysConfig> page, String keywords);

    SysConfig selectByConfigKey(String configKey);

    List<SysConfig> selectPublicConfigs();

    boolean updateByConfigKey(String configKey, SysConfig config);

    boolean createConfig(SysConfig config);

    boolean removeConfig(Long id);

    boolean refreshCache();
}
