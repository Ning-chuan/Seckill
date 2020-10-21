package com.yuziyan.seckill.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.yuziyan.seckill.dao.UserDao;
import com.yuziyan.seckill.entity.User;
import com.yuziyan.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    public User getUser(int id) {
        return userDao.getUser(id);
    }

    @Override
    public User login(String username,String password) {
        return userDao.getUserByNameAndPassword(username, DigestUtil.md5Hex(password));
    }

}
