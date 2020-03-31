package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author lxn
 * @create 2020-03-18 16:34
 */
public interface ItemService {
    Map<String, Object> getBySkuId(Long skuId);
}
