package com.ygoj.user.controller;

import cn.hutool.core.lang.Validator;
import com.ygoj.common.Result;
import com.ygoj.common.filter.Permission;
import com.ygoj.user.pojo.Userinfo;
import com.ygoj.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

//    @GetMapping("/user/{id}")
//    public User getUser(@PathVariable("id") Long id){
//        User user = userService.getUserById(id);
//        return user;
//    }
//
//    @GetMapping("/addcnt")
//    public Result addcnt(@RequestParam Long userId){
//        userService.addcnt(userId);
//        return Result.success();
//    }

    /**
     * 用户注册接口
     * @param userinfo 注册用户信息
     * @return {@link Result}
     */
    @PostMapping("/register")
    public Result register(Userinfo userinfo) {
        //数据检验

        //用户名是否合法
        if(!Validator.isGeneral(userinfo.getUsername())){
            return Result.error(400, "用户名只能由字母、数字和下划线组成");
        }
        int len = userinfo.getUsername().length();
        if(3 > len || len > 20) {
            return Result.error(400, "用户名的长度要大于3小于20");
        }

        //用户名是否重复
        if(userService.getUserinfoByUsername(userinfo.getUsername()) != null){
            return Result.error(400, "用户名重复");
        }

        //用户昵称是否合法
        len = userinfo.getNickname().length();
        if(3 > len || len > 20) {
            return Result.error(400, "用户昵称的长度要大于3小于20");
        }

        //密码是否合法
        if(!Validator.isMatchRegex("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]+$", userinfo.getPassword())) {
            return Result.error(400, "密码只能由字母、数字和特殊符号组成");
        }

        //检查邮箱格式是否合法
        if(!Validator.isEmail(userinfo.getEmail())) {
            return Result.error(400, "邮箱不合法");
        }

        userService.register(userinfo);
        return Result.success();
    }

    @PostMapping("/login")
    public Result login(String loginStr, String password) {
        //登录功能
        Userinfo userinfo = userService.login(loginStr, password);

        if(userinfo == null){
            return Result.error(400, "账号或密码错误");
        }
        return Result.success(userinfo.getPassword());
    }

    @GetMapping("/test")
    @Permission(auth = 0)
    public Result test() {
        return Result.success();
    }
}
