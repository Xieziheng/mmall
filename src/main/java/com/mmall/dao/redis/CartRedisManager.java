package com.mmall.dao.redis;

import com.mmall.dao.CartMapper;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Map;


public class CartRedisManager {

    private Logger logger = LoggerFactory.getLogger(CartRedisManager.class);

    @Resource
    private CartMapper cartMapper;

    private static volatile Jedis jedis = null;

    public static Jedis getInstance(){
        if(jedis==null){
            synchronized (CartRedisManager.class){
                if(jedis==null){
                    jedis = new Jedis(PropertiesUtil.getProperty("redis_url"),new Integer(PropertiesUtil.getProperty("redis_port")));
                }
            }
        }
        return jedis;
    }
}
