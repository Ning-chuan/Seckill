package com.yuziyan.seckill.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yuziyan.seckill.dto.ResponseResult;
import com.yuziyan.seckill.dto.SeckillUrl;
import com.yuziyan.seckill.entity.SeckillItem;
import com.yuziyan.seckill.entity.SeckillOrder;
import com.yuziyan.seckill.entity.User;
import com.yuziyan.seckill.exception.SeckillException;
import com.yuziyan.seckill.exception.UserException;
import com.yuziyan.seckill.service.SeckillItemService;
import com.yuziyan.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillItemService seckillItemService;
    @Autowired
    SeckillOrderService seckillOrderService;

    @RequestMapping("/getSeckillItemList")
    public String getSeckillItemList(Model model){
        List<SeckillItem> seckillItemList = seckillItemService.getSeckillItemList();
        model.addAttribute("seckillItemList", seckillItemList);
        return "itemList";
    }


    @RequestMapping("/itemDetail/{itemId}")
    public String itemDetail(@PathVariable("itemId")Integer itemId, Model model){
        //System.out.println("SeckillController.toItemDetail");
        if (itemId <= 0) {
            throw new SeckillException("无效的参数");
        }
        SeckillItem seckillItem = seckillItemService.getSeckillItem(itemId);
        if (ObjectUtil.isEmpty(seckillItem)) {
            throw new SeckillException("没有这个商品");
        }
        model.addAttribute("seckillItem",seckillItem);
        return "itemDetail";
    }

    //返回服务器的当前时间
    @RequestMapping("/getServerTime")
    @ResponseBody
    public ResponseResult<Long> getServerTime(){
        return new ResponseResult<>(true,new Date().getTime(),"ok");
    }

    //获取商品的秒杀url
    @RequestMapping(value = "/getSeckillUrl/{itemId}",method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<SeckillUrl> getSeckillUrl(@PathVariable("itemId") int itemId, HttpSession session){
        //后端验证：
        if (ObjectUtil.isEmpty(session.getAttribute("user"))) {
            throw new UserException("尚未登陆");
        }
        ResponseResult<SeckillUrl> result = new ResponseResult<>();
        try {
            //获取seckillUrl 设置响应信息
            SeckillUrl seckillUrl = seckillItemService.getSeckillUrl(itemId);
            result.setSuccess(true);
            result.setData(seckillUrl);
            result.setMessage("ok");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/startSeckill/{itemId}/{md5}",method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<SeckillOrder> startSeckill(@PathVariable("itemId")Integer itemId,@PathVariable("md5") String md5,HttpSession session){
        ResponseResult<SeckillOrder> result = new ResponseResult<>();
        //1.验证
        //  验证用户是否登录：
        User user = (User) session.getAttribute("user");
        if (ObjectUtil.isEmpty(user)) {
            result.setSuccess(false);
            result.setMessage("no user login");
            return result;
        }
        //  验证md5标记
        boolean mark = seckillItemService.verifyMd5Str(itemId,md5);
        if (!mark) {
            result.setSuccess(false);
            result.setMessage("Commodity dose not exist or not during the event! ");
            return result;
        }

        //2.验证通过，调用业务层的方法，执行秒杀操作
        boolean success = seckillItemService.executeSeckill(user,itemId);
        if (!success) {
            //此时秒杀执行失败，设置失败信息，并返回。
            result.setSuccess(false);
            result.setMessage("很遗憾，未能抢购成功。");
            return result;
        }

        //3.代码走到这里说明秒杀执行成功了，调用业务层方法，生成订单（状态为未支付）
        SeckillOrder order = seckillOrderService.createOrder(itemId, user.getId(), 1);
        result.setSuccess(true);
        result.setData(order);
        result.setMessage("ok");
        return result;
    }

    //转发到订单页面
    @RequestMapping("/toOrderPage/{orderCode}")
    public String toOrderPage(@PathVariable("orderCode") String orderCode,Model model){
        //需要存订单和商品两个参数，在订单页面中获取
        SeckillOrder order = seckillOrderService.getOrderByOrderCode(orderCode);
        SeckillItem seckillItem = seckillItemService.getSeckillItem(order.getSeckillItemId());
        model.addAttribute("order", order);
        model.addAttribute("seckillItem", seckillItem);
        return "orderPage";
    }

    //处理支付订单请求
    @RequestMapping(value = "/payOrder")
    public String payOrder(String orderCode){
        System.out.println("orderCode = " + orderCode);
        if (StrUtil.isEmpty(orderCode)) {
            throw new SeckillException("没有该订单");
        }
        boolean res = seckillOrderService.payOrder(orderCode);
        if (res){
            return "paySuccess";
        }
        throw new SeckillException("支付失败，可能的原因有：订单不存在，订单已支付，订单已过期。");
    }

}
