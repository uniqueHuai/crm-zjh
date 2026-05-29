package com.crm.system.service.impl;

import com.crm.common.constant.Constants;
import com.crm.framework.security.LoginUser;
import com.crm.system.entity.SysUser;
import com.crm.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Spring Security 用户加载 — 覆盖框架层默认实现
 */
@Service
@RequiredArgsConstructor
public class SysUserDetailsService implements UserDetailsService {

    private final ISysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new UsernameNotFoundException("账户已被禁用");
        }

        // TODO: 从数据库加载角色和权限
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        Set<String> permissions = new HashSet<>();
        permissions.add("*:*:*");

        return LoginUser.builder()
                .userId(user.getId())
                .deptId(user.getDeptId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .password(user.getPassword())
                .dataScope(Constants.DATA_SCOPE_ALL)
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
