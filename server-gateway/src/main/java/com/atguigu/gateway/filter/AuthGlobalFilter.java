package com.atguigu.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author lxn
 * @create 2020-03-29 17:54
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    //匹配路径工具类


    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取url
        String path = request.getURI().getPath();
        //如果是内部接口，则网关拦截不允许访问
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //获取用户id
        String userId = getUserId(request);
        String userTempId = getUserTempId(request);


        //用户验证
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        // 验证用户请求的控制器  list.gmall.com/trade.html?catagory3Id=61
        // authUrls = trade.html,myOrder.html,list.html
        // 在配置文件中配置了，访问的控制器条件
        if (!StringUtils.isEmpty(authUrls)) {
            // 循环判断
            for (String authUrl : authUrls.split(",")) {
                // 判断格式
                // path 中存在{trade.html,myOrder.html,list.html} 并且 用户Id为空的情况下 未登录
                if (path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)) {
                    // 如果匹配正确，说明是内部接口，给一个响应提示没有权限访问！
                    ServerHttpResponse response = exchange.getResponse();
                    // 303 表示请求对赢的资源存在着另一个url ，应该重定向
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    // 重定向到登录页面
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                    // 表示设置完成
                    return response.setComplete();
                }
            }
        }

        if (!StringUtils.isEmpty(userId)||!StringUtils.isEmpty(userTempId)) {
            if (!StringUtils.isEmpty(userId)){
                request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)){
                request.mutate().header("userTempId", userTempId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }


        return chain.filter(exchange);
    }





    // 获取用户临时Id
    private String getUserTempId(ServerHttpRequest request) {

        String userTempId = "";
        List<String> tokenList = request.getHeaders().get("userTempId");
        if (tokenList != null && tokenList.size() > 0) {
            userTempId = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie cookie = cookies.getFirst("userTempId");
            if (cookie != null) {
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }


    // 获取用户Id
    private String getUserId(ServerHttpRequest request) {

        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if (tokenList != null && tokenList.size() > 0) {
            token = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie cookie = cookies.getFirst("token");
            if (cookie != null) {
                token = URLDecoder.decode(cookie.getValue());
            }
        }
        if (!StringUtils.isEmpty(token)) {
            // 拼接缓存中的key
            String userKey = "user:login:" + token;
            // 从缓存中获取数据
            String userId = redisTemplate.opsForValue().get(userKey).toString();
            return userId;
        }
        return null;
    }

    //返回没有权限
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        Result<Object> result = Result.build(null, permission);

        byte[] bytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);

        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        return response.writeWith(Mono.just(wrap));
    }
}
