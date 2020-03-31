package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author lxn
 * @create 2020-03-15 22:36
 */
//public interface BaseTrademarkService extends IService<BaseTrademark> {
public interface BaseTrademarkService extends IService<BaseTrademark> {
    IPage<BaseTrademark> selectPage(Page<BaseTrademark> baseTrademarkPage);

    List<BaseTrademark> getBaseTrademarkList();

}
