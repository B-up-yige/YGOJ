package com.ygoj.user.service;

import com.ygoj.common.Result;
import com.ygoj.user.pojo.Userinfo;
import org.apache.catalina.User;

public interface UserService {
    void register(Userinfo userinfo);
    Userinfo getUserinfoByUsername(String username);
    Userinfo login(String loginStr, String password);
}
