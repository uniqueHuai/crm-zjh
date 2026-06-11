package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.common.enums.ResultCode;
import com.crm.system.entity.SysDictItem;
import com.crm.system.entity.SysDictType;
import com.crm.system.mapper.SysDictItemMapper;
import com.crm.system.mapper.SysDictTypeMapper;
import com.crm.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;

    @Override
    public IPage<SysDictType> selectPageWithCondition(Page<SysDictType> page, String keywords) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .like(keywords != null, SysDictType::getTypeName, keywords)
                .or(keywords != null, w -> w.like(SysDictType::getTypeCode, keywords))
                .orderByAsc(SysDictType::getId);
        return page(page, wrapper);
    }

    @Override
    public List<SysDictItem> selectItemsByTypeCode(String typeCode) {
        return dictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getTypeCode, typeCode)
                        .orderByAsc(SysDictItem::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDictItem(SysDictItem item) {
        long count = dictItemMapper.selectCount(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getTypeCode, item.getTypeCode())
                        .eq(SysDictItem::getItemCode, item.getItemCode()));
        if (count > 0) {
            throw new BizException(400002, "字典项编码已存在");
        }
        return dictItemMapper.insert(item) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictItem(SysDictItem item) {
        return dictItemMapper.updateById(item) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDictItem(Long id) {
        return dictItemMapper.deleteById(id) > 0;
    }

    @Override
    public boolean hasItems(String typeCode) {
        return dictItemMapper.selectCount(
                new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getTypeCode, typeCode)) > 0;
    }

    @Override
    public Map<String, List<SysDictItem>> selectDictByCodes(String[] codes) {
        Map<String, List<SysDictItem>> result = new HashMap<>();
        Arrays.stream(codes).forEach(code -> {
            List<SysDictItem> items = dictItemMapper.selectList(
                    new LambdaQueryWrapper<SysDictItem>()
                            .eq(SysDictItem::getTypeCode, code)
                            .eq(SysDictItem::getStatus, 1)
                            .orderByAsc(SysDictItem::getSortOrder));
            result.put(code, items);
        });
        return result;
    }
}
