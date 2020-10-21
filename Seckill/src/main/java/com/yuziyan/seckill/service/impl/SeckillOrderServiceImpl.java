package com.yuziyan.seckill.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.yuziyan.seckill.dao.RedisDao;
import com.yuziyan.seckill.dao.SeckillItemDao;
import com.yuziyan.seckill.dao.SeckillOrderDao;
import com.yuziyan.seckill.entity.SeckillOrder;
import com.yuziyan.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling//开启基于Spring的定时任务
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    SeckillOrderDao seckillOrderDao;
    @Autowired
    RedisDao redisDao;
    @Autowired
    SeckillItemDao seckillItemDao;

    @Override
    public SeckillOrder createOrder(Integer itemId, Integer userId, Integer state) {
        //创建订单，根据业务需求设置超时时长，如果不设置，默认为5分钟
        SeckillOrder seckillOrder = new SeckillOrder(IdUtil.simpleUUID(), itemId, userId, state, new Date(), 30);
        //存到redis缓存中，由于redis存值时自带超时，可以用于检测超时的订单
        redisDao.setEx("order_timeout_" + seckillOrder.getOrderCode(), seckillOrder, 30, TimeUnit.SECONDS);
        //存到MySQL数据库
        seckillOrderDao.addSeckillOrder(seckillOrder);
        return seckillOrder;
    }

    @Override
    public SeckillOrder getOrderByOrderCode(String orderCode) {
        return seckillOrderDao.selectOrderByOrderCode(orderCode);
    }

    //定时任务：每秒检查过期的订单，修改数据库中对应订单的状态
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void checkExpireOrder() {
        List<SeckillOrder> noPaidOrders = seckillOrderDao.getOrdersByState(1);
        if (ObjectUtil.isEmpty(noPaidOrders) || noPaidOrders.size() == 0) {
            //MySQL数据库中没有未支付的订单，直接返回
            return;
        }
        //遍历未支付订单，如果redis中没有该订单，说明已经超时，更新MySQL中对应订单的状态
        for (SeckillOrder order : noPaidOrders) {
            Object orderOfRedis = redisDao.get("order_timeout_" + order.getOrderCode());

            if (ObjectUtil.isEmpty(orderOfRedis)) {
                //此时说明订单超时，修改订单状态为超时、未支付
                seckillOrderDao.setStateByOrderCode(order.getOrderCode(), 4);

                //redis MySQL中对应商品的库存分别加一
                redisDao.addStock("stock_" + order.getSeckillItemId());
                seckillItemDao.updateStock(1, order.getSeckillItemId());
            }

        }
    }

    @Override
    public boolean payOrder(String orderCode) {
        //调用dao查看该订单的状态
        SeckillOrder seckillOrder = seckillOrderDao.selectOrderByOrderCode(orderCode);
        Integer state = seckillOrder.getState();
        if (ObjectUtil.isEmpty(seckillOrder) || state != 1) {
            return false;
        }
        //设置订单状态为已支付
        seckillOrderDao.setStateByOrderCode(orderCode, 2);
        return true;
    }


}
