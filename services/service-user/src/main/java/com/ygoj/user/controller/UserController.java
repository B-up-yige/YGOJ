package com.ygoj.user.controller;

import com.ygoj.common.Result;
import com.ygoj.user.pojo.User;
import com.ygoj.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
}
