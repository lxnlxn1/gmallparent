package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author lxn
 * @create 2020-03-18 21:35
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
   private ItemService itemService;

    @RequestMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId){
     Map<String,Object> result = itemService.getBySkuId(skuId);
        System.out.println(result+"-----------------------------");
     return Result.ok(result);

    }
}
