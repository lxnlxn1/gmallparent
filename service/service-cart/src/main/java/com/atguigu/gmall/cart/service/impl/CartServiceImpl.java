package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lxn
 * @create 2020-03-29 22:07
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        //获取购物车的key
        String cartKey = getCartKey(userId);

        if (!redisTemplate.hasKey(cartKey)) {
            loadCartCache(userId);
        }

        CartInfo cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id", skuId));
        //获取到数据  说明已经存在  数量相加
        if (cartInfo != null) {
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);

            cartInfo.setSkuPrice(skuPrice);
            cartInfoMapper.updateById(cartInfo);
        } else {
            CartInfo cartInfo1 = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setUserId(userId);
            cartInfoMapper.insert(cartInfo1);
            cartInfo = cartInfo1;
        }
        //更新redi缓存
        redisTemplate.boundHashOps(cartKey).put(skuId.toString(), cartInfo);
        setCartKeyExpire(cartKey);
    }


    //通过用户ID查询购物车列表
    @Override

    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoList = new ArrayList<>();

        // 用户未登录
        if (StringUtils.isEmpty(userId)) {
            // 获取未登录购物车集合数据
            cartInfoList = getCartList(userTempId);
            // return cartInfoList;
        }
        // 用户登录情况
        if (!StringUtils.isEmpty(userId)) {
            // 登录的时候合并购物车：
            // 获取未登录购物车数据
            List<CartInfo> cartInfoArrayList = this.getCartList(userTempId);
            // 未登录购物车数据不是空的
            if (!CollectionUtils.isEmpty(cartInfoArrayList)) {
                //进行合并
                cartInfoList = this.mergeToCartList(cartInfoArrayList, userId);
                //删除未登陆的购物车
                this.deleteCartList(userTempId);
            }
            if (CollectionUtils.isEmpty(cartInfoArrayList)) {
                cartInfoList = this.getCartList(userId);
            }


        }

        return cartInfoList;

    }


    //更新选中状态
    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.update(cartInfo, new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id", skuId));
        //修改缓存
        String cartKey = this.getCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        if (boundHashOperations.hasKey(skuId.toString())) {
            CartInfo cartInfo1 = (CartInfo) boundHashOperations.get(skuId.toString());
            cartInfo1.setIsChecked(1);
            boundHashOperations.put(skuId.toString(), cartInfo1);
            setCartKeyExpire(cartKey);
        }

    }


    //删除购物车
    @Override
    public void deleteCart(Long skuId, String userId) {
        //删除缓存中数据
        String cartKey = this.getCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        if (boundHashOperations.hasKey(skuId.toString())) {
            boundHashOperations.delete(skuId.toString());
        }

        //删除数据库中数据
        cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id", skuId));


    }


    //根据用户id查询购物车列表
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> list = new ArrayList<>();
        String cartKey = this.getCartKey(userId);

        List<CartInfo> cartCachInfoList = redisTemplate.opsForHash().values(cartKey);

        if (!CollectionUtils.isEmpty(cartCachInfoList)) {
            for (CartInfo cartInfo : cartCachInfoList) {
                if (cartInfo.getIsChecked().intValue() == 1) {
                    list.add(cartInfo);
                }
            }
        }

return list;
    }

    //删除未登陆的购物车
    private void deleteCartList(String userTempId) {

        cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id", userTempId));
        String cartKey = getCartKey(userTempId);
        Boolean hasKey = redisTemplate.hasKey(cartKey);
        if (hasKey) {
            redisTemplate.delete(cartKey);
        }
    }


    //合并用户和临时用户的购物车
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoArrayList, String userId) {
        List<CartInfo> cartList = this.getCartList(userId);
        Map<Long, CartInfo> cartInfoMapLogin = cartList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));

        for (CartInfo cartInfoNoLogin : cartInfoArrayList) {
            Long skuId = cartInfoNoLogin.getSkuId();

            if (cartInfoMapLogin.containsKey(skuId)) {
                CartInfo cartInfo = cartInfoMapLogin.get(skuId);
                cartInfo.setSkuName(cartInfo.getSkuName() + cartInfoNoLogin.getSkuName());

                if (cartInfoNoLogin.getIsChecked().intValue() == 1) {
                    cartInfo.setIsChecked(1);
                }
                cartInfoMapper.updateById(cartInfo);
            } else {
                cartInfoNoLogin.setId(null);
                cartInfoNoLogin.setUserId(userId);
                cartInfoMapper.insert(cartInfoNoLogin);
            }
        }
        //查询数据库
        List<CartInfo> cartInfoList = loadCartCache(userId);

        return cartInfoList;
    }

    //根据用户获取购物车
    private List<CartInfo> getCartList(String userId) {
        List<CartInfo> list = new ArrayList<>();
        if (StringUtils.isEmpty(userId)) return list;

        String cartKey = this.getCartKey(userId);

        list = redisTemplate.opsForHash().values(cartKey);
        if (!CollectionUtils.isEmpty(list)) {
            list.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().toString().compareTo(o2.getId().toString());
                }
            });
            return list;
        } else {
            list = loadCartCache(userId);
            return list;
        }

    }

    //数据库中获取值
    private List<CartInfo> loadCartCache(String userId) {

        List<CartInfo> cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        if (CollectionUtils.isEmpty(cartInfoList)) {//????????????
            return cartInfoList;
        }

        String cartKey = getCartKey(userId);
        HashMap<String, CartInfo> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            map.put(cartInfo.getSkuId().toString(), cartInfo);
        }
        redisTemplate.opsForHash().putAll(cartKey, map);


        return cartInfoList;
    }

    //获取购物车的key过期时间
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    //获取购物车的key
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
