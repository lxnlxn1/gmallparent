package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxn
 * @create 2020-03-29 22:01
 */

@Mapper
public interface CartInfoMapper extends BaseMapper<CartInfo> {
}
