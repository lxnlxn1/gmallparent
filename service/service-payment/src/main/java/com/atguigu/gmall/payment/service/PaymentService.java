package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * @author lxn
 * @create 2020-04-01 10:51
 */


public interface PaymentService {


        /**
         * 保存交易记录
         * @param orderInfo
         * @param paymentType 支付类型（1：微信 2：支付宝）
         */
        void savePaymentInfo(OrderInfo orderInfo, String paymentType);
}
