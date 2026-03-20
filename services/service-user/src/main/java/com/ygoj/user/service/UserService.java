package com.ygoj.user.service;

import com.ygoj.user.Userinfo;

import java.util.Map;

public interface UserService {
    void register(Userinfo userinfo);
    Userinfo getUserinfoByUsername(String username);
    Userinfo login(String loginStr, String password);

    void logout(String token);

    Userinfo getUserinfoById(Long id);

    Long getUserIdByToken(String token);
}
