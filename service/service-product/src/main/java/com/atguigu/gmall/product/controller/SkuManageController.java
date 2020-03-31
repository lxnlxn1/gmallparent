package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lxn
 * @create 2020-03-16 21:49
 */

@Api("商品sku属性")
@RestController
@RequestMapping("admin/product/")
public class SkuManageController {

    @Autowired
    private ManageService manageService;


    //用于回显
    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId){
           return Result.ok(manageService.getSpuImageList(spuId));
    }

    //回显spu数据
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId){
        return Result.ok(manageService.getSpuSaleAttrList(spuId));
    }


    //保存
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);

        return Result.ok();
    }


    @GetMapping("list/{page}/{limite}")
    public Result list(@PathVariable Long page,@PathVariable Long limite){

        return Result.ok(manageService.selectPage(new Page<SkuInfo>(page,limite)));
    }


    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        manageService.onSale(skuId);
        return Result.ok();
    }

    //下架  cancelSale/13
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        manageService.cancelSale(skuId);
        return Result.ok();
    }


}
