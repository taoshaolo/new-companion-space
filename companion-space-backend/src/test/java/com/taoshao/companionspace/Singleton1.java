package com.taoshao.companionspace;

import org.junit.jupiter.api.Test;

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


    @Test
    void test() {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder append = stringBuilder.append("123");
        System.out.println(append);
        stringBuilder.insert(0, "456");

        System.out.println(stringBuilder);

        int i = append.indexOf("1");
        System.out.println(i);

    }
}
