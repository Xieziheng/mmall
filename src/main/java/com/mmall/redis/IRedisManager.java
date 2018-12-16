package com.mmall.redis;

import java.util.List;
import java.util.Map;

public interface IRedisManager {

    String set(String key, String value);

    String get(String key);

    Long hset(String key, String item, String value) ;

    String hget(String key, String item);

    Long incr(String key) ;

    Long decr(String key) ;

    Long expire(String key, int second) ;

    Long ttl(String key) ;

    Long del(String key);

    Long hdel(String key, String... field);

    Map<String,String> hgetAll(String key);

    Long lpush(String key,String value);

    List<String> lrange(String key, Long start, Long stop);

    Long lrem(String key, Long count, String value);

    Long hlen(String key);
}
