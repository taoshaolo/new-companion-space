package com.taoshao.companionspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.taoshao.companionspace.model.entity.BlogLike;

/**
* @author taoshao
* @description 针对表【blog_like】的数据库操作Mapper
* @createDate 2023-06-05 21:54:55
 * @Entity com.taoshao.companionspace.model.entity.BlogLike
*/
@Mapper
public interface BlogLikeMapper extends BaseMapper<BlogLike> {

}




