package com.yuziyan.seckill.dao;

import com.yuziyan.seckill.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserDao {


    User getUser(int id);


    User getUserByNameAndPassword(@Param("name") String name, @Param("password") String password);

    int addUser(User user);
}
