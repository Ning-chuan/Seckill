package com.yuziyan.seckill.dao;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisDao {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    //从redis获取数据
    public Object get(String key) {
        if (StrUtil.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    //存数据
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 存数据（设置超时）
     * @param key
     * @param value
     * @param time  超时时间（秒）
     * @return
     */
    public boolean setEx(String key, Object value, long time,TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                this.set(key, value);

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private final static String REDUCE_STOCK_LUA_SCRIPT;
    private final static String ADD_STOCK_LUA_SCRIPT;

    static {

        StringBuilder sb = new StringBuilder();
        sb.append("if (redis.call('exists', KEYS[1]) == 1) then");
        sb.append("    local stock = tonumber(redis.call('get', KEYS[1]));");
//        sb.append("    if (stock == -1) then");
//        sb.append("        return -1");
//        sb.append("    end;");
        sb.append("    if (stock > 0) then");
        sb.append("        redis.call('incrby', KEYS[1], -1);");
        sb.append("        return stock - 1;");
        sb.append("    end;");
        sb.append("    return -1;");
        sb.append("end;");
        sb.append("return -2;");

        REDUCE_STOCK_LUA_SCRIPT = sb.toString();


        // 加库存脚本
        StringBuilder sb2 = new StringBuilder();
        sb2.append("if (redis.call('exists', KEYS[1]) == 1) then");
        sb2.append("    local stock = tonumber(redis.call('get', KEYS[1]));");
        sb2.append("    if (stock == -1) then");
        sb2.append("        redis.call('set', KEYS[1],1)");
        sb2.append("    end;");
        sb2.append("    if (stock == 0) then");
        sb2.append("        redis.call('set', KEYS[1],1)");
        sb2.append("    end;");
        sb2.append("    if (stock > 0) then");
        sb2.append("        redis.call('incrby', KEYS[1], 1);");
        sb2.append("        return stock + 1;");
        sb2.append("    end;");
        sb2.append("end;");
        sb2.append("return -2;");

        ADD_STOCK_LUA_SCRIPT = sb2.toString();
    }

    /**
     * lua脚本减库存
     */
    public Integer reduceStock(String key) {

        // 初始化减库存lua脚本
        // -1 库存不足
        // -2 不存在
        // 整数是正常操作，减库存成功

        // 脚本里的KEYS参数
        final List<String> keys = new ArrayList<String>();
        keys.add(key);
        // 脚本里的ARGV参数
        final List<String> args = new ArrayList<String>();

        Integer result = redisTemplate.execute(new RedisCallback<Integer>() {

            public Integer doInRedis(RedisConnection connection) throws DataAccessException {
                Object nativeConnection = connection.getNativeConnection();
                // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                // redis集群模式，执行脚本
//                if (nativeConnection instanceof JedisCluster) {
//                    return (Integer) ((JedisCluster) nativeConnection).eval(LUA_SCRIPT, keys, args);
//                }

                // redis单机模式，执行脚本
//                else if (nativeConnection instanceof Jedis) {
                if (nativeConnection instanceof Jedis) {
                    Object temp = ((Jedis) nativeConnection).eval(REDUCE_STOCK_LUA_SCRIPT, keys, args);
                    //System.out.println(" =====================>" + temp);
                    return Integer.valueOf(String.valueOf(temp));
                }

                return null;
            }
        });
        return result;
    }

    /**
     * lua脚本加库存
     */
    public Integer addStock(String key) {
        // 脚本里的KEYS参数
        final List<String> keys = new ArrayList<String>();
        keys.add(key);
        // 脚本里的ARGV参数
        final List<String> args = new ArrayList<String>();

        Integer result = redisTemplate.execute(new RedisCallback<Integer>() {

            public Integer doInRedis(RedisConnection connection) throws DataAccessException {
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof Jedis) {
                    Object temp = ((Jedis) nativeConnection).eval(ADD_STOCK_LUA_SCRIPT, keys, args);
                    return Integer.valueOf(String.valueOf(temp));
                }

                return null;
            }
        });
        return result;
    }

}
