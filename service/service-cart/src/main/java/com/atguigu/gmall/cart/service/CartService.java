package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author lxn
 * @create 2020-03-29 22:03
 */
public interface CartService {

    //添加购物车
    void addToCart(Long skuId,String userId,Integer skuNum);

    //通过用户ID查询购物车列表
    List<CartInfo> getCartList(String userId,String userTempId);

    //更新选中状态
    void checkCart(String userId,Integer isChecked ,Long skuId);

    //删除购物车
    void deleteCart(Long skuId,String userId);

    //根据用户id查询购物车列表
    List<CartInfo> getCartCheckedList(String userId);
}
