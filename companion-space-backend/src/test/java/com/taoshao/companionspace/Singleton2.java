package com.taoshao.companionspace;

/**
 * 单例模式（饿汉式）
 */
class Singleton2 {
    private static Singleton2 instance = new Singleton2();

    private Singleton2() {
    }

    public static Singleton2 getInstance() {
        return instance;
    }
}
