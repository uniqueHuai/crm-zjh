package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.dto.LoginRequest;
import com.crm.system.dto.LoginResponse;
import com.crm.system.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {

    LoginResponse login(LoginRequest request);

    IPage<SysUser> selectPageWithDept(Page<SysUser> page, String keywords, Long deptId, Integer status);

    SysUser selectByUsername(String username);

    boolean createUser(SysUser user);

    boolean updateUser(SysUser user);

    boolean updateStatus(Long userId, Integer status);

    boolean resetPassword(Long userId, String newPassword);
}
