package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lxn
 * @create 2020-03-16 19:29
 */
@Api(tags = "商品属性SPU管理")
@RequestMapping("admin/product")
@RestController
public class SpuManageController {

    @Autowired
    private ManageService manageService;


    //返回商品属性SPU属性值

    @GetMapping("baseSaleAttrList")
    public Result<List<BaseSaleAttr>> getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();

        // 返回数据
        return Result.ok(baseSaleAttrList);
    }

    //添加商品属性SPU管理   saveSkuInfo
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }


}
