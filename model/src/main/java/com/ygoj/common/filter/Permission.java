package com.ygoj.common.filter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 权限注解，配合权限拦截器 {@link AuthInterceptor} 使用
 * @author xushangyi
 * @date 2026/02/11 12:18
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    String value() default "";
    int auth() default 0;
}
