package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author lxn
 * @create 2020-03-13 22:33
 */


//接口方法
public interface ManageService {

    /**
     * 返回所有一级分类信息
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级ID   获取二级信息
     * @param Category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long Category1Id);

    /**
     * 根据二级ID   获取三级信息
     * @param Category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long Category2Id);

    /**
     * 根据  传入ID  查询数据  BaseAttrInfo里面封装了 BaseAttrValue
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo>  getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getAttrInfo(long attrId);

    IPage<SpuInfo> selectPage(Page page, SpuInfo spuInfo);


    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList(Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    //前台商品属性sku展示
    IPage<SkuInfo> selectPage(Page<SkuInfo> page);

    //商品上架
    void onSale(Long skuId);
    //商品下架
    void cancelSale(Long skuId);



    //以下为远程调用Api
    //根据skuId获取  skuInfo对象
    SkuInfo getSkuInfo(Long skuId);

    //通过三级ID获取  BaseCategoryView  信息
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    //获取价格信息
    BigDecimal getSkuPrice(Long skuId);

    // <!--根据skuId，spuId 查询数据-->
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId,Long spuId);


    //根据spuId查询map集合
    Map getSkuValueIdsMap(Long spuId);

    //获取全部分类信息
    List<JSONObject> getBaseCategoryList();



    //根据品牌id来查询数据
    BaseTrademark getTrademarkByTmId(Long tmId);


    //根据skuid 查询数据
    List<BaseAttrInfo> getAttrList(Long skuId);





}
