package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.constant.Constants;
import com.crm.common.exception.BizException;
import com.crm.framework.security.JwtUtils;
import com.crm.framework.security.LoginUser;
import com.crm.framework.security.SecurityUtils;
import com.crm.system.dto.LoginRequest;
import com.crm.system.dto.LoginResponse;
import com.crm.system.entity.SysUser;
import com.crm.system.mapper.SysUserMapper;
import com.crm.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserMapper userMapper;
    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 验证码校验（略，根据配置决定是否开启）

        // 2. Spring Security 认证
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        LoginUser loginUser = (LoginUser) auth.getPrincipal();

        // 3. 生成 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", loginUser.getUserId());
        claims.put("username", loginUser.getUsername());
        String token = jwtUtils.createToken(claims);
        String refreshToken = jwtUtils.createRefreshToken(loginUser.getUserId());

        // 5. 更新最后登录时间
        String nowStr = java.time.LocalDateTime.now().toString().replace('T', ' ');
        loginUser.setLastLoginAt(nowStr);
        lambdaUpdate()
                .eq(SysUser::getId, loginUser.getUserId())
                .set(SysUser::getLastLoginAt, java.time.LocalDateTime.now())
                .update();

        // 4. 缓存登录用户（在更新 lastLoginAt 之后缓存，确保缓存是最新值）
        String cacheKey = Constants.CACHE_TOKEN + token.hashCode();
        redisTemplate.opsForValue().set(cacheKey, loginUser, jwtUtils.getExpire(), TimeUnit.SECONDS);

        return LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getExpire())
                .tokenType("Bearer")
                .userInfo(LoginResponse.UserInfo.builder()
                        .userId(loginUser.getUserId())
                        .username(loginUser.getUsername())
                        .realName(loginUser.getRealName())
                        .avatar(loginUser.getAvatar())
                        .phone(loginUser.getPhone())
                        .email(loginUser.getEmail())
                        .deptId(loginUser.getDeptId())
                        .deptName(loginUser.getDeptName())
                        .lastLoginAt(nowStr)
                        .roles(loginUser.getRoles().stream().toList())
                        .permissions(loginUser.getPermissions())
                        .build())
                .build();
    }

    @Override
    public IPage<SysUser> selectPageWithDept(Page<SysUser> page, String keywords, Long deptId, Integer status) {
        return userMapper.selectPageWithDept(page, keywords, deptId, status);
    }

    @Override
    public SysUser selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BizException(400001, "密码不能为空");
        }
        long count = lambdaQuery().eq(SysUser::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new BizException(400002, "用户名已存在");
        }
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        return lambdaUpdate().eq(SysUser::getId, userId).set(SysUser::getStatus, status).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        return lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, SecurityUtils.encryptPassword(newPassword))
                .update();
    }
}
