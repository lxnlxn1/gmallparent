package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import javafx.scene.CacheHint;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author lxn
 * @create 2020-03-22 22:49
 */

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) throws Throwable {

        Object result = null;
        Object[] args = point.getArgs();

        // 想得到哪些方法上有注解
        // 获取方法上的签名
        MethodSignature signature = (MethodSignature) point.getSignature();

        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);

        String prefix = annotation.prefix();

        String key = prefix + Arrays.asList(args).toString();
        // 正常先查询缓存 {从缓存获取数据：第一必须传递key，第二必须知道缓存中存储的数据类型}
        result = cacheHint(signature, key);
        if (result != null) {
            return result;
        }
        RLock lock = redissonClient.getLock(key + ":lock");
        try {
            boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);

            if (res) {
                result = point.proceed(args);
                if (result == null) {
                    Object o = new Object();
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(o), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    return o;
                } else {
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    return result;
                }
            } else {
                Thread.sleep(1000);
                return cacheHint(signature, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return result;
    }

    private Object cacheHint(MethodSignature signature, String key) {

      String cache =  (String) redisTemplate.opsForValue().get(key);

      if (StringUtils.isNotBlank(cache)){
          Class returnType = signature.getReturnType();
          return JSONObject.parseObject(cache,returnType);
      }
      return null;

    }
}
