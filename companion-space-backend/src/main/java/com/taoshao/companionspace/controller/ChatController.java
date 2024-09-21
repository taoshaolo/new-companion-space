package com.taoshao.companionspace.controller;

import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.constant.ChatConstant;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.manager.AiManager;
import com.taoshao.companionspace.manager.RedisLimiterManager;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.ChatRequest;
import com.taoshao.companionspace.model.vo.ChatMessageVo;
import com.taoshao.companionspace.service.ChatService;
import com.taoshao.companionspace.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author: taoshao
 * @Date: 2023年04月11日 11:37
 * @Version: 1.0
 * @Description:
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatService chatService;
    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    /**
     * 私聊
     * @param chatRequest
     * @param request
     * @return
     */
    @PostMapping("/privateChat")
    public BaseResponse<List<ChatMessageVo>> getPrivateChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        User loginUser = userService.getLoginUser(request);
        List<ChatMessageVo> privateChat = chatService.getPrivateChat(chatRequest, ChatConstant.PRIVATE_CHAT, loginUser);
        return ResultUtil.success(privateChat);
    }

    /**
     * 大厅聊天
     * @param request
     * @return
     */
    @GetMapping("/hallChat")
    public BaseResponse<List<ChatMessageVo>> getHallChat(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<ChatMessageVo> hallChat = chatService.getHallChat(ChatConstant.HALL_CHAT, loginUser);
        return ResultUtil.success(hallChat);
    }

    /**
     * 群聊
     * @param chatRequest
     * @param request
     * @return
     */
    @PostMapping("/teamChat")
    public BaseResponse<List<ChatMessageVo>> getTeamChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求有误");
        }
        User loginUser = userService.getLoginUser(request);
        List<ChatMessageVo> teamChat = chatService.getTeamChat(chatRequest, ChatConstant.TEAM_CHAT, loginUser);
        return ResultUtil.success(teamChat);
    }


    @PostMapping("/aiChat")
    public BaseResponse<String> getAiChat(@RequestBody String message, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (message.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("aiChat" + loginUser.getId());

        String aiMessage = aiManager.doRequest(message);
        return ResultUtil.success(aiMessage);
    }

}
