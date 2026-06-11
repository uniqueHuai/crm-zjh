package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.sales.entity.CrmProductCategory;
import com.crm.sales.mapper.CrmProductCategoryMapper;
import com.crm.sales.service.ICrmProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmProductCategoryServiceImpl extends ServiceImpl<CrmProductCategoryMapper, CrmProductCategory>
        implements ICrmProductCategoryService {

    @Override
    public List<CrmProductCategory> selectTree() {
        List<CrmProductCategory> all = lambdaQuery()
                .orderByAsc(CrmProductCategory::getSortOrder)
                .list();
        List<CrmProductCategory> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        for (CrmProductCategory root : roots) {
            root.setChildren(buildChildren(root.getId(), all));
        }
        return roots;
    }

    private List<CrmProductCategory> buildChildren(Long parentId, List<CrmProductCategory> all) {
        List<CrmProductCategory> children = new ArrayList<>();
        for (CrmProductCategory node : all) {
            if (parentId.equals(node.getParentId())) {
                node.setChildren(buildChildren(node.getId(), all));
                children.add(node);
            }
        }
        return children;
    }
}
