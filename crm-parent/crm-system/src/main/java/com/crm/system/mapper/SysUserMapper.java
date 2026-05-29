package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> selectPageWithDept(Page<SysUser> page, @Param("keywords") String keywords,
                                      @Param("deptId") Long deptId, @Param("status") Integer status);

    List<SysUser> selectByDeptId(@Param("deptId") Long deptId);

    SysUser selectByUsername(@Param("username") String username);
}
