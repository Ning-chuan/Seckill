package com.yuziyan.seckill.service;

import com.yuziyan.seckill.dto.SeckillUrl;
import com.yuziyan.seckill.entity.SeckillItem;
import com.yuziyan.seckill.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SeckillItemService {
    List<SeckillItem> getSeckillItemList();

    SeckillItem getSeckillItem(Integer id);

    SeckillUrl getSeckillUrl(Integer itemId);

    boolean verifyMd5Str(Integer itemId,String md5);

    boolean executeSeckill(User user, Integer itemId);

    void updateMySQLStock(Integer changeNum,Integer itemId);

}
