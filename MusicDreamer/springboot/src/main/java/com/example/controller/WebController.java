package com.example.controller;

import com.example.util.Result;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class WebController {

    @Resource
    UserService userService;
    /**
     * 默认请求接口
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        Map<String, Object> loginResult = userService.login(user);
        return Result.success(loginResult);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }
}
