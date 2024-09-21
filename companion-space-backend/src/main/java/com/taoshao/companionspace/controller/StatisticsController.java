package com.taoshao.companionspace.controller;

import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.mapper.BlogMapper;
import com.taoshao.companionspace.mapper.ChatMapper;
import com.taoshao.companionspace.mapper.UserMapper;
import com.taoshao.companionspace.statistic.BlogLikeStatisticsDTO;
import com.taoshao.companionspace.statistic.UserActivityStatisticsDTO;
import com.taoshao.companionspace.statistic.UserTagStatisticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统计分析接口
 * @Author taoshao
 * @Date 2024/9/19
 */
@RestController
@RequestMapping("/statistic")
@Slf4j
public class StatisticsController {
    @Resource
    private ChatMapper chatMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private BlogMapper blogMapper;

    /**
     * 统计标签（top 10）
     * @return
     */
    @GetMapping("/userTag")
    public BaseResponse<List<UserTagStatisticsDTO>> userTagStatistics(){
        return ResultUtil.success(userMapper.userTagStatisticsDTO());
    }

    /**
     * 统计用户活跃度（top 10）
     * @return
     */
    @GetMapping("/userActivity")
    public BaseResponse<List<UserActivityStatisticsDTO>> userActivityStatistics(){
        return ResultUtil.success(chatMapper.userActivityStatistics());
    }

    /**
     * 统计博客点赞数（top 10）
     * @return
     */
    @GetMapping("/blogLike")
    public BaseResponse<List<BlogLikeStatisticsDTO>> blogLikeStatistics(){
        return ResultUtil.success(blogMapper.blogLikeStatistics());
    }
}
