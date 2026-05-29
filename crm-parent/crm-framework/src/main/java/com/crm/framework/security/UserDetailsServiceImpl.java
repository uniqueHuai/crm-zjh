package com.crm.framework.security;

import com.crm.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户加载服务
 * 实际 UserDetailsService 由 crm-system 模块实现并通过 @Component 注册，
 * 此处为框架层的降级实现，防止启动报错。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new BizException(500, "UserDetailsService 未被正确实现，请检查 crm-system 模块");
    }
}
