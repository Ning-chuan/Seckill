package com.yuziyan.seckill.service;

import com.yuziyan.seckill.entity.SeckillOrder;

public interface SeckillOrderService {

    SeckillOrder createOrder(Integer itemId, Integer userId, Integer state);

    SeckillOrder getOrderByOrderCode(String orderCode);

    void checkExpireOrder();

    boolean payOrder(String orderCode);
}
