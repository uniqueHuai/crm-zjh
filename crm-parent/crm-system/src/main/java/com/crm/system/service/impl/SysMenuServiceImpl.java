package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.framework.security.SecurityUtils;
import com.crm.system.entity.SysMenu;
import com.crm.system.mapper.SysMenuMapper;
import com.crm.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> selectMenuTree() {
        List<SysMenu> allMenus = lambdaQuery()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder)
                .list();
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<SysMenu> selectRoutesByUserId(Long userId) {
        if (SecurityUtils.isAdmin()) {
            return selectMenuTree();
        }
        List<SysMenu> userMenus = menuMapper.selectVisibleMenuByUserId(userId);
        return buildTree(userMenus, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(SysMenu menu) {
        return save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysMenu menu) {
        return updateById(menu);
    }

    @Override
    public boolean hasChildren(Long menuId) {
        return lambdaQuery().eq(SysMenu::getParentId, menuId).count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        List<SysMenu> children = lambdaQuery().eq(SysMenu::getParentId, menuId).list();
        for (SysMenu child : children) {
            deleteMenu(child.getId());
        }
        removeById(menuId);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        List<SysMenu> children = menus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .collect(Collectors.toList());
        for (SysMenu menu : children) {
            List<SysMenu> childNodes = buildTree(menus, menu.getId());
            if (!childNodes.isEmpty()) {
                menu.setChildren(childNodes);
            } else {
                menu.setChildren(new ArrayList<>());
            }
            if ("C".equals(menu.getMenuType())) {
                List<SysMenu> buttons = menus.stream()
                        .filter(m -> "F".equals(m.getMenuType()) && menu.getId().equals(m.getParentId()))
                        .collect(Collectors.toList());
                menu.setButtons(buttons.isEmpty() ? null : buttons);
            }
            tree.add(menu);
        }
        return tree;
    }
}
