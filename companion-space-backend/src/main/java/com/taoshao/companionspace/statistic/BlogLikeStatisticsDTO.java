package com.taoshao.companionspace.statistic;

import lombok.Data;

/**
 * 博客点赞统计
 *
 * @Author taoshao
 * @Date 2024/7/28
 */
@Data
public class BlogLikeStatisticsDTO {

    /**
     * 博客 id
     */
    private Long id;

    /**
     * 点赞数
     */
    private Long likedNum;
}
