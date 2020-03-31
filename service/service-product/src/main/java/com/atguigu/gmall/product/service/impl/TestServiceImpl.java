package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author lxn
 * @create 2020-03-20 20:57
 */
@Service
public class TestServiceImpl implements TestService {

    @Autowired
   private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;




    @Override
    public void redissonClientTest() {

        RLock lock = redissonClient.getLock("lock");
        lock.lock(10,TimeUnit.SECONDS);

        String value = redisTemplate.opsForValue().get("num");
        if (StringUtils.isEmpty(value)){
            return;
        }
        int num = Integer.parseInt(value);
        redisTemplate.opsForValue().set("num",String.valueOf(++num));

       // lock.unlock();


    }

    //读锁
    @Override
    public String readLock() {

        RReadWriteLock readwriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readwriteLock.readLock();
        rLock.lock(10,TimeUnit.SECONDS);
        String msg = redisTemplate.opsForValue().get("msg");
        return msg;
    }
    //写锁
    @Override
    public String writeLock() {
        RReadWriteLock readwriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock writeLock = readwriteLock.writeLock();
        writeLock.lock(10,TimeUnit.SECONDS);
redisTemplate.opsForValue().set("msg",UUID.randomUUID().toString());
        return "成功写入了内容。。。。。。";
    }

    @Override
    public synchronized void testLock() {

    String uuid = UUID.randomUUID().toString();

        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,2, TimeUnit.SECONDS);

        if (lock){
            String value = redisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(value)){
                return;
            }
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num",String.valueOf(++num));

            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Long.class);
            redisScript.setScriptText(script);

            redisTemplate.execute(redisScript, Arrays.asList("lock"),uuid);
//            if (uuid.equals(redisTemplate.opsForValue().get("lock")))
//            redisTemplate.delete("lock");
        }else {
            try {
                Thread.sleep(1000);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



       // System.out.println("------------"+num);


    }


}
