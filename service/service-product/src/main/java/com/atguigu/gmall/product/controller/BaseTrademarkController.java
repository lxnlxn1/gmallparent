package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
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
 * @create 2020-03-15 22:23
 */

@Api(tags = "品牌列表")
@RequestMapping("/admin/product/baseTrademark")
@RestController
public class BaseTrademarkController {


    @Autowired
    private BaseTrademarkService baseTrademarkService;


    /**
     * 分页查询
     *
     * @return
     */
    @ApiOperation(value = "分页查询")
    @GetMapping("{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true) @PathVariable long page,
                        @ApiParam(name = "limit", value = "每页数", required = true) @PathVariable long limit) {
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.selectPage(new Page<BaseTrademark>(page, limit));
        return Result.ok(baseTrademarkIPage);
    }

    @ApiOperation(value = "添加品牌")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @ApiOperation(value = "删除品牌")
    @DeleteMapping("remove/{id}")
    public Result remove(@ApiParam(name = "id", value = "id", required = true) @PathVariable long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }


    @ApiOperation(value = "回显ID")
    @GetMapping("get/{id}")
    public Result get(@PathVariable long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }


    @ApiOperation(value = "修改品牌")
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = baseTrademarkService.getBaseTrademarkList();

        return Result.ok(baseTrademarkList);
    }


}
