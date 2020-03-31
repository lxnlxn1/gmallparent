package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.sun.org.apache.regexp.internal.RE;
import jdk.nashorn.internal.ir.IfNode;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lxn
 * @create 2020-03-27 23:49
 */

@Controller
@RequestMapping
public class ListController {


    @Autowired
    private ListFeignClient listFeignClient;

    /**
     * 列表搜索
     *
     * @param searchParam
     * @return
     */
    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        Result<Map> list = listFeignClient.list(searchParam);
        model.addAllAttributes(list.getData());


        //拼接url
        String urlParam = makeUrlParam(searchParam);
        //处理品牌会显
        String trademarkParam = this.makeTrademark(searchParam.getTrademark());
        //处理平台属性回显
        List<Map<String, String>> propsParamList = this.makeProps(searchParam.getProps());
        //排序方法
        Map<String,Object> orderMap = this.dealOrder(searchParam.getOrder());

        model.addAttribute("searchParam", searchParam);
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("trademarkParam",trademarkParam);
        model.addAttribute("propsParamList",propsParamList);

        model.addAttribute("orderMap",orderMap);

        return "list/index";

    }


    //拼接url
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();

        if (searchParam.getKeyword() != null) {
            urlParam.append("keywprd=").append(searchParam.getKeyword());
        }
        if (searchParam.getCategory1Id() != null) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if (searchParam.getCategory2Id() != null) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (searchParam.getCategory3Id() != null) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (searchParam.getTrademark() != null) {
            if (urlParam.length() > 0) {
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }

        }

        //判断平台属性值
        if (null != searchParam.getProps()) {
            for (String prop : searchParam.getProps()) {
                if (urlParam.length() > 0) {
                    urlParam.append("&props=").append(prop);
                }

            }
        }


        return "list.html?" + urlParam.toString();

    }

    //处理品牌回显
    private String makeTrademark(String trademark) {
        if (StringUtils.isNotBlank(trademark)) {
            String[] split = StringUtils.split(trademark, ":");
            if (split != null && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return "";
    }

    //处理平台属性

    private List<Map<String, String>> makeProps(String[] props) {
        List<Map<String, String>> list = new ArrayList<>();
        if (props != null && props.length != 0) {
            for (String prop : props) {
                String[] split = StringUtils.split(prop, ":");
                if (split != null && split.length == 3) {
                    Map<String, String> map = new HashMap<>();
                    map.put("attrId", split[0]);
                    map.put("attrValue", split[1]);
                    map.put("attrName", split[2]);
                    list.add(map);
                }
            }
        }
        return list;
    }

    //排序显示商品
    private Map<String,Object> dealOrder(String order){
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(order)){
            String[] split = StringUtils.split(order, ":");
            if (split!=null&&split.length==2){
                //传递哪个字段
                map.put("type",split[0]);
                //升序，升序
                map.put("sort",split[1]);
            }
        }else {
            //传递哪个字段
            map.put("type","1");
            //升序，升序
            map.put("sort","asc");
        }
        return map;
    }


}

