package com.crm.framework.aspect;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在 Controller 方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 模块名 */
    String module() default "";

    /** 操作类型：create/update/delete/import/export */
    String action() default "";

    /** 业务描述 */
    String description() default "";

    /** 是否保存请求参数 */
    boolean saveParams() default true;
}
