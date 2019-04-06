package com.mmall.redis;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mmall.dao.CartMapper;
import com.mmall.pojo.Cart;
import com.mmall.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import com.mmall.common.Const;

@Service("cartRedisManager")
public class CartRedisManager{

    private Logger logger = LoggerFactory.getLogger(CartRedisManager.class);

    @Resource
    private IRedisManager redisManager;

    @Resource
    private CartMapper cartMapper;

    private Gson gson = new Gson();

    public Cart getCart(Integer userId, Integer productId){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager getCart Enter...");
        }
        Cart cart = null;
        String cartJson = redisManager.hget(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId));

        if(CommonUtils.isEmpty(cartJson)){
            cart = cartMapper.selectCartByUserIdProductId(userId,productId);
            if(cart!=null){
                redisManager.hset(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cart));
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
        Map<String,String> map = redisManager.hgetAll(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId));
        List<Cart> cartList = null;
        if(CollectionUtils.isEmpty(map)){
            cartList = cartMapper.selectCartByUserId(userId);
            if(!CollectionUtils.isEmpty(cartList)){
                for(Cart cartItem:cartList){
                    insertCart(cartItem);
                }
            }
        } else {
            List<String> cartJsonList = Lists.newArrayList(map.values());
            cartList = new ArrayList<>(cartJsonList.size());
            for(String cartJson : cartJsonList){
                cartList.add(gson.fromJson(cartJson ,Cart.class));
            }
        }
        return cartList;
    }

    public Integer insertCart(Cart cart){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager insertCart Enter...");
        }
        cartMapper.insert(cart);
        //保持redis缓存中数据同步
        int userId = cart.getUserId();
        int productId = cart.getProductId();
        Long resultRow =  redisManager.hset(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cart));
        return resultRow.intValue();
    }

    private Integer insertCartInRedis(Cart cart){
        int userId = cart.getUserId();
        int productId = cart.getProductId();
        Long resultRow =  redisManager.hset(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cart));
        return resultRow.intValue();
    }

    public Integer delCart(Integer userId, Integer productId){
        String userIdStr = CommonUtils.intToString(userId);
        String productIdStr = CommonUtils.intToString(productId);
        Long resultRow = redisManager.hdel(Const.RedisKey.CART_KEY+userIdStr,productIdStr);
        return resultRow.intValue();
    }

    public Integer delCarts(Integer userId, List<String> productIdList){
        //String[] productIdArray = (String[]) productIdList.toArray();
        String[] productIdArray = productIdList.toArray(new String[productIdList.size()]);
        String userIdStr = CommonUtils.intToString(userId);
        Long resultRow = redisManager.hdel(Const.RedisKey.CART_KEY+userIdStr,productIdArray);
        //数据库同步删除
        cartMapper.deleteByUserIdProductIds(userId,productIdList);
        return resultRow.intValue();
    }

    private Integer updateCartInRedis(Cart cart){
        int userId = cart.getUserId();
        int productId = cart.getProductId();
        String cartJson = redisManager.hget(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId));
        Cart cartRedis = gson.fromJson(cartJson,Cart.class);
        if(cart.getQuantity()!=null){
            cartRedis.setQuantity(cart.getQuantity());
        }
        if(cart.getChecked()!=null){
            cartRedis.setChecked(cart.getChecked());
        }
        Long resultRow = redisManager.hset(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cartRedis));
        return resultRow.intValue();
    }

    public Integer updateCart(Cart cart){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager cartCart Enter...");
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        int userId = cart.getUserId();
        int productId = cart.getProductId();
        String cartJson = redisManager.hget(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId));
        Cart cartRedis = gson.fromJson(cartJson,Cart.class);
        if(cart.getQuantity()!=null){
            cartRedis.setQuantity(cart.getQuantity());
        }
        if(cart.getChecked()!=null){
            cartRedis.setChecked(cart.getChecked());
        }

        //Cart cartNew = cartMapper.selectCartByUserIdProductId(userId,productId);
        Long resultRow = redisManager.hset(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cartRedis));
        return resultRow.intValue();
    }

    public Integer selectCartProductCount(Integer userId){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager cartCart Enter...");
        }
        Long count = redisManager.hlen(Const.RedisKey.CART_KEY+CommonUtils.intToString(userId));
        if(count==null){
            return cartMapper.selectCartProductCount(userId);
        }
        return count.intValue();
    }

    public void selectOrUnSelect(Integer userId, Integer checked, Integer productId){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager cartCart Enter...");
        }
        cartMapper.checkedOrUnCheckedProduct(userId,checked,productId);
        //全选productId为null
        if(productId==null){
            List<Cart> cartList = getCartListByUserId(userId);
            for(Cart cartItem : cartList){
                cartItem.setChecked(checked);
                updateCartInRedis(cartItem);
            }
        }
        else {
            //否则为单选
            Cart cart = getCart(userId,productId);
            cart.setChecked(checked);
            updateCartInRedis(cart);
        }
    }
}
