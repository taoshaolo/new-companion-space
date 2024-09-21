package com.taoshao.companionspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoshao.companionspace.statistic.BlogLikeStatisticsDTO;
import org.apache.ibatis.annotations.Mapper;
import com.taoshao.companionspace.model.entity.Blog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author taoshao
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2023-06-03 15:54:34
 * @Entity com.taoshao.companionspace.model.entity.Blog
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    /**
     * 博客点赞统计（top 10）
     * @return
     */
    @Select("select id,likedNum\n" +
            "from blog\n" +
            "order by likedNum desc")
    List<BlogLikeStatisticsDTO> blogLikeStatistics();
}




