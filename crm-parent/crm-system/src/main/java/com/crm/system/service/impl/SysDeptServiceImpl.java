package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.common.enums.ResultCode;
import com.crm.system.entity.SysDept;
import com.crm.system.mapper.SysDeptMapper;
import com.crm.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysDeptMapper deptMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SysDept> selectDeptTree() {
        List<SysDept> allDepts = deptMapper.selectAllDepts();
        return buildTree(allDepts, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDept(SysDept dept) {
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            SysDept parent = getById(dept.getParentId());
            if (parent == null) {
                throw new BizException(ResultCode.NOT_FOUND, "父级部门不存在");
            }
            dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
        } else {
            dept.setAncestors("0");
        }
        return save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(SysDept dept) {
        return updateById(dept);
    }

    @Override
    public boolean hasChildrenOrUser(Long deptId) {
        long childCount = lambdaQuery().eq(SysDept::getParentId, deptId).count();
        if (childCount > 0) {
            return true;
        }
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE dept_id = ? AND deleted_at IS NULL",
                Integer.class, deptId);
        return userCount != null && userCount > 0;
    }

    @Override
    public List<Long> selectChildDeptIds(Long deptId) {
        return deptMapper.selectChildDeptIds(deptId);
    }

    private List<SysDept> buildTree(List<SysDept> depts, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        List<SysDept> children = depts.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .collect(Collectors.toList());
        for (SysDept dept : children) {
            List<SysDept> childNodes = buildTree(depts, dept.getId());
            dept.setChildren(childNodes.isEmpty() ? new ArrayList<>() : childNodes);
            tree.add(dept);
        }
        return tree;
    }
}
