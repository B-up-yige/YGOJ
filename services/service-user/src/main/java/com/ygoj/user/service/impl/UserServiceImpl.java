package com.ygoj.user.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ygoj.user.mapper.UserinfoMapper;
import com.ygoj.user.Userinfo;
import com.ygoj.user.service.UserService;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.digest.DigestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserinfoMapper userinfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 注册新用户
     *
     * @param userinfo 用户信息
     */
    @Override
    @GlobalTransactional
    public void register(Userinfo userinfo){
        //密码MD5加密存储
        userinfo.setPassword(DigestUtil.md5Hex(userinfo.getPassword()));

        userinfoMapper.insert(userinfo);
    }

    /**
     * 通过用户名获取用户信息
     *
     * @param username 用户名
     * @return {@link Userinfo}
     */
    @Override
    public Userinfo getUserinfoByUsername(String username) {
        LambdaQueryWrapper<Userinfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Userinfo::getUsername, username);
        Userinfo userinfo = userinfoMapper.selectOne(queryWrapper);

        if(userinfo == null){
            return null;
        }

        userinfo.setPassword(null);

        return userinfo;
    }

    /**
     * 用户登录
     *
     * @param loginStr 登录str
     * @param password 密码
     * @return {@link Userinfo}
     */
    @Override
    public Userinfo login(String loginStr, String password) {
        //password进行md5加密
        password = DigestUtil.md5Hex(password);

        //验证账号密码
        LambdaQueryWrapper<Userinfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Userinfo::getPassword, password);

        //判断登录字段是邮箱还是用户名
        if(Validator.isEmail(loginStr)){
            wrapper.eq(Userinfo::getEmail, loginStr);
        }else{
            wrapper.eq(Userinfo::getUsername, loginStr);
        }

        Userinfo userinfo = null;
        userinfo = userinfoMapper.selectList(wrapper).stream().findFirst().orElse(null);

        //登陆成功后处理
        //在redis中创建token和对应的jwt缓存
        if(userinfo != null){
            String token = System.currentTimeMillis() + UUID.randomUUID().toString();
            String jwt;

            Map<String, Object>payload = new HashMap<>();
            payload.put("username", userinfo.getUsername());
            payload.put("userId", userinfo.getId());
            payload.put("email", userinfo.getEmail());
            //TODO:用户权限从数据库获取
            payload.put("permission", 1);

            //TODO：密钥从配置文件获取
            jwt = JWTUtil.createToken(payload, "tes".getBytes());

            //设置redis缓存
            redisTemplate.opsForValue().set(token, jwt, 7, TimeUnit.DAYS);

            userinfo.setPassword(token);
        }

        //登录失败返回null
        //成功返回userinfo
        return userinfo;
    }

    /**
     * 注销，直接在redis中删除令牌
     *
     * @param token 令牌
     */
    @Override
    public void logout(String token) {
        redisTemplate.delete(token);
    }

    /**
     * 通过id获取用户信息
     *
     * @param id id
     * @return {@link Userinfo}
     */
    @Override
    public Userinfo getUserinfoById(Long id) {
        Userinfo userinfo = userinfoMapper.selectById(id);
        userinfo.setPassword(null);

        return userinfo;
    }

    /**
     * 通过令牌获取用户id
     *
     * @param token 令牌
     * @return {@link Long}
     */
    @Override
    public Long getUserIdByToken(String token) {
        String JWTstr = (String) redisTemplate.opsForValue().getAndExpire(token, 7, TimeUnit.DAYS);

        if(JWTstr == null || JWTstr.isBlank()){
            return null;
        }

        JWT jwt = JWTUtil.parseToken(JWTstr);
        Map<String, Object> payload = jwt.getPayloads();

        return ((Number) payload.get("userId")).longValue();
    }
}
