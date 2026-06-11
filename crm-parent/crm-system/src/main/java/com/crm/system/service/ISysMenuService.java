package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.entity.SysMenu;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    List<SysMenu> selectMenuTree();

    List<SysMenu> selectRoutesByUserId(Long userId);

    boolean createMenu(SysMenu menu);

    boolean updateMenu(SysMenu menu);

    boolean hasChildren(Long menuId);

    void deleteMenu(Long menuId);
}
