package com.taoshao.companionspace.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @Author taoshao
 * @Date 2024/6/15
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void test() {

    }

}


/**
 * 单例模式（懒汉式和线程同步）
 */
class Singleton1 {
    private static Singleton1 instance;

    private Singleton1() {
    }

    public static synchronized Singleton1 getInstance() {
        if (instance == null) {
            instance = new Singleton1();
        }
        return instance;
    }

}

/**
 * 单例模式（饿汉式）
 */
class Singleton2 {
    private static Singleton2 instance = new Singleton2();

    private Singleton2(){
    }

    public static Singleton2 getInstance(){
        return instance;
    }
}

/**
 * 单例模式（使用双重检查锁定）
 */
class Singleton3 {
    // 私有静态实例变量，使用 volatile 确保多线程环境下的可见性
    private static volatile Singleton3 instance;

    // 私有构造方法
    private Singleton3() {
    }

    // 提供一个公共的静态方法，用于获取实例
    public static Singleton3 getInstance() {
        // 第一次检查，避免不必要的同步
        if (instance == null) {
            // 同步块，只有第一次创建实例才会进入
            synchronized (Singleton3.class) {
                // 第二次检查，确保只有一个实例被创建
                if (instance == null) {
                    instance = new Singleton3();
                }
            }
        }
        return instance;
    }
}

