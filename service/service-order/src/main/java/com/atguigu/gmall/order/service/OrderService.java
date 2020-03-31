package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author lxn
 * @create 2020-03-31 21:36
 */
public interface OrderService extends IService<OrderInfo> {

    //保存订单
    Long saveOrderInfo(OrderInfo orderInfo);


    //生产流水号
    String getTradeNo(String userId);

    //比较流水号
    boolean checkTradeCode(String userId, String tradeCodeNo);
    //删除流水号

    void deleteTradeNo(String userId);

    //验证库存

}
