package com.taoshao.companionspace;

import cn.hutool.core.lang.Singleton;

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