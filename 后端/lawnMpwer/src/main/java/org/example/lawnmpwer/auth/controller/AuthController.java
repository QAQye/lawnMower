package org.example.lawnmpwer.auth.controller;

import org.example.lawnmpwer.auth.dto.LoginRequest;
import org.example.lawnmpwer.auth.dto.LoginResponse;
import org.example.lawnmpwer.auth.dto.RegisterRequest;
import org.example.lawnmpwer.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

//    前端访问auth/login即可调用登录的接口
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        try {
//            service层调用login函数进行登录，最终得到一个这个对象含带token
//            service是专门处理业务逻辑的,里面涵盖有登录注册的接口
            String token = userService.login(username, password);
            return new LoginResponse(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名或密码不能为空");
        }

        try {
            userService.register(username, password);
            return Map.of("message", "注册成功！");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}