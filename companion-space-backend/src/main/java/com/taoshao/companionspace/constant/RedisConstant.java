package com.taoshao.companionspace.constant;

/**
 * Redis常量
 *
 * @author taoshao
 * @date 2023/06/22
 */
public final class RedisConstant {
    private RedisConstant() {
    }

    /**
     * 博客推送键
     */
    public static final String BLOG_FEED_KEY = "companionspace:feed:blog:";
    /**
     * 新博文消息键
     */
    public static final String MESSAGE_BLOG_NUM_KEY = "companionspace:message:blog:num:";
    /**
     * 新点赞消息键
     */
    public static final String MESSAGE_LIKE_NUM_KEY = "companionspace:message:like:num:";

}
