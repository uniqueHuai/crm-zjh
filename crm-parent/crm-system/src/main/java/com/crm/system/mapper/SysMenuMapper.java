package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectVisibleMenuByUserId(@Param("userId") Long userId);

    List<SysMenu> selectMenuByUserId(@Param("userId") Long userId);

    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    List<String> selectAllPermissions();

    List<SysMenu> selectChildrenByParentId(@Param("parentId") Long parentId);
}
