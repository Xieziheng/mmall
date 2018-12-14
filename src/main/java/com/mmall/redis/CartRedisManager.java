package com.mmall.redis;

import com.google.gson.Gson;
import com.mmall.dao.CartMapper;
import com.mmall.pojo.Cart;
import com.mmall.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        Cart cart = null;
        String cartJson = redisManager.hget(CommonUtils.intToString(userId),CommonUtils.intToString(productId));

        if(CommonUtils.isEmpty(cartJson)){
            cart = cartMapper.selectCartByUserIdProductId(userId,productId);
            if(cart!=null){
                redisManager.hset(CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cart));
            }
        }
        else {
            cart = gson.fromJson(cartJson,Cart.class);
        }

        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager getCart the jsonStr = "+cartJson);
        }
        return cart;
    }

    public List<Cart> getCartListByUserId(Integer userId){
        Map<String,String> map = redisManager.hgetAll(CommonUtils.intToString(userId));
        //todo
        return null;
    }


    public Integer insertCart(Cart cart){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager insertCart Enter...");
        }
        cartMapper.insert(cart);
        return this.delCart(cart.getUserId(),cart.getProductId());
    }

    public Integer delCart(Integer userId, Integer productId){
        String userIdStr = CommonUtils.intToString(userId);
        String productIdStr = CommonUtils.intToString(productId);
        Long resultRow = redisManager.hdel(userIdStr,productIdStr);
        return resultRow.intValue();
    }

    public Integer delCarts(Integer userId, List<String> productIdList){
        String[] productIdArray = (String[]) productIdList.toArray();
        String userIdStr = CommonUtils.intToString(userId);
        Long resultRow = redisManager.hdel(userIdStr,productIdArray);
        return resultRow.intValue();
    }

    public Integer updateCart(Cart cart){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager cartCart Enter...");
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.delCart(cart.getUserId(),cart.getProductId());
    }


}
