package com.ygoj.user.service;

import com.ygoj.common.Result;
import com.ygoj.user.pojo.Userinfo;
import org.apache.catalina.User;

public interface UserService {
//    User getUserById(Long id);
//
//    void addcnt(Long userId);
    Userinfo register(Userinfo userinfo);

    Userinfo getUserinfoByUsername(String username);
}
