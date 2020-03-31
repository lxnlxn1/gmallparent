package com.atguigu.gmall.product.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.google.common.annotations.GwtCompatible;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author lxn
 * @create 2020-03-19 15:18
 */

@Api(tags = "前端API调试")
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private ManageService manageService;



    //以下为远程调用Api
    //根据skuId获取  skuInfo对象
@ApiOperation(value = "skuId获取  skuInfo对象")
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){

      return   manageService.getSkuInfo(skuId);
    }


    //通过三级ID获取  BaseCategoryView  信息   ????

    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){

        return manageService.getCategoryViewByCategory3Id(category3Id);
    }

    //根据spuId，skuId 查询销售属性集合
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,@PathVariable Long spuId){
        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    //* 根据spuId 查询map 集合属性
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
      return   manageService.getSkuValueIdsMap(spuId);
    }



    // 商品价格数据
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        return manageService.getSkuPrice(skuId);
    }

    //通过品牌id 来查询数据
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable Long tmId){
    return manageService.getTrademarkByTmId(tmId);
    }


    //根据skuid 查询数据
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){

    return manageService.getAttrList(skuId);
    }

    //获取全部分类信息
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList(){
return Result.ok(manageService.getBaseCategoryList());
    }

}
