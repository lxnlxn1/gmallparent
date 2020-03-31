package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @author lxn
 * @create 2020-03-29 16:58
 */
public interface UserService {
    //登陆
    UserInfo login(UserInfo userInfo);
}
