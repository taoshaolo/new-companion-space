package com.taoshao.companionspace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taoshao.companionspace.model.entity.Chat;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.ChatRequest;
import com.taoshao.companionspace.model.vo.ChatMessageVo;

import java.util.Date;
import java.util.List;

/**
 * @author taoshao
 * @description 针对表【chat(聊天消息表)】的数据库操作Service
 * @createDate 2023-04-11 11:19:33
 */
public interface ChatService extends IService<Chat> {
    /**
     * 保存缓存
     *
     * @param redisKey
     * @param id
     * @param chatMessageVos
     */
    void saveCache(String redisKey, String id, List<ChatMessageVo> chatMessageVos);

    /**
     * 获取缓存
     *
     * @param redisKey
     * @param id
     * @return
     */
    List<ChatMessageVo> getCache(String redisKey, String id);

    /**
     * 获取私聊聊天内容
     *
     * @param chatRequest
     * @param chatType
     * @param loginUser
     * @return
     */
    List<ChatMessageVo> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser);

    /**
     * 获取大厅聊天纪录
     *
     * @param chatType
     * @param loginUser
     * @return
     */
    List<ChatMessageVo> getHallChat(int chatType, User loginUser);

    /**
     * 聊天记录映射
     *
     * @param fromId
     * @param toId
     * @param text
     * @param chatType
     * @param createTime
     * @return
     */
    ChatMessageVo chatResult(Long fromId, Long toId, String text, Integer chatType, Date createTime);

    /**
     * 队伍聊天室
     *
     * @param chatRequest
     * @param chatType
     * @param loginUser
     * @return
     */
    List<ChatMessageVo> getTeamChat(ChatRequest chatRequest, int chatType, User loginUser);


    /**
     * 删除key
     *
     * @param key
     * @param id
     */
    void deleteKey(String key, String id);
}
