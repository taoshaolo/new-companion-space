package com.taoshao.companionspace.constant;

/**
 * redisson常量
 *
 * @author taoshao
 * @date 2023/06/22
 */
public final class RedissonConstant {
    private RedissonConstant() {
    }

    /**
     * 博客点赞锁
     */
    public static final String BLOG_LIKE_LOCK = "companionspace:blog:like:lock:";
    /**
     * 评论点赞锁
     */
    public static final String COMMENTS_LIKE_LOCK = "companionspace:comments:like:lock:";
    /**
     * 默认等待时间
     */
    public static final long DEFAULT_WAIT_TIME = 0;
    /**
     * 违约租赁时间
     */
    public static final long DEFAULT_LEASE_TIME = -1;

}
