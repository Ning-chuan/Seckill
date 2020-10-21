package com.yuziyan.seckill.controller;


import cn.hutool.core.util.ObjectUtil;
import com.yuziyan.seckill.entity.SeckillItem;
import com.yuziyan.seckill.entity.User;
import com.yuziyan.seckill.exception.UserException;
import com.yuziyan.seckill.service.SeckillItemService;
import com.yuziyan.seckill.service.UserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/getUser")
    @ResponseBody
    public User getUser(int id) {
        System.out.println("UserController.getUser");
        return userService.getUser(id);
    }

    @RequestMapping(value = "/login")
    public String login(String username, String password, Model model , HttpSession session) {
        //数据校验
        if (username == null || "".equals(username) && password == null || "".equals(password)) {
            throw new UserException("非法登用户，请重新录！");
        }
        //调用业务层登录方法
        User user = userService.login(username, password);
        if (ObjectUtil.isEmpty(user)) {
            throw new UserException("用户名或密码错误");
        }
        //把当前用户信息存在session里
        session.setAttribute("user",user);
        //转发到秒杀商品列表页
        return "forward:/getSeckillItemList";
    }


}
