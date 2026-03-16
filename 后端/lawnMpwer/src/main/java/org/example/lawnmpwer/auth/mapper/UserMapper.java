package org.example.lawnmpwer.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.lawnmpwer.auth.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 就拥有了基础的 CRUD 能力，无需写 XML
}