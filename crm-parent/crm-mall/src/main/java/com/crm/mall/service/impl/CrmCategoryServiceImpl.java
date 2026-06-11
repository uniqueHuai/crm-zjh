package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.mall.entity.CrmCategory;
import com.crm.mall.mapper.CrmCategoryMapper;
import com.crm.mall.service.ICrmCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmCategoryServiceImpl extends ServiceImpl<CrmCategoryMapper, CrmCategory> implements ICrmCategoryService {

    @Override
    public List<CrmCategory> selectTree() {
        List<CrmCategory> all = lambdaQuery()
                .orderByAsc(CrmCategory::getSortOrder)
                .list();
        List<CrmCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        for (CrmCategory root : roots) {
            root.setChildren(buildChildren(root.getId(), all));
        }
        return roots;
    }

    private List<CrmCategory> buildChildren(Long parentId, List<CrmCategory> all) {
        List<CrmCategory> children = new ArrayList<>();
        for (CrmCategory node : all) {
            if (parentId.equals(node.getParentId())) {
                node.setChildren(buildChildren(node.getId(), all));
                children.add(node);
            }
        }
        return children;
    }
}
