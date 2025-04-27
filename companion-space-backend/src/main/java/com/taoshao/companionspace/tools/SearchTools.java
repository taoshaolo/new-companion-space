package com.taoshao.companionspace.tools;

import com.taoshao.companionspace.service.UserService;
import dev.langchain4j.agent.tool.Tool;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 查询小伙伴、队伍、文章
 * @author taoshao
 * @date 2025/4/26
 */
@Component
public class SearchTools {

    @Resource
    private UserService usersService;

    @Tool("根据标签搜索用户")
    Object SearchUsersByTags(Set<String> tags) {
        return usersService.searchUserByTags(tags);
    }

    //todo

}
