package com.taoshao.companionspace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.taoshao.companionspace.model.entity.Message;

/**
* @author taoshao
* @description 针对表【message】的数据库操作Mapper
* @createDate 2023-06-21 17:39:30
 * @Entity com.taoshao.companionspace.model.entity.Message
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}




