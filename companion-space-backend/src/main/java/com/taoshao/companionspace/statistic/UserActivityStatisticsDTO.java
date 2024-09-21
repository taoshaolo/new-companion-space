package com.taoshao.companionspace.statistic;

import lombok.Data;

/**
 * 用户活跃度统计
 *
 * @Author taoshao
 * @Date 2024/7/28
 */
@Data
public class UserActivityStatisticsDTO {

    private String fromId;

    private Long activity;
}
