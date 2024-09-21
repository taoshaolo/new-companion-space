package com.taoshao.companionspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.taoshao.companionspace.model.entity.CommentLike;

/**
* @author taoshao
* @description 针对表【comment_like】的数据库操作Mapper
* @createDate 2023-06-08 16:24:28
 * @Entity com.taoshao.companionspace.model.entity.CommentLike
*/
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

}




