package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author lxn
 * @create 2020-03-31 21:37
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {


    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //保存订单

    @Override
    public Long saveOrderInfo(OrderInfo orderInfo) {

        orderInfo.sumTotalAmount();

        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());

        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setCreateTime(new Date());
//定义为1天
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(instance.getTime());

        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());

        //获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        StringBuffer tradeBody = new StringBuffer();

        for (OrderDetail orderDetail : orderDetailList) {
            tradeBody.append(orderDetail.getSkuName());
        }
        if (tradeBody.toString().length()>100){
            orderInfo.setTradeBody(tradeBody.toString().substring(0,100));
        }else {
            orderInfo.setTradeBody(tradeBody.toString());
        }
        // 设置进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            // 插入数据
            orderDetailMapper.insert(orderDetail);
        }

        return orderInfo.getId();
    }




    @Override
    public String getTradeNo(String userId) {
        String tradeNoKey = "user:"+userId+":tradeCode";
      String  tradeNo = UUID.randomUUID().toString().replace("-","");
redisTemplate.opsForValue().set(tradeNoKey,tradeNo);
        return tradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        String tradeNoKey = "user:"+userId+":tradeCode";
        String redisTradeNo  = (String) redisTemplate.opsForValue().get(tradeNoKey);


        return tradeCodeNo.equals(redisTradeNo);
    }

    @Override
    public void deleteTradeNo(String userId) {
        String tradeNoKey = "user:"+userId+":tradeCode";
        redisTemplate.delete(tradeNoKey);
    }
}
