package com.crm.mall.mp;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Order(-5)
@RequiredArgsConstructor
public class MpAuthFilter implements Filter {

    private final MpJwtUtils mpJwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // only intercept /app/* paths
        if (path.contains("/app/")) {
            // skip auth endpoint and public pages
            if (path.contains("/app/auth/login")
                    || path.contains("/app/page-templates")
                    || path.contains("/app/products")
                    || path.contains("/app/coupons/available")
                    || path.contains("/app/activities")) {
                chain.doFilter(request, response);
                return;
            }

            String token = extractToken(req);
            if (!StringUtils.hasText(token) || !mpJwtUtils.validateToken(token)) {
                resp.setContentType("application/json;charset=utf-8");
                resp.setStatus(401);
                resp.getWriter().write("{\"code\":401,\"message\":\"mp-unauthorized\"}");
                return;
            }

            try {
                Long customerId = mpJwtUtils.getCustomerIdFromToken(token);
                if (customerId == null) {
                    resp.setContentType("application/json;charset=utf-8");
                    resp.setStatus(401);
                    resp.getWriter().write("{\"code\":401,\"message\":\"mp-unauthorized\"}");
                    return;
                }
                MpContext.setCustomerId(customerId);
                chain.doFilter(request, response);
            } finally {
                MpContext.clear();
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return request.getParameter("token");
    }
}
