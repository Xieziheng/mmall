package com.mmall.redis;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

@Service("iRedisManager")
public class RedisManagerImpl implements IRedisManager {

    private JedisPool jedisPool = JedisManager.getInstance();

    @Override
    public String set(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        String result = jedis.set(key,value);
        jedis.close();
        return result;
    }

    @Override
    public String get(String key) {
        Jedis jedis = jedisPool.getResource();
        String result = jedis.get(key);
        jedis.close();
        return result;
    }

    @Override
    public Long hset(String key, String item, String value) {
        Jedis jedis = jedisPool.getResource();
        Long result = jedis.hset(key, item, value);
        jedis.close();
        return result;
    }

    @Override
    public String hget(String key, String item) {
        Jedis jedis = jedisPool.getResource();
        String result = jedis.hget(key, item);
        jedis.close();
        return result;

    }

    @Override
    public Long incr(String key) {
        Jedis jedis = jedisPool.getResource();
        Long result = jedis.incr(key);
        jedis.close();
        return result;
    }

    @Override
    public Long decr(String key) {
        Jedis jedis = jedisPool.getResource();
        Long result = jedis.decr(key);
        jedis.close();
        return result;
    }

    @Override
    public Long expire(String key, int second) {
        Jedis jedis = jedisPool.getResource();
        Long result = jedis.expire(key, second);
        jedis.close();
        return result;
    }

    @Override
    public Long ttl(String key) {
        Jedis jedis = jedisPool.getResource();
        Long result = jedis.ttl(key);
        jedis.close();
        return result;
    }

    @Override
    public Long del(String key) {
        Jedis jedis = jedisPool.getResource();
        Long resultRow = jedis.del(key);
        jedis.close();
        return resultRow;
    }

    @Override
    public Long hdel(String key, String... field) {
        Jedis jedis = jedisPool.getResource();
        Long resultRow = jedis.hdel(key,field);
        jedis.close();
        return resultRow;
    }

    public Map<String,String> hgetAll(String key){
        Jedis jedis = jedisPool.getResource();
        Map<String,String> resultMap = jedis.hgetAll(key);
        jedis.close();
        return resultMap;
    }

    public Long lpush(String key,String value){
        Jedis jedis = jedisPool.getResource();
        Long resultRow = jedis.lpush(key,value);
        jedis.close();
        return resultRow;
    }

    public List<String> lrange(String key, Long start, Long stop){
        Jedis jedis = jedisPool.getResource();
        List<String> resultList = jedis.lrange(key,start,stop);
        jedis.close();
        return resultList;
    }

    public Long lrem(String key, Long count, String value){
        Jedis jedis = jedisPool.getResource();
        Long resultRow = jedis.lrem(key,count,value);
        jedis.close();
        return resultRow;
    }

    public Long hlen(String key){
        Jedis jedis = jedisPool.getResource();
        Long result = jedis.hlen(key);
        jedis.close();
        return result;
    }
}
