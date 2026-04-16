package com.ygoj.user.controller;

import cn.hutool.core.lang.Validator;
import com.ygoj.common.Result;
import com.ygoj.common.filter.Permission;
import com.ygoj.user.Userinfo;
import com.ygoj.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户注册接口（公开访问）
     * @param userinfo 注册用户信息
     * @return {@link Result}
     */
    @PostMapping("/register")
    @Permission(requireLogin = false)
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
     * 登录功能接口（公开访问）
     *
     * @param loginStr 登录str（用户名称或邮箱）
     * @param password 密码
     * @return {@link Result}
     */
    @PostMapping("/login")
    @Permission(requireLogin = false)
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
            log.info("用户登录成功, userId: {}", userinfo.getId());
            return Result.success(userinfo.getPassword());
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 注销
     *
     * @param request 请求
     * @return {@link Result}
     */
    @PostMapping("/logout")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "0", // 任何登录用户都可以注销
        message = "请先登录"
    )
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
     * 根据id获取用户信息（需要登录）
     *
     * @param id 用户id
     * @return {@link Result}
     */
    @GetMapping("/userinfo/{id}")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "0", // 任意登录用户
        message = "请先登录"
    )
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
     * 通过token获取用户ID（需要登录）
     */
    @PostMapping("/userinfo")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "0", // 任意登录用户
        message = "请先登录"
    )
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
     * 获取所有用户列表（仅管理员）
     */
    @GetMapping("/admin/users")
    @Permission(
        type = Permission.PermissionType.ROLE,
        value = "ADMIN",
        message = "只有管理员可以查看用户列表"
    )
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
     * 更新用户权限（仅管理员）
     */
    @PutMapping("/admin/user/permission")
    @Permission(
        type = Permission.PermissionType.ROLE,
        value = "ADMIN",
        message = "只有管理员可以修改用户权限"
    )
    public Result updateUserPermission(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String role = (String) params.get("role");
            Long permission = Long.valueOf(params.get("permission").toString());
            
            log.info("管理员更新用户权限请求, userId: {}, role: {}, permission: {}", userId, role, permission);
            
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
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/admin/user/{id}")
    @Permission(
        type = Permission.PermissionType.ROLE,
        value = "ADMIN",
        message = "只有管理员可以删除用户"
    )
    public Result deleteUser(@PathVariable Long id) {
        try {
            log.info("管理员删除用户请求, userId: {}", id);
            
            if (id == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            return userService.deleteUser(id);
        } catch (Exception e) {
            log.error("删除用户失败, userId: {}", id, e);
            return Result.error(500, "删除用户失败: " + e.getMessage());
        }
    }
}
