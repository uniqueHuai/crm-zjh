package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.constant.Constants;
import com.crm.system.entity.SysConfig;
import com.crm.system.mapper.SysConfigMapper;
import com.crm.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    private final SysConfigMapper configMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<SysConfig> selectPageWithCondition(Page<SysConfig> page, String keywords) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .like(keywords != null, SysConfig::getConfigName, keywords)
                .or(keywords != null, w -> w.like(SysConfig::getConfigKey, keywords))
                .orderByAsc(SysConfig::getId);
        return page(page, wrapper);
    }

    @Override
    public SysConfig selectByConfigKey(String configKey) {
        return lambdaQuery().eq(SysConfig::getConfigKey, configKey).one();
    }

    @Override
    public List<SysConfig> selectPublicConfigs() {
        return lambdaQuery().eq(SysConfig::getIsPublic, true).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByConfigKey(String configKey, SysConfig config) {
        boolean ok = lambdaUpdate()
                .eq(SysConfig::getConfigKey, configKey)
                .set(SysConfig::getConfigValue, config.getConfigValue())
                .set(config.getRemark() != null, SysConfig::getRemark, config.getRemark())
                .update();
        redisTemplate.delete(Constants.CACHE_CONFIG + configKey);
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfig(SysConfig config) {
        long count = lambdaQuery().eq(SysConfig::getConfigKey, config.getConfigKey()).count();
        if (count > 0) {
            throw new com.crm.common.exception.BizException(400002, "参数键名已存在");
        }
        config.setIsPublic(config.getIsPublic() != null && config.getIsPublic());
        return save(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeConfig(Long id) {
        SysConfig config = getById(id);
        if (config != null && config.getConfigType() != null && config.getConfigType() == 0) {
            throw new com.crm.common.exception.BizException(409002, "系统内置参数不允许删除");
        }
        redisTemplate.delete(Constants.CACHE_CONFIG + config.getConfigKey());
        return removeById(id);
    }

    @Override
    public boolean refreshCache() {
        List<SysConfig> configs = list();
        configs.forEach(c -> redisTemplate.opsForValue().set(Constants.CACHE_CONFIG + c.getConfigKey(), c.getConfigValue()));
        return true;
    }
}
