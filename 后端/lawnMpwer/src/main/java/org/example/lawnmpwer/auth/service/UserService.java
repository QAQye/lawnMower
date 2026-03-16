package org.example.lawnmpwer.auth.service;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.lawnmpwer.auth.entity.User;

/**
 * UserService 用户业务逻辑接口
 * * 继承了 MyBatis-Plus 提供的 IService<User> 接口。
 * 这一步非常强大，意味着即使你不写任何代码，
 * 这个接口就已经自带了对 User 表的增、删、改、查、分页等几十个现成的方法。
 */
public interface UserService extends IService<User> {
    String login(String username, String password);
    void register(String username, String password);
}