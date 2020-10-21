package com.yuziyan.seckill.service;

import com.yuziyan.seckill.entity.User;

public interface UserService {

    User getUser(int id);

    User login(String username,String password);
}
