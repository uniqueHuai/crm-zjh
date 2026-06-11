package com.crm.system.service.impl;

import com.crm.common.constant.Constants;
import com.crm.framework.security.LoginUser;
import com.crm.system.entity.SysRole;
import com.crm.system.entity.SysUser;
import com.crm.system.mapper.SysMenuMapper;
import com.crm.system.mapper.SysRoleMapper;
import com.crm.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Service
@RequiredArgsConstructor
public class SysUserDetailsService implements UserDetailsService {

    private final ISysUserService userService;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectByUsername(username);
        if (user == null) {
            throw new InternalAuthenticationServiceException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new InternalAuthenticationServiceException("账户已被禁用");
        }

        List<SysRole> roles = roleMapper.selectByUserId(user.getId());
        Set<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toSet());

        Set<String> permissions = new HashSet<>();
        boolean isAdmin = roleCodes.contains("admin");
        if (isAdmin) {
            // Admin: load ALL permission codes directly (bypass role-menu mapping)
            List<String> dbPerms = menuMapper.selectAllPermissions();
            if (!dbPerms.isEmpty()) {
                for (String perm : dbPerms) {
                    // Keep the original permission (may be non-standard e.g. "ticket:assign")
                    permissions.add(perm);
                    // Expand base permission (e.g. "system:user:list") to include all CRUD variants
                    String prefix = perm.substring(0, perm.lastIndexOf(':'));
                    permissions.add(prefix + ":list");
                    permissions.add(prefix + ":query");
                    permissions.add(prefix + ":create");
                    permissions.add(prefix + ":edit");
                    permissions.add(prefix + ":delete");
                    permissions.add(prefix + ":import");
                    permissions.add(prefix + ":export");
                }
            }
        } else {
            permissions.addAll(menuMapper.selectPermissionsByUserId(user.getId()));
        }

        List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());
        int dataScope = roles.stream()
                .mapToInt(r -> r.getDataScope() != null ? r.getDataScope() : Constants.DATA_SCOPE_SELF)
                .min().orElse(Constants.DATA_SCOPE_SELF);

        String lastLoginAtStr = user.getLastLoginAt() != null
                ? user.getLastLoginAt().toString().replace('T', ' ')
                : null;

        return LoginUser.builder()
                .userId(user.getId())
                .deptId(user.getDeptId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .deptName(user.getDeptName())
                .lastLoginAt(lastLoginAtStr)
                .password(user.getPassword())
                .dataScope(dataScope)
                .roleIds(roleIds)
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }
}
