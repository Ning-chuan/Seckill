package com.yuziyan.seckill.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.yuziyan.seckill.dao.RedisDao;
import com.yuziyan.seckill.dao.SeckillItemDao;
import com.yuziyan.seckill.dto.SeckillUrl;
import com.yuziyan.seckill.entity.SeckillItem;
import com.yuziyan.seckill.entity.User;
import com.yuziyan.seckill.service.SeckillItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillItemServiceImpl implements SeckillItemService {

    @Autowired
    SeckillItemDao seckillItemDao;
    @Autowired
    RedisDao redisDao;

    @Override
    public List<SeckillItem> getSeckillItemList() {
        return seckillItemDao.getAllSeckillItems();
    }

    @Override
    public SeckillItem getSeckillItem(Integer id) {
        return seckillItemDao.getSeckillItemById(id);
    }

    @Override
    public SeckillUrl getSeckillUrl(Integer itemId) {
        SeckillUrl seckillUrl = new SeckillUrl();
        seckillUrl.setItemId(itemId);

        //判断这个商品的是否在活动时间：
        // 获取这个商品，先从redis中拿
        SeckillItem seckillItem = (SeckillItem) redisDao.get(itemId.toString());
        if (ObjectUtil.isEmpty(seckillItem)) {
            //redis缓存中没有，从MySQL中拿
            seckillItem = seckillItemDao.getSeckillItemById(itemId);
            if (ObjectUtil.isEmpty(seckillItem)) {
                //数据库中没有这个id的商品
                //设置为不可用，然后返回
                seckillUrl.setEnable(false);
                return seckillUrl;
            }
            //此时查到了数据
            //存入redis，后面的用户就可以直接从redis缓存中获取了
            redisDao.set(itemId.toString(), seckillItem);
            //同时把商品库存同时存进redis，方便后面执行秒杀操作时用
            redisDao.set("stock_" + itemId, seckillItem.getNumber());
        }
        // 判断时间：
        long startTime = seckillItem.getStartTime().getTime();
        long endTime = seckillItem.getEndTime().getTime();
        long curTime = new Date().getTime();
        if (curTime < startTime || curTime >= endTime) {
            //不在活动期间，设置为不可用，然后返回
            seckillUrl.setEnable(false);
            return seckillUrl;
        }

        //通过了验证，需要生成MD5字串
        String md5Str = this.generateMd5Str(itemId);
        seckillUrl.setMd5(md5Str);
        //设置可用
        seckillUrl.setEnable(true);
        return seckillUrl;
    }

    //用于混淆md5的key
    private static final String mixKey = "DGS_8$2@sh";

    private String generateMd5Str(Integer itemId) {
        return DigestUtil.md5Hex(mixKey + "," + itemId);
    }

    @Override
    public boolean verifyMd5Str(Integer itemId, String md5) {
        if (ObjectUtil.isEmpty(itemId) || StrUtil.isEmpty(md5)) {
            return false;
        }
        return this.generateMd5Str(itemId).equals(md5);
    }

    /**
     * 执行秒杀操作
     * 业务需求：一个用户对一个商品的秒杀请求，在五分钟内只处理一次
     * @param user
     * @param itemId
     * @return
     */
    @Override
    public boolean executeSeckill(User user, Integer itemId) {
        //1.检查超时时间（保证五分钟内只处理一次用户对某个商品的请求）
        //思路：把用户的phone和商品id结合作为一个标识，在第一次请求的时候把这个标识存入redis缓存
        //     同时设置超时时间为5分钟，在这期间的多次请求不再处理。
        //标识格式：phone_itemId
        System.out.println("SeckillItemServiceImpl.executeSeckill");
        String mark = user.getPhone()+"_"+itemId;
        if (!ObjectUtil.isEmpty(redisDao.get(mark))) {
            //此时redis缓存中存在标记，此次请求不处理
            return false;
        }
        redisDao.setEx(mark, itemId, 60 * 5, TimeUnit.SECONDS);


        //2.调用redis进行查、减库存的操作（利用redis lua脚本的原子性）
        // -1 库存不足
        // -2 不存在
        // 整数是正常操作，减库存成功
        Integer result = redisDao.reduceStock("stock_" + itemId);
        if (ObjectUtil.isEmpty(result) || result == -1 || result == -2) {
            //排除没有执行成功的情况
            return false;
        }
        //代码走到这一行说明redis的减库存操作执行成功了，由于lua脚本执行具有原子性
        //大量未抢购成功的人已经被上一步拦下了，剩下的都是抢购到的用户，此时可以进行MySQL数据库的更新

        //3.更新MySQL对应数据（需要使用Spring框架增加事务，所以单独抽取一个方法）
        this.updateMySQLStock(-1,itemId);

        return true;
    }

    //更新MySQL库存
    //参数：正数：增加库存   负数：减少库存
    @Transactional
    @Override
    public void updateMySQLStock(Integer changeNum,Integer itemId){
        seckillItemDao.updateStock(changeNum,itemId);
    }


}
