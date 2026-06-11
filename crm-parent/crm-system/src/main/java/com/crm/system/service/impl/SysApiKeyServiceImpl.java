package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.system.entity.SysApiKey;
import com.crm.system.mapper.SysApiKeyMapper;
import com.crm.system.service.ISysApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysApiKeyServiceImpl extends ServiceImpl<SysApiKeyMapper, SysApiKey> implements ISysApiKeyService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysApiKey createApiKey(SysApiKey apiKey) {
        apiKey.setApiKey("ak_" + UUID.randomUUID().toString().replace("-", ""));
        apiKey.setApiSecret("sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        save(apiKey);
        return apiKey;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysApiKey regenerateSecret(Long id) {
        SysApiKey apiKey = getById(id);
        if (apiKey == null) {
            throw new BizException(404, "API密钥不存在");
        }
        apiKey.setApiSecret("sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        updateById(apiKey);
        return apiKey;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        return lambdaUpdate().eq(SysApiKey::getId, id).set(SysApiKey::getStatus, status).update();
    }

    @Override
    public List<SysApiKey> selectList() {
        return lambdaQuery().orderByDesc(SysApiKey::getCreatedAt).list();
    }
}
