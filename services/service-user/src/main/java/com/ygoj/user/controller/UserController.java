package com.ygoj.user.controller;

import cn.hutool.core.lang.Validator;
import com.ygoj.common.Result;
import com.ygoj.user.Userinfo;
import com.ygoj.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户注册接口(公开访问)
     * @param userinfo 注册用户信息
     * @return {@link Result}
     */
    @PostMapping("/register")
    public Result register(@RequestBody Userinfo userinfo) {
        try {
            log.info("用户注册请求, username: {}, email: {}", userinfo.getUsername(), userinfo.getEmail());
            
            // 参数校验
            if (userinfo == null) {
                return Result.error(400, "用户信息不能为空");
            }
            if (userinfo.getUsername() == null || userinfo.getUsername().trim().isEmpty()) {
                return Result.error(400, "用户名不能为空");
            }
            if (userinfo.getPassword() == null || userinfo.getPassword().trim().isEmpty()) {
                return Result.error(400, "密码不能为空");
            }
            if (userinfo.getEmail() == null || userinfo.getEmail().trim().isEmpty()) {
                return Result.error(400, "邮箱不能为空");
            }
            if (userinfo.getNickname() == null || userinfo.getNickname().trim().isEmpty()) {
                return Result.error(400, "昵称不能为空");
            }
            
            //数据检验

            //用户名是否合法
            if(!Validator.isGeneral(userinfo.getUsername())){
                log.warn("用户名不合法: {}", userinfo.getUsername());
                return Result.error(400, "用户名只能由字母、数字和下划线组成");
            }
            int len = userinfo.getUsername().length();
            if(3 > len || len > 20) {
                log.warn("用户名长度不合法: {}, length: {}", userinfo.getUsername(), len);
                return Result.error(400, "用户名的长度要大于3小于20");
            }

            //用户名是否重复
            if(userService.getUserinfoByUsername(userinfo.getUsername()) != null){
                log.warn("用户名已存在: {}", userinfo.getUsername());
                return Result.error(400, "用户名重复");
            }

            //用户昵称是否合法
            len = userinfo.getNickname().length();
            if(3 > len || len > 20) {
                log.warn("用户昵称长度不合法: {}, length: {}", userinfo.getNickname(), len);
                return Result.error(400, "用户昵称的长度要大于3小于20");
            }

            //密码是否合法
            if(!Validator.isMatchRegex("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$", userinfo.getPassword())) {
                log.warn("密码格式不合法");
                return Result.error(400, "密码只能由字母、数字和特殊符号组成");
            }

            //检查邮箱格式是否合法
            if(!Validator.isEmail(userinfo.getEmail())) {
                log.warn("邮箱格式不合法: {}", userinfo.getEmail());
                return Result.error(400, "邮箱不合法");
            }

            userService.register(userinfo);
            log.info("用户注册成功, username: {}", userinfo.getUsername());
            return Result.success();
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error(500, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 登录功能接口(公开访问)
     *
     * @param loginStr 登录str(用户名称或邮箱)
     * @param password 密码
     * @return {@link Result}
     */
    @PostMapping("/login")
    public Result login(String loginStr, String password) {
        try {
            log.info("用户登录请求, loginStr: {}", loginStr);
            
            // 参数校验
            if (loginStr == null || loginStr.trim().isEmpty()) {
                return Result.error(400, "用户名或邮箱不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error(400, "密码不能为空");
            }
            
            //登录功能
            Userinfo userinfo = userService.login(loginStr, password);

            if(userinfo == null){
                log.warn("登录失败, loginStr: {}", loginStr);
                return Result.error(400, "账号或密码错误");
            }
            
            // 检查是否是被拉黑的特殊标记
            if ("BANNED".equals(userinfo.getPassword())) {
                log.warn("用户已被拉黑，禁止登录, userId: {}", userinfo.getId());
                return Result.error(403, "你的账号已被拉黑");
            }
            
            log.info("用户登录成功, userId: {}", userinfo.getId());
            return Result.success(userinfo.getPassword());
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 注销(需要登录)
     *
     * @param request 请求
     * @return {@link Result}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            log.info("用户注销请求, token: {}", token);
            
            if (token == null || token.trim().isEmpty()) {
                return Result.error(400, "Token不能为空");
            }
            
            userService.logout(token);
            log.info("用户注销成功");
            return Result.success();
        } catch (Exception e) {
            log.error("用户注销失败", e);
            return Result.error(500, "注销失败: " + e.getMessage());
        }
    }

    /**
     * 根据id获取用户信息(需要登录)
     *
     * @param id 用户id
     * @return {@link Result}
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/userinfo/{id}")
    public Result userinfo(@PathVariable("id") Long id) {
        try {
            log.info("获取用户信息请求, userId: {}", id);
            
            if (id == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            Userinfo userinfo = userService.getUserinfoById(id);
            if (userinfo == null) {
                log.warn("用户不存在, userId: {}", id);
                return Result.error(404, "用户不存在");
            }
            
            log.info("获取用户信息成功, userId: {}", id);
            return Result.success(userinfo);
        } catch (Exception e) {
            log.error("获取用户信息失败, userId: {}", id, e);
            return Result.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 通过token获取用户ID(需要登录)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/userinfo")
    public Result getUserIdByToken(@RequestBody Map<String, Object> json) {
        try {
            String token = (String) json.get("token");
            log.debug("通过token获取用户ID请求");
            
            if (token == null || token.trim().isEmpty()) {
                return Result.error(400, "Token不能为空");
            }
            
            Long userId = userService.getUserIdByToken(token);
            if (userId == null) {
                return Result.error(401, "Token无效或已过期");
            }
            
            log.debug("通过token获取用户ID成功, userId: {}", userId);
            return Result.success(userId);
        } catch (Exception e) {
            log.error("通过token获取用户ID失败", e);
            return Result.error(500, "获取用户ID失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户列表(仅管理员 - 需要用户管理权限)
     */
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @GetMapping("/admin/users")
    public Result getAllUsers(@RequestParam(defaultValue = "1") Long page,
                              @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            log.info("管理员获取用户列表请求, page: {}, pageSize: {}", page, pageSize);
            return userService.getAllUsers(page, pageSize);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error(500, "获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户权限(仅超级管理员 - 需要用户管理权限)
     */
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/admin/user/permission")
    public Result updateUserPermission(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String role = (String) params.get("role");
            Long permission = Long.valueOf(params.get("permission").toString());
            
            // 获取当前用户ID
            Long currentUserId = com.ygoj.common.security.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            
            // 检查当前用户是否为超级管理员
            com.ygoj.user.Userinfo currentUser = userService.getUserinfoById(currentUserId);
            if (currentUser == null || !"SUPER_ADMIN".equals(currentUser.getRole())) {
                log.warn("非超级管理员尝试修改用户权限, currentUserId: {}", currentUserId);
                return Result.error(403, "只有超级管理员才能修改用户权限和角色");
            }
            
            // 检查是否尝试修改自己的权限
            if (currentUserId.equals(userId)) {
                log.warn("超级管理员尝试修改自己的权限, userId: {}", currentUserId);
                return Result.error(403, "超级管理员不能修改自己的权限和角色");
            }
            
            log.info("超级管理员更新用户权限请求, currentUserId: {}, targetUserId: {}, role: {}, permission: {}", 
                currentUserId, userId, role, permission);
            
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            if (role == null || role.trim().isEmpty()) {
                return Result.error(400, "角色不能为空");
            }
            if (permission == null) {
                return Result.error(400, "权限值不能为空");
            }
            
            return userService.updateUserPermission(userId, role, permission);
        } catch (Exception e) {
            log.error("更新用户权限失败", e);
            return Result.error(500, "更新用户权限失败: " + e.getMessage());
        }
    }

    /**
     * 拉黑/解禁用户(仅管理员 - 需要用户管理权限)
     */
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    @PutMapping("/admin/user/ban/{id}")
    public Result banUser(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            log.info("管理员拉黑/解禁用户请求, userId: {}", id);
            
            if (id == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            Integer isBanned = params.get("isBanned") != null ? 
                Integer.valueOf(params.get("isBanned").toString()) : 0;
            
            return userService.banUser(id, isBanned);
        } catch (Exception e) {
            log.error("拉黑/解禁用户失败, userId: {}", id, e);
            return Result.error(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息（昵称、邮箱）(需要登录)
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/userinfo")
    public Result updateUserInfo(@RequestBody Map<String, Object> params) {
        try {
            // 从 SecurityContext 获取当前用户 ID
            Long userId = com.ygoj.common.security.SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            
            String nickname = (String) params.get("nickname");
            String email = (String) params.get("email");
            
            log.info("更新用户信息请求, userId: {}, nickname: {}, email: {}", userId, nickname, email);
            
            return userService.updateUserInfo(userId, nickname, email);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error(500, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码（需要登录）
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/password")
    public Result changePassword(@RequestBody Map<String, Object> params) {
        try {
            // 从 SecurityContext 获取当前用户 ID
            Long userId = com.ygoj.common.security.SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "未登录或登录已过期");
            }
            
            String oldPassword = (String) params.get("oldPassword");
            String newPassword = (String) params.get("newPassword");
            
            log.info("修改密码请求, userId: {}", userId);
            
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error(400, "原密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error(400, "新密码不能为空");
            }
            
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(500, "修改密码失败: " + e.getMessage());
        }
    }
}
