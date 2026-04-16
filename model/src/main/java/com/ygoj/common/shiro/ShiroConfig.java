package com.ygoj.common.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Apache Shiro 配置类
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Configuration
public class ShiroConfig {

    @Autowired
    private JwtRealm jwtRealm;

    /**
     * 创建 SecurityManager
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm);
        // 禁用 session，使用无状态认证
        securityManager.setSessionManager(new org.apache.shiro.web.session.mgt.DefaultWebSessionManager());
        ((org.apache.shiro.web.session.mgt.DefaultWebSessionManager) securityManager.getSessionManager())
                .setSessionValidationSchedulerEnabled(false);
        return securityManager;
    }

    /**
     * 注册 JWT 过滤器
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtFilter());
        registration.addUrlPatterns("/*");
        registration.setName("jwtFilter");
        registration.setOrder(1);
        return registration;
    }

    /**
     * 配置 Shiro 过滤器链 (仅用于支持 Shiro 注解)
     */
    @Bean
    @ConditionalOnMissingBean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();
        filterFactoryBean.setSecurityManager(securityManager);

        // 配置过滤规则 - 全部放行,由 JwtFilter 处理认证
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/**", "anon");
        filterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return filterFactoryBean;
    }
}
