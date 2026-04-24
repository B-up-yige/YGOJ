package com.ygoj.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 JWT 不需要）
            .csrf(csrf -> csrf.disable())
            
            // 设置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开接口 - 用户认证
                .requestMatchers(
                    "/user/login",
                    "/user/register",
                    "/user/userinfo/**"
                ).permitAll()
                
                // 公开接口 - 题目相关
                .requestMatchers(
                    "/problem/list",
                    "/problem/probleminfo/**",
                    "/problem/probleminfo/*/tag"
                ).permitAll()
                
                // 公开接口 - 比赛相关
                .requestMatchers(
                    "/contest/list",
                    "/contest/**"
                ).permitAll()
                
                // 公开接口 - 题集相关
                .requestMatchers(
                    "/problemset/list",
                    "/problemset/**"
                ).permitAll()
                
                // 公开接口 - 提交记录相关
                .requestMatchers(
                    "/record/list",
                    "/record/recordinfo/**",
                    "/record/statistics/**",
                    "/record/learning-curve/**",
                    "/record/contest-progress",
                    "/record/problemset-progress"
                ).permitAll()
                
                // 公开接口 - 讨论区相关
                .requestMatchers(
                    "/discuss/posts",
                    "/discuss/post/**",
                    "/discuss/comments/**",
                    "/discuss/category/list"
                ).permitAll()
                
                // 公开接口 - 文件下载(判题服务异步调用,无法传递 JWT)
                .requestMatchers("/file/download/**").permitAll()
                
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
