package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

/**
 * @author lxn
 * @create 2020-03-23 23:00
 */
public interface SearchService {

    //上架商品列表
    void upperGoods(Long skuId);

    //下架商品列表
    void lowerGoods(Long skuId);

    //更新热点
    void incrHotScore(Long skuId);

    //搜索列表
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
