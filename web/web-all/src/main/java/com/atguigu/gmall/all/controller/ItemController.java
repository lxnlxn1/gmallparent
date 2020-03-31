package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.function.ServerResponse;


import java.util.Map;

/**
 * @author mqx
 * @date 2020/3/18 11:55
 */
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;


    //获取单个页面
    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model){
        // Map<String, Object> result = itemService.getBySkuId(skuId);
        Result<Map> result = itemFeignClient.getItem(skuId);
        System.out.println(result+"-----------------");
        // 保存数据 将map 中所有的数据都保存起来！
        model.addAllAttributes(result.getData());
        return "item/index";
    }






}
