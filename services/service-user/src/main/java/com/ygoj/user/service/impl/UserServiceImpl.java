package com.ygoj.user.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ygoj.user.mapper.UserinfoMapper;
import com.ygoj.user.Userinfo;
import com.ygoj.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        try {
            log.info("开始注册用户, username: {}, email: {}", userinfo.getUsername(), userinfo.getEmail());
            
            if (userinfo == null) {
                throw new IllegalArgumentException("用户信息不能为空");
            }
            
            //密码MD5加密存储
            userinfo.setPassword(DigestUtil.md5Hex(userinfo.getPassword()));

            userinfoMapper.insert(userinfo);
            log.info("用户注册成功, userId: {}", userinfo.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常, username: {}", userinfo.getUsername(), e);
            throw new RuntimeException("用户注册失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过用户名获取用户信息
     *
     * @param username 用户名
     * @return {@link Userinfo}
     */
    @Override
    public Userinfo getUserinfoByUsername(String username) {
        try {
            log.debug("通过用户名查询用户, username: {}", username);
            
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空");
            }
            
            LambdaQueryWrapper<Userinfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Userinfo::getUsername, username);
            Userinfo userinfo = userinfoMapper.selectOne(queryWrapper);

            if(userinfo == null){
                log.debug("用户不存在, username: {}", username);
                return null;
            }

            userinfo.setPassword(null);

            return userinfo;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("通过用户名查询用户异常, username: {}", username, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
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
        try {
            log.info("开始处理登录请求, loginStr: {}", loginStr);
            
            if (loginStr == null || loginStr.trim().isEmpty()) {
                throw new IllegalArgumentException("用户名或邮箱不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            
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
                log.info("登录验证成功, userId: {}", userinfo.getId());
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
                log.debug("生成token并缓存到Redis, userId: {}", userinfo.getId());

                userinfo.setPassword(token);
            } else {
                log.warn("登录验证失败, loginStr: {}", loginStr);
            }

            //登录失败返回null
            //成功返回userinfo
            return userinfo;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录异常, loginStr: {}", loginStr, e);
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 注销，直接在redis中删除令牌
     *
     * @param token 令牌
     */
    @Override
    public void logout(String token) {
        try {
            log.info("用户注销, token: {}", token);
            
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token不能为空");
            }
            
            redisTemplate.delete(token);
            log.debug("Token已从Redis删除");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注销异常", e);
            throw new RuntimeException("注销失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过id获取用户信息
     *
     * @param id id
     * @return {@link Userinfo}
     */
    @Override
    public Userinfo getUserinfoById(Long id) {
        try {
            log.debug("通过ID获取用户信息, userId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            Userinfo userinfo = userinfoMapper.selectById(id);
            if (userinfo != null) {
                userinfo.setPassword(null);
            }

            return userinfo;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("通过ID获取用户信息异常, userId: {}", id, e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过令牌获取用户id
     *
     * @param token 令牌
     * @return {@link Long}
     */
    @Override
    public Long getUserIdByToken(String token) {
        try {
            log.debug("通过token获取用户ID, token: {}", token);
            
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token不能为空");
            }
            
            String JWTstr = (String) redisTemplate.opsForValue().getAndExpire(token, 7, TimeUnit.DAYS);

            if(JWTstr == null || JWTstr.isBlank()){
                log.warn("Token无效或已过期");
                return null;
            }

            JWT jwt = JWTUtil.parseToken(JWTstr);
            Map<String, Object> payload = jwt.getPayloads();

            Long userId = ((Number) payload.get("userId")).longValue();
            log.debug("解析token成功, userId: {}", userId);
            return userId;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("通过token获取用户ID异常", e);
            throw new RuntimeException("获取用户ID失败: " + e.getMessage(), e);
        }
    }
}
