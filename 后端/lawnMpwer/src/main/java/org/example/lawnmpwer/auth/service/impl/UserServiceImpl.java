package org.example.lawnmpwer.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.lawnmpwer.auth.security.JwtUtil;
import org.example.lawnmpwer.auth.entity.User;
import org.example.lawnmpwer.auth.mapper.UserMapper;
import org.example.lawnmpwer.auth.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 密码加密器
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        // 1. 去数据库查询是否存在该用户
        User user = this.query().eq("username", username).one();

        // 2. 验证用户是否存在，且密码是否匹配
        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 登录成功，生成并返回 Token
        return JwtUtil.generateToken(username);
    }

    @Override
    public void register(String username, String password) {
        // 1. 检查用户名是否被占用
        if (this.query().eq("username", username).exists()) {
            throw new RuntimeException("用户名已存在，请换一个");
        }

        // 2. 创建新用户，并对密码进行加密保存
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encoder.encode(password)); // 存入数据库的是乱码一样的密文

        this.save(newUser);
    }
}