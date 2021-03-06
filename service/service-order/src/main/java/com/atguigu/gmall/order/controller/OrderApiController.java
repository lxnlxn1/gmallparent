package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lxn
 * @create 2020-03-30 23:19
 */

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderService orderService;


    /**
     * 确认订单
     *
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) {
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);

        //获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

        //获取货物清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);

        //声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            detailArrayList.add(orderDetail);
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();


        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressList);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalNum", detailArrayList.size());
        result.put("totalAmount", orderInfo.getTotalAmount());

        String tradeNo = orderService.getTradeNo(userId);
        result.put("tradeNo", tradeNo);


        return Result.ok(result);
    }


    //提交订单
    @PostMapping("auth/submitOrder")
    public Result auth(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {


        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));


        Long orderId = orderService.saveOrderInfo(orderInfo);

        String tradeNo = request.getParameter("tradeNo");
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag) {
            // 比较失败！
            return Result.fail().message("不能重复提交订单！");
        }

        orderService.deleteTradeNo(userId);
        return Result.ok(orderId);

    }
}
