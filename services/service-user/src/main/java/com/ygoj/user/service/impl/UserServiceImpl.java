package com.ygoj.user.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.common.Result;
import com.ygoj.common.security.JwtUtils;
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
    @Autowired
    private JwtUtils jwtUtils;

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
            
            // 设置默认角色为 USER
            if (userinfo.getRole() == null || userinfo.getRole().trim().isEmpty()) {
                userinfo.setRole("USER");
            }
            
            // 设置默认权限值: 5731
            // 包含权限: 查看题目(0), 提交代码(1), 查看提交记录(5), 查看排行榜(6), 
            //          参加比赛(9), 创建题集(10), 查看题集(12)
            // 计算: 2^0 + 2^1 + 2^5 + 2^6 + 2^9 + 2^10 + 2^12 = 1 + 2 + 32 + 64 + 512 + 1024 + 4096 = 5731
            if (userinfo.getPermission() == null) {
                userinfo.setPermission(5731L);
            }
            
            // 设置默认为未拉黑状态
            if (userinfo.getIsBanned() == null) {
                userinfo.setIsBanned(0);
            }
            
            //密码MD5加密存储
            userinfo.setPassword(DigestUtil.md5Hex(userinfo.getPassword()));

            userinfoMapper.insert(userinfo);
            log.info("用户注册成功, userId: {}, role: {}, permission: {}", 
                userinfo.getId(), userinfo.getRole(), userinfo.getPermission());
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
                // 检查用户是否被拉黑
                if (userinfo.getIsBanned() != null && userinfo.getIsBanned() == 1) {
                    log.warn("用户已被拉黑，禁止登录, userId: {}", userinfo.getId());
                    // 返回特殊标记，让Controller层返回明确的错误信息
                    userinfo.setPassword("BANNED");
                    return userinfo;
                }
                
                log.info("登录验证成功, userId: {}", userinfo.getId());
                String token = System.currentTimeMillis() + UUID.randomUUID().toString();
                
                // 从数据库获取用户角色和权限
                String role = userinfo.getRole() != null ? userinfo.getRole() : "USER";
                Long permission = userinfo.getPermission() != null ? userinfo.getPermission() : 1L;
                
                // 使用 JwtUtils 生成 JWT
                String jwt = jwtUtils.generateToken(
                    userinfo.getId(),
                    userinfo.getUsername(),
                    role,
                    permission
                );

                //设置redis缓存
                redisTemplate.opsForValue().set(token, jwt, 7, TimeUnit.DAYS);
                log.debug("生成token并缓存到Redis, userId: {}, role: {}, permission: {}", 
                    userinfo.getId(), role, permission);

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
            
            // 从 Redis 中获取 JWT token
            Object jwtValue = redisTemplate.opsForValue().get(token);
            if (jwtValue == null) {
                log.warn("Token在Redis中不存在或已过期");
                return null;
            }
            
            String jwt = jwtValue.toString();
            
            // 使用 JwtUtils 解析 JWT token
            Long userId = jwtUtils.getUserIdFromToken(jwt);
            
            if (userId == null) {
                log.warn("JWT Token无效或已过期");
                return null;
            }
            
            // 刷新 Token 过期时间
            if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
                redisTemplate.expire(token, 7, TimeUnit.DAYS);
            }
            
            log.debug("解析token成功, userId: {}", userId);
            return userId;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("通过token获取用户ID异常", e);
            throw new RuntimeException("获取用户ID失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有用户列表（分页）
     */
    @Override
    public Result getAllUsers(Long page, Long pageSize) {
        try {
            log.info("获取用户列表, page: {}, pageSize: {}", page, pageSize);
            
            Page<Userinfo> pageInfo = new Page<>(page, pageSize);
            LambdaQueryWrapper<Userinfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Userinfo::getId);
            
            Page<Userinfo> result = userinfoMapper.selectPage(pageInfo, wrapper);
            
            // 清除密码信息
            result.getRecords().forEach(user -> user.setPassword(null));
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("page", result.getCurrent());
            data.put("pageSize", result.getSize());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户列表异常", e);
            return Result.error(500, "获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户权限
     */
    @Override
    public Result updateUserPermission(Long userId, String role, Long permission) {
        try {
            log.info("更新用户权限, userId: {}, role: {}, permission: {}", userId, role, permission);
            
            Userinfo user = userinfoMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            user.setRole(role);
            user.setPermission(permission);
            userinfoMapper.updateById(user);
            
            log.info("用户权限更新成功, userId: {}", userId);
            return Result.success("权限更新成功");
        } catch (Exception e) {
            log.error("更新用户权限异常, userId: {}", userId, e);
            return Result.error(500, "更新用户权限失败: " + e.getMessage());
        }
    }

    /**
     * 拉黑/解禁用户
     */
    @Override
    public Result banUser(Long userId, Integer isBanned) {
        try {
            log.info("拉黑/解禁用户, userId: {}, isBanned: {}", userId, isBanned);
            
            Userinfo user = userinfoMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 不允许拉黑管理员和超级管理员账号
            if ("ADMIN".equals(user.getRole())) {
                return Result.error(403, "不能拉黑管理员账号");
            }
            if ("SUPER_ADMIN".equals(user.getRole())) {
                return Result.error(403, "不能拉黑超级管理员账号");
            }
            
            // 更新拉黑状态
            user.setIsBanned(isBanned != null ? isBanned : 0);
            userinfoMapper.updateById(user);
            
            String action = (isBanned != null && isBanned == 1) ? "拉黑" : "解禁";
            log.info("用户{}成功, userId: {}", action, userId);
            return Result.success("用户" + action + "成功");
        } catch (Exception e) {
            log.error("拉黑/解禁用户异常, userId: {}", userId, e);
            return Result.error(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息（昵称、邮箱）
     */
    @Override
    public Result updateUserInfo(Long userId, String nickname, String email) {
        try {
            log.info("更新用户信息, userId: {}, nickname: {}, email: {}", userId, nickname, email);
            
            Userinfo user = userinfoMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 验证昵称长度
            if (nickname != null && !nickname.trim().isEmpty()) {
                int len = nickname.length();
                if (len < 3 || len > 20) {
                    return Result.error(400, "昵称长度必须在3-20位之间");
                }
                user.setNickname(nickname);
            }
            
            // 验证邮箱格式
            if (email != null && !email.trim().isEmpty()) {
                if (!cn.hutool.core.lang.Validator.isEmail(email)) {
                    return Result.error(400, "邮箱格式不合法");
                }
                
                // 检查邮箱是否已被其他用户使用
                LambdaQueryWrapper<Userinfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Userinfo::getEmail, email)
                       .ne(Userinfo::getId, userId);
                Userinfo existingUser = userinfoMapper.selectOne(wrapper);
                if (existingUser != null) {
                    return Result.error(400, "邮箱已被使用");
                }
                
                user.setEmail(email);
            }
            
            userinfoMapper.updateById(user);
            log.info("用户信息更新成功, userId: {}", userId);
            return Result.success("用户信息更新成功");
        } catch (Exception e) {
            log.error("更新用户信息异常, userId: {}", userId, e);
            return Result.error(500, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @Override
    public Result changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            log.info("修改密码请求, userId: {}", userId);
            
            Userinfo user = userinfoMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 验证旧密码
            String encryptedOldPassword = DigestUtil.md5Hex(oldPassword);
            if (!encryptedOldPassword.equals(user.getPassword())) {
                return Result.error(400, "原密码错误");
            }
            
            // 验证新密码格式
            if (!cn.hutool.core.lang.Validator.isMatchRegex("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$", newPassword)) {
                return Result.error(400, "密码只能由字母、数字和特殊符号组成");
            }
            
            // 加密新密码
            String encryptedNewPassword = DigestUtil.md5Hex(newPassword);
            user.setPassword(encryptedNewPassword);
            userinfoMapper.updateById(user);
            
            log.info("密码修改成功, userId: {}", userId);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码异常, userId: {}", userId, e);
            return Result.error(500, "修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 管理员重置用户密码
     */
    @Override
    public Result resetUserPassword(Long userId, String newPassword) {
        try {
            log.info("管理员重置用户密码, userId: {}", userId);
            
            Userinfo user = userinfoMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 不允许重置超级管理员密码
            if ("SUPER_ADMIN".equals(user.getRole())) {
                return Result.error(403, "不能重置超级管理员的密码");
            }
            
            // 验证新密码格式
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error(400, "新密码不能为空");
            }
            if (!cn.hutool.core.lang.Validator.isMatchRegex("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$", newPassword)) {
                return Result.error(400, "密码只能由字母、数字和特殊符号组成");
            }
            
            // 加密新密码
            String encryptedNewPassword = DigestUtil.md5Hex(newPassword);
            user.setPassword(encryptedNewPassword);
            userinfoMapper.updateById(user);
            
            log.info("用户密码重置成功, userId: {}", userId);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置用户密码异常, userId: {}", userId, e);
            return Result.error(500, "重置密码失败: " + e.getMessage());
        }
    }
}
