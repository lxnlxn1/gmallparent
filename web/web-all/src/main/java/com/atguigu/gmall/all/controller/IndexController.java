package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author lxn
 * @create 2020-03-26 20:46
 */


@Controller
@RequestMapping
public class IndexController {


    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private SpringTemplateEngine templateEngine;


    //生成静态页面
    @GetMapping("/createHtml")
    public Result createHtml() throws IOException {
        Result result  = productFeignClient.getBaseCategoryList();
        Context context = new Context();
        context.setVariable("list",result.getData());
        FileWriter fileWriter = new FileWriter("e:\\index.html");
        templateEngine.process("index/index.html",context,fileWriter);
        return Result.ok();
    }

    //访问主页方法
    @GetMapping({"/","index.html"})
    public String idnex(){
        return "index";
    }

//    //从缓存获取值 动态渲染？？？？？？？
//    @GetMapping({"/","index.html"})
//    public String idnex(HttpServletRequest request){
//        Result baseCategoryList = productFeignClient.getBaseCategoryList();
//        request.setAttribute("list",baseCategoryList);
//        return "index/index";
//    }

}
