package com.mmall.redis;

import com.mmall.util.CommonUtils;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public class JedisManager {

    private static Logger logger = LoggerFactory.getLogger(JedisManager.class);

    private static volatile JedisPool jedisPool = null;

    private static String REDIS_URL = null;

    private static Integer REDIS_PORT = null;

    private static Integer MAX_TOTAL = null;

    private static Integer MAX_IDLE = null;

    private static Integer MIN_IDLE = null;

    static {
        REDIS_URL = PropertiesUtil.getProperty("redis_url");
        REDIS_PORT = CommonUtils.strToInteger(PropertiesUtil.getProperty("redis_port"),6379);
        MAX_TOTAL = CommonUtils.strToInteger(PropertiesUtil.getProperty("max_total"),8);
        MAX_IDLE = CommonUtils.strToInteger(PropertiesUtil.getProperty("max_idle"),8);
        MIN_IDLE = CommonUtils.strToInteger(PropertiesUtil.getProperty("min_idle"),0);
    }

    public static JedisPool getInstance(){
        if(jedisPool==null){
            synchronized (JedisManager.class){
                if(jedisPool==null){
                    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                    poolConfig.setMaxIdle(MAX_IDLE);
                    poolConfig.setMaxTotal(MAX_TOTAL);
                    poolConfig.setMinIdle(MIN_IDLE);
                    jedisPool = new JedisPool(poolConfig,REDIS_URL,REDIS_PORT);
                }
            }
        }
        return jedisPool;
    }
}
