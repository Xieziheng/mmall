package com.mmall.redis;

import com.google.gson.Gson;
import com.mmall.dao.CartMapper;
import com.mmall.pojo.Cart;
import com.mmall.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

public class CartRedisManager{

    private Logger logger = LoggerFactory.getLogger(CartRedisManager.class);

    @Resource
    private IRedisManager redisManager;

    @Resource
    private CartMapper cartMapper;

    Gson gson = new Gson();

    public Cart getCart(Integer userId, Integer productId){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager getCart Enter...");
        }
        String cartJson = redisManager.hget(CommonUtils.intToString(userId),CommonUtils.intToString(productId));
        Cart cart = gson.fromJson(cartJson,Cart.class);
        return cart;
    }
}
