package com.mmall.redis;

import com.google.gson.Gson;
import com.mmall.pojo.User;
import com.mmall.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.mmall.common.Const;


@Component
public class SessionRedisManager {

    @Autowired
    private IRedisManager redisManager;

    private Gson gson = new Gson();

    public User getSession(Integer userId){
        String userJson = redisManager.get(Const.RedisKey.SESSION_KEY+ CommonUtils.intToString(userId));
        return gson.fromJson(userJson,User.class);
    }

    public User getSession(){
        String userJson = redisManager.get(Const.CURRENT_USER);
        return gson.fromJson(userJson,User.class);
    }

    public void setSession(Integer userId, User user){
        if(userId==null || user==null)
            return;
        redisManager.set(Const.RedisKey.SESSION_KEY+CommonUtils.intToString(userId),gson.toJson(user));
    }

    public void setSession(User user){
        if(user==null)
            return;
        redisManager.set(Const.CURRENT_USER,gson.toJson(user));
    }

    public void delSession(Integer userId){
        redisManager.del(Const.RedisKey.SESSION_KEY+CommonUtils.intToString(userId));
    }

    public void delSession(){
        redisManager.del(Const.CURRENT_USER);
    }
}
