package com.taoshao.companionspace.service.impl;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.constant.ChatConstant;
import com.taoshao.companionspace.constant.UserConstant;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.mapper.ChatMapper;
import com.taoshao.companionspace.model.entity.Chat;
import com.taoshao.companionspace.model.entity.Team;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.ChatRequest;
import com.taoshao.companionspace.model.vo.ChatMessageVo;
import com.taoshao.companionspace.model.vo.WebSocketVo;
import com.taoshao.companionspace.service.ChatService;
import com.taoshao.companionspace.service.TeamService;
import com.taoshao.companionspace.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author taoshao
 * @description 针对表【chat(聊天消息表)】的数据库操作Service实现
 * @createDate 2023-04-11 11:19:33
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
        implements ChatService {

    @Resource
    private RedisTemplate<String, List<ChatMessageVo>> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Override
    public List<ChatMessageVo> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser) {
        Long toId = chatRequest.getToId();
        if (toId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态异常请重试");
        }
        List<ChatMessageVo> chatRecords = getCache(ChatConstant.CACHE_CHAT_PRIVATE, loginUser.getId() + "" + toId);
        if (chatRecords != null) {
            saveCache(ChatConstant.CACHE_CHAT_PRIVATE, loginUser.getId() + "" + toId, chatRecords);
            return chatRecords;
        }
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.
                and(privateChat -> privateChat.eq(Chat::getFromId, loginUser.getId()).eq(Chat::getToId, toId)
                        .or().
                        eq(Chat::getToId, loginUser.getId()).eq(Chat::getFromId, toId)
                ).eq(Chat::getChatType, chatType);
        // 两方共有聊天
        List<Chat> list = this.list(chatLambdaQueryWrapper);
        List<ChatMessageVo> chatMessageVoList = list.stream().map(chat -> {
            ChatMessageVo chatMessageVo = chatResult(loginUser.getId(), toId, chat.getText(), chatType, chat.getCreateTime());
            if (chat.getFromId().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            return chatMessageVo;
        }).collect(Collectors.toList());
        saveCache(ChatConstant.CACHE_CHAT_PRIVATE, loginUser.getId() + "" + toId, chatMessageVoList);
        return chatMessageVoList;
    }

    @Override
    public List<ChatMessageVo> getHallChat(int chatType, User loginUser) {
        List<ChatMessageVo> chatRecords = getCache(ChatConstant.CACHE_CHAT_HALL, String.valueOf(loginUser.getId()));
        if (chatRecords != null) {
            List<ChatMessageVo> chatMessageVos = checkIsMyMessage(loginUser, chatRecords);
            saveCache(ChatConstant.CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVos);
            return chatMessageVos;
        }
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, chatType);
        List<ChatMessageVo> chatMessageVos = returnMessage(loginUser, null, chatLambdaQueryWrapper);
        saveCache(ChatConstant.CACHE_CHAT_HALL, String.valueOf(loginUser.getId()), chatMessageVos);
        return chatMessageVos;
    }

    private List<ChatMessageVo> checkIsMyMessage(User loginUser, List<ChatMessageVo> chatRecords) {
        return chatRecords.stream().peek(chat -> {
            if (chat.getFormUser().getId() != loginUser.getId() && chat.getIsMy()) {
                chat.setIsMy(false);
            }
            if (chat.getFormUser().getId() == loginUser.getId() && !chat.getIsMy()) {
                chat.setIsMy(true);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 获取缓存
     *
     * @param redisKey
     * @param id
     * @return
     */
    @Override
    public List<ChatMessageVo> getCache(String redisKey, String id) {
        ValueOperations<String, List<ChatMessageVo>> valueOperations = redisTemplate.opsForValue();
        List<ChatMessageVo> chatRecords;
        if (redisKey.equals(ChatConstant.CACHE_CHAT_HALL)) {
            chatRecords = valueOperations.get(redisKey);
        } else {
            chatRecords = valueOperations.get(redisKey + id);
        }
        return chatRecords;
    }

    @Override
    public void deleteKey(String key, String id) {
        if (key.equals(ChatConstant.CACHE_CHAT_HALL)) {
            redisTemplate.delete(key);
        } else {
            redisTemplate.delete(key + id);
        }
    }

    /**
     * 保存缓存
     *
     * @param redisKey
     * @param id
     * @param chatMessageVos
     */
    @Override
    public void saveCache(String redisKey, String id, List<ChatMessageVo> chatMessageVos) {
        try {
            ValueOperations<String, List<ChatMessageVo>> valueOperations = redisTemplate.opsForValue();
            // 解决缓存雪崩
            int i = RandomUtil.randomInt(2, 3);
            if (redisKey.equals(ChatConstant.CACHE_CHAT_HALL)) {
                valueOperations.set(redisKey, chatMessageVos, 2 + i / 10, TimeUnit.MINUTES);
            } else {
                valueOperations.set(redisKey + id, chatMessageVos, 2 + i / 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key error");
        }
    }

    @Override
    public ChatMessageVo chatResult(Long userId, Long toId, String text, Integer chatType, Date createTime) {
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        User fromUser = userService.getById(userId);
        User toUser = userService.getById(toId);
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        WebSocketVo toWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        BeanUtils.copyProperties(toUser, toWebSocketVo);
        chatMessageVo.setFormUser(fromWebSocketVo);
        chatMessageVo.setToUser(toWebSocketVo);
        chatMessageVo.setChatType(chatType);
        chatMessageVo.setText(text);
        chatMessageVo.setCreateTime(DateUtil.format(createTime, "yyyy年MM月dd日 HH:mm:ss"));
        return chatMessageVo;
    }

    @Override
    public List<ChatMessageVo> getTeamChat(ChatRequest chatRequest, int chatType, User loginUser) {
        Long teamId = chatRequest.getTeamId();
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        List<ChatMessageVo> chatRecords = getCache(ChatConstant.CACHE_CHAT_TEAM, String.valueOf(teamId));
        if (chatRecords != null) {
            List<ChatMessageVo> chatMessageVos = checkIsMyMessage(loginUser, chatRecords);
            saveCache(ChatConstant.CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVos);
            return chatMessageVos;
        }
        Team team = teamService.getById(teamId);
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType, chatType).eq(Chat::getTeamId, teamId);
        List<ChatMessageVo> chatMessageVos = returnMessage(loginUser, team.getUserId(), chatLambdaQueryWrapper);
        saveCache(ChatConstant.CACHE_CHAT_TEAM, String.valueOf(teamId), chatMessageVos);
        return chatMessageVos;
    }


    private List<ChatMessageVo> returnMessage(User loginUser, Long userId, LambdaQueryWrapper<Chat> chatLambdaQueryWrapper) {
        List<Chat> chatList = this.list(chatLambdaQueryWrapper);
        return chatList.stream().map(chat -> {
            ChatMessageVo chatMessageVo = chatResult(chat.getFromId(), chat.getText());
            boolean isCaptain = userId != null && userId.equals(chat.getFromId());
            if (userService.getById(chat.getFromId()).getUserRole() == UserConstant.ADMIN_ROLE || isCaptain) {
                chatMessageVo.setIsAdmin(true);
            }
            if (chat.getFromId().equals(loginUser.getId())) {
                chatMessageVo.setIsMy(true);
            }
            chatMessageVo.setCreateTime(DateUtil.format(chat.getCreateTime(), "yyyy年MM月dd日 HH:mm:ss"));
            return chatMessageVo;
        }).collect(Collectors.toList());
    }

    /**
     * Vo映射
     *
     * @param userId
     * @param text
     * @return
     */
    public ChatMessageVo chatResult(Long userId, String text) {
        ChatMessageVo chatMessageVo = new ChatMessageVo();
        User fromUser = userService.getById(userId);
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(fromUser, fromWebSocketVo);
        chatMessageVo.setFormUser(fromWebSocketVo);
        chatMessageVo.setText(text);
        return chatMessageVo;
    }
}




