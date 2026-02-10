package com.ygoj.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ygoj.common.Result;
import com.ygoj.user.mapper.UserinfoMapper;
import com.ygoj.user.pojo.Userinfo;
import com.ygoj.user.service.UserService;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.digest.DigestUtil;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserinfoMapper userinfoMapper;

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

    /**
     * 注册新用户
     * @param userinfo
     * @return {@link Userinfo}
     */
    @Override
    @GlobalTransactional
    public Userinfo register(Userinfo userinfo){
        //密码MD5加密存储
        userinfo.setPassword(DigestUtil.md5Hex(userinfo.getPassword()));

        userinfoMapper.insert(userinfo);
        return userinfo;
    }

    @Override
    public Userinfo getUserinfoByUsername(String username) {
        LambdaQueryWrapper<Userinfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Userinfo::getUsername, username);
        Userinfo userinfo = userinfoMapper.selectOne(queryWrapper);

        return userinfo;
    }
}
