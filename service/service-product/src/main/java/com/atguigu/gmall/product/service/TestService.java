package com.atguigu.gmall.product.service;

/**
 * @author lxn
 * @create 2020-03-20 20:56
 */
public interface TestService {
    void testLock();

    void redissonClientTest();

    String readLock();

    String writeLock();

}
