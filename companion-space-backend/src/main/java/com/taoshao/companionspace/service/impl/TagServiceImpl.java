package com.taoshao.companionspace.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoshao.companionspace.mapper.TagMapper;
import com.taoshao.companionspace.model.entity.Tag;
import com.taoshao.companionspace.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author taoshaoli
* @description 针对表【tag(标签)】的数据库操作Service实现
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




