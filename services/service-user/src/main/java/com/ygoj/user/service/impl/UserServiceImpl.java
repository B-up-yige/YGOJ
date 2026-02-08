package com.ygoj.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ygoj.user.mapper.UserMapper;
import com.ygoj.user.pojo.User;
import com.ygoj.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

//    @Override
//    public User getUserById(Long id) {
//        User user = userMapper.selectById(id);
//        return user;
//    }
//
//    @Override
//    @Transactional
//    public void addcnt(Long userId) {
//        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("id", userId);
//        updateWrapper.setSql("cnt = cnt+1");
//        userMapper.update(updateWrapper);
//
//        int i = 10/0;
//    }
}
