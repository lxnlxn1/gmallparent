package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author lxn
 * @create 2020-03-29 17:00
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
   private UserInfoMapper userInfoMapper;

    //登陆
    @Override
    public UserInfo login(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        System.out.println(newPasswd+"--------密码-----");
        UserInfo userInfo1 = userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("login_name", userInfo.getLoginName()).eq("passwd", newPasswd));
        if (userInfo1!=null){
            return userInfo1;
        }

        return null;
    }
}
