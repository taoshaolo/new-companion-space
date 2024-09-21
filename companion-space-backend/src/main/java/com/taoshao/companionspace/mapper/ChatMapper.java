package com.taoshao.companionspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoshao.companionspace.model.entity.Chat;
import com.taoshao.companionspace.statistic.UserActivityStatisticsDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author taoshao
 * @description 针对表【chat(聊天消息表)】的数据库操作Mapper
 * @createDate 2023-04-11 11:19:33
 * @Entity com.taoshao.companionspace.entity.Chat
 */
public interface ChatMapper extends BaseMapper<Chat> {

    /**
     * 用户活跃度统计(Top 10)
     */
    @Select("select fromId,count(text) as activity\n" +
            "from chat\n" +
            "where chatType = 3\n" +
            "group by fromId\n" +
            "order by activity desc\n" +
            "limit 10")
    List<UserActivityStatisticsDTO> userActivityStatistics();

}




