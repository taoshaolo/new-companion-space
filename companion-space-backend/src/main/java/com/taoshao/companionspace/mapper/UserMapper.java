package com.taoshao.companionspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.statistic.UserTagStatisticsDTO;

import java.util.List;

/**
 * @author taoshao
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2023-03-08 23:14:16
 * @Entity com.taoshao.companionspace.entity.User
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 用户标签统计（top 10）
     * @return
     */
    List<UserTagStatisticsDTO> userTagStatisticsDTO();

}




