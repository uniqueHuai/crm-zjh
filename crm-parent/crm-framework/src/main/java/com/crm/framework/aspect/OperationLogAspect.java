package com.crm.framework.aspect;

import com.crm.common.constant.Constants;
import com.crm.framework.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 操作日志切面 — 记录 Controller 层请求日志
 * 使用 @OperationLog 注解标记需要记录日志的方法
 * TODO: 将日志异步写入 sys_operation_log 表
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Pointcut("@annotation(com.crm.framework.aspect.OperationLog)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[操作日志] {} | 耗时: {}ms | 参数: {}", methodName, duration,
                    Arrays.toString(joinPoint.getArgs()));
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[操作日志] {} | 耗时: {}ms | 异常: {}", methodName, duration, e.getMessage());
            throw e;
        }
    }
}
