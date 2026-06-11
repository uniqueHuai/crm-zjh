package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysRole;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {

    IPage<SysRole> selectPageWithCondition(Page<SysRole> page, String keywords, Integer status);

    List<SysRole> selectByUserId(Long userId);

    boolean createRole(SysRole role);

    boolean updateRole(SysRole role);

    boolean assignMenus(Long roleId, List<Long> menuIds);

    boolean assignDataScope(Long roleId, Integer dataScope, List<Long> deptIds);

    List<Long> selectMenuIdsByRoleId(Long roleId);
}
