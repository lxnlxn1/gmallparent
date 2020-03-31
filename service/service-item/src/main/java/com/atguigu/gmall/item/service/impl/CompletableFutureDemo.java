package com.atguigu.gmall.item.service.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author lxn
 * @create 2020-03-24 22:29
 */
public class CompletableFutureDemo {


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
                //int i = 10 / 0;
                return 1024;
            }
        }).thenApply(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer o) {
                System.out.println("thenApply方法，上次返回结果：" + o);
                return  o * 2;
            }
        }).whenComplete(new BiConsumer<Integer, Throwable>() {
            @Override
            public void accept(Integer o, Throwable throwable) {
                System.out.println("-------o=" + o);
                System.out.println("-------throwable=" + throwable);
            }
        }).exceptionally(new Function<Throwable, Integer>() {
            @Override
            public Integer apply(Throwable throwable) {
                System.out.println("throwable=" + throwable);
                return 6666;
            }
        }).handle(new BiFunction<Integer, Throwable, Integer>() {
            @Override
            public Integer apply(Integer integer, Throwable throwable) {
                System.out.println("handle o=" + integer);
                System.out.println("handle throwable=" + throwable);
                return 8888;
            }
        });
        System.out.println(future.get());















//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
//            @Override
//            public Integer get() {
//                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
//                int i = 10 / 0;
//                return 1024;
//            }
//        }).thenApply(new Function<Integer, Integer>() {
//            @Override
//            public Integer apply(Integer o) {
//                System.out.println("thenApply方法，上次返回结果：" + o);
//                return  o * 2;
//            }
//        }).whenComplete(new BiConsumer<Integer, Throwable>() {
//            @Override
//            public void accept(Integer o, Throwable throwable) {
//                System.out.println("-------o=" + o);
//                System.out.println("-------throwable=" + throwable);
//            }
//        }).exceptionally(new Function<Throwable, Integer>() {
//            @Override
//            public Integer apply(Throwable throwable) {
//                System.out.println("throwable=" + throwable);
//                return 6666;
//            }
//        }).handle(new BiFunction<Integer, Throwable, Integer>() {
//            @Override
//            public Integer apply(Integer integer, Throwable throwable) {
//                System.out.println("handle o=" + integer);
//                System.out.println("handle throwable=" + throwable);
//                return 8888;
//            }
//        });
//        System.out.println(future.get());









//        CompletableFuture future = CompletableFuture.supplyAsync(new Supplier<Object>() {
//            @Override
//            public Object get() {
//                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
//               // int i = 10 / 0;
//                return 1024;
//            }
//        }).whenComplete(new BiConsumer<Object, Throwable>() {
//            @Override
//            public void accept(Object o, Throwable throwable) {
//                System.out.println("-------o=" + o.toString());
//                System.out.println("-------throwable=" + throwable);
//            }
//        }).exceptionally(new Function<Throwable, Object>() {
//            @Override
//            public Object apply(Throwable throwable) {
//                System.out.println("throwable=" + throwable);
//                return 6666;
//            }
//        });
//        System.out.println(future.get());
    }
}
