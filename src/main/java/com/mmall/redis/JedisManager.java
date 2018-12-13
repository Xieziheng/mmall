package com.mmall.redis;

import com.mmall.util.CommonUtils;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class JedisManager {

    private static Logger logger = LoggerFactory.getLogger(JedisManager.class);

    private static volatile JedisPool jedisPool = null;

    public static JedisPool getInstance(){
        if(jedisPool!=null){
            synchronized (JedisManager.class){
                if(jedisPool!=null){
                    jedisPool = new JedisPool(PropertiesUtil.getProperty("redis_url"),
                            CommonUtils.strToInteger(PropertiesUtil.getProperty("redis_port"),6379));

                }
            }
        }
        return jedisPool;
    }
}
