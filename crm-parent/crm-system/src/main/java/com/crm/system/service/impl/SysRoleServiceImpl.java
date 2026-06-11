package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.system.entity.SysRole;
import com.crm.system.mapper.SysRoleMapper;
import com.crm.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<SysRole> selectPageWithCondition(Page<SysRole> page, String keywords, Integer status) {
        return roleMapper.selectPageWithCondition(page, keywords, status);
    }

    @Override
    public List<SysRole> selectByUserId(Long userId) {
        return roleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(SysRole role) {
        long count = lambdaQuery().eq(SysRole::getRoleCode, role.getRoleCode()).count();
        if (count > 0) {
            throw new BizException(400002, "角色编码已存在");
        }
        save(role);
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), role.getMenuIds());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        updateById(role);
        if (role.getMenuIds() != null) {
            assignMenus(role.getId(), role.getMenuIds());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                jdbcTemplate.update("INSERT INTO sys_role_menu(role_id, menu_id) VALUES(?, ?)", roleId, menuId);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignDataScope(Long roleId, Integer dataScope, List<Long> deptIds) {
        lambdaUpdate().eq(SysRole::getId, roleId)
                .set(SysRole::getDataScope, dataScope)
                .update();
        jdbcTemplate.update("DELETE FROM sys_role_dept WHERE role_id = ?", roleId);
        if (dataScope == 5 && deptIds != null && !deptIds.isEmpty()) {
            for (Long deptId : deptIds) {
                jdbcTemplate.update("INSERT INTO sys_role_dept(role_id, dept_id) VALUES(?, ?)", roleId, deptId);
            }
        }
        return true;
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }
}
