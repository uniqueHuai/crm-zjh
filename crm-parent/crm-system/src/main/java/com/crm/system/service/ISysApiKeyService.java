package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysApiKey;

import java.util.List;

public interface ISysApiKeyService extends IService<SysApiKey> {

    SysApiKey createApiKey(SysApiKey apiKey);

    SysApiKey regenerateSecret(Long id);

    boolean updateStatus(Long id, Integer status);

    List<SysApiKey> selectList();
}
