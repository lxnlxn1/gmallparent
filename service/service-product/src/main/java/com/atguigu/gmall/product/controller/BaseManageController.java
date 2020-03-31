package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lxn
 * @create 2020-03-13 23:11
 */
@Api(tags = "商品基础属性")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;


//获取一级分类
    @ApiOperation(value = "获取一级分类")
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1(){
        List<BaseCategory1> category1List = manageService.getCategory1();

        return Result.ok(category1List);
    }


    //获取二级分类
    @ApiOperation(value = "获取二级分类")
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable long category1Id){
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    //获取三级分类
    @ApiOperation(value = "获取三级分类")
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable long category2Id){
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    //获取分类id获取平台属性
    @ApiOperation(value = "获取分类id获取平台属性")
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable long category1Id
            ,@PathVariable long category2Id,@PathVariable long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }

    //添加平台属性 ,修改平台属性
    @ApiOperation(value = "添加平台属性")
    @PostMapping("saveAttrInfo")

    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
            manageService.saveAttrInfo(baseAttrInfo);
            return Result.ok();
    }


    //根据平台属性ID获取平台属性

    @ApiOperation(value = "根据平台属性ID获取平台属性")
    @GetMapping("getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>>  getAttrValueList(@PathVariable long attrId){
        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        return Result.ok(attrValueList) ;
    }

    //分页显示
    @ApiOperation(value = "分页显示")
    @GetMapping("{page}/{size}")
    public Result<IPage<SpuInfo>> index(@ApiParam(name = "page",value = "当前页码",required = true) @PathVariable Long page,
                                        @ApiParam(name = "size",value = "每页显示的记录数",required = true) @PathVariable Long size,
                                        @ApiParam(name = "spuInfo",value = "查询对象",required = false) SpuInfo spuInfo
                                      //  @ApiParam(name = "category3Id",value = "3级ID",required = true) @PathVariable long category3Id
    ){
        // 封装分页查询参数
        System.out.println("---------------------");
        Page<SpuInfo> pageParam = new Page<>(page,size);
        System.out.println(spuInfo.getCategory3Id());
        IPage<SpuInfo> spuInfoIPage = manageService.selectPage(pageParam,spuInfo);
        return Result.ok(spuInfoIPage);
    }




}
