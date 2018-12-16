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

@Service("cartRedisManager")
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
        Long resultRow =  redisManager.hset(CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cart));
        return resultRow.intValue();
    }

    public Integer delCart(Integer userId, Integer productId){
        String userIdStr = CommonUtils.intToString(userId);
        String productIdStr = CommonUtils.intToString(productId);
        Long resultRow = redisManager.hdel(userIdStr,productIdStr);
        return resultRow.intValue();
    }

    public Integer delCarts(Integer userId, List<String> productIdList){
        //String[] productIdArray = (String[]) productIdList.toArray();
        String[] productIdArray = productIdList.toArray(new String[productIdList.size()]);
        String userIdStr = CommonUtils.intToString(userId);
        Long resultRow = redisManager.hdel(userIdStr,productIdArray);
        return resultRow.intValue();
    }

    /**
     * 这里不得不在更新数据的时候两次访问mysql：
     * 第一次写的时候，计划在更新数据的时候直接将redis缓存中的数据删除，
     * 在查询时发现redis缓存不存在此条数据再去mysql搜索并加入redis缓存，
     * 这种逻辑本来应该是最合理的，但是在设计时发现业务有冲突--业务中搜索一般都是批量查询，查userId下所有购物车产品的数据
     * 但是在redis中我以userId作为key，productId作为field，然后再将整个Cart对象转成json报文作为value存入redis（哈希对象），
     * 如果还是批量查询还是先查redis中缓存，会因为更新过的数据被删除而导致redis缓存的数据与mysql中的数据不同步现象，
     * 为了满足业务中的批量查询，所以第二次改写的时候不得不在每次改变数据时（增删改）的时候保证redis缓存与mysql中数据的同步，
     * 这样做其实相对很不合理，一是增加了繁琐的工作量，二是在业务逻辑特别复杂的情况下很容易出问题，
     * 比如在这个updateCart方法中，我想避免二次访问mysql，但是发现cartMapper.updateByPrimaryKey(cart);中update_time字段的逻辑是在sql语句用sql内置函数做的，
     * 如果我在此方法内调用new Date()作为update_time就很有可能造成redis缓存中与mysql中的同一条数据的数据不一致风险，
     * 所以为了保证数据同步且一致，不得不在更新了数据后再次访问mysql取出数据然后再存入redis。
     * 这样，本以提高效率为目的而加入的redis缓存在更新数据时却要比原来还要多一次访问mysql，
     * 上述种种，主要原因是在设计redis缓存之初，没有想通透，导致了原有逻辑和新增逻辑的冲突，
     * 后期看看有时间，找一个解决方案。
     */
    public Integer updateCart(Cart cart){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager cartCart Enter...");
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        int userId = cart.getUserId();
        int productId = cart.getProductId();
//        String cartJson = redisManager.hget(CommonUtils.intToString(userId),CommonUtils.intToString(productId));
//        Cart cartRedis = gson.fromJson(cartJson,Cart.class);
//        if(cart.getQuantity()!=null){
//            cartRedis.setQuantity(cart.getQuantity());
//        }
//        if(cart.getChecked()!=null){
//            cartRedis.setChecked(cart.getChecked());
//        }
        Cart cartNew = cartMapper.selectCartByUserIdProductId(userId,productId);
        Long resultRow = redisManager.hset(CommonUtils.intToString(userId),CommonUtils.intToString(productId),gson.toJson(cartNew));
        return resultRow.intValue();
    }

    public Integer selectCartProductCount(Integer userId){
        if(logger.isDebugEnabled()){
            logger.info("CartRedisManager cartCart Enter...");
        }
        Long count = redisManager.hlen(CommonUtils.intToString(userId));
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
            List<Cart> cartList = cartMapper.selectCartByUserId(userId);
            for(Cart cartItem : cartList){
                insertCart(cartItem);
            }
        }
        //否则为单选
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        insertCart(cart);
    }
}
