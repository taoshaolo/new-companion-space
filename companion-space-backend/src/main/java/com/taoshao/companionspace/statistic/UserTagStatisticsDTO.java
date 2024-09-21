package com.taoshao.companionspace.statistic;

import lombok.Data;

/**
 * 用户标签统计
 *
 * @Author taoshao
 * @Date 2024/7/28
 */
@Data
public class UserTagStatisticsDTO {

    /**
     * 标签
     */
    private String tag;

    /**
     * 标签数量
     */
    private Long tagCount;
}
