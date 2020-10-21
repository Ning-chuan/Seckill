package com.yuziyan.seckill.dao;

import com.yuziyan.seckill.entity.SeckillItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SeckillItemDao {
    List<SeckillItem> getAllSeckillItems();

    SeckillItem getSeckillItemById(Integer id);

    void updateStock(@Param("changeNum") Integer changeNum, @Param("itemId") Integer itemId);

}
