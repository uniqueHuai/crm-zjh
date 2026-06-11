package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.entity.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper extends BaseMapper<SysDept> {

    List<SysDept> selectAllDepts();

    List<SysDept> selectByParentId(@Param("parentId") Long parentId);

    List<Long> selectChildDeptIds(@Param("deptId") Long deptId);

    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
}
