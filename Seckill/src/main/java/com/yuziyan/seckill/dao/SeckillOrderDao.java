package com.yuziyan.seckill.dao;

import com.yuziyan.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SeckillOrderDao {

    void addSeckillOrder(SeckillOrder seckillOrder);

    SeckillOrder selectOrderByOrderCode(String orderCode);

    List<SeckillOrder> getOrdersByState(Integer i);

    void setStateByOrderCode(@Param("orderCode") String orderCode, @Param("state") Integer state);
}
