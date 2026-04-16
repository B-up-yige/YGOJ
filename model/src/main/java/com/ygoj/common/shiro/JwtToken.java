package com.ygoj.common.shiro;

import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * JWT Token 封装类
 * 
 * @author xushangyi
 * @date 2026/04/16
 */
@Data
public class JwtToken implements AuthenticationToken {
    
    private final String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
