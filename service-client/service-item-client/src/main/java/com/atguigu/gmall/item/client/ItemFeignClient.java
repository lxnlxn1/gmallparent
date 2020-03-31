package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lxn
 * @create 2020-03-19 20:41
 */

@FeignClient(value = "service-item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {



    // 把service-item 中的服务暴露出来
    // 根据skuId 查询数据
    @RequestMapping("api/item/{skuId}")
    Result getItem(@PathVariable Long skuId);

}
