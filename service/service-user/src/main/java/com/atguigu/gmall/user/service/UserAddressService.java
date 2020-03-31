package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author lxn
 * @create 2020-03-30 21:35
 */
public interface UserAddressService extends IService<UserAddress> {

    //根据用户id查询收货地址列表

    List<UserAddress> findUserAddressListByUserId(String userId);
}
