package com.taoshao.companionspace.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.vo.BlogVO;
import com.taoshao.companionspace.model.vo.MessageVO;
import com.taoshao.companionspace.service.MessageService;
import com.taoshao.companionspace.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.taoshao.companionspace.constant.RedisConstant.MESSAGE_BLOG_NUM_KEY;

/**
 * 消息控制器
 *
 * @author taoshao
 * @date 2023/06/22
 */
@RestController
@RequestMapping("/message")
@Api(tags = "消息管理模块")
public class MessageController {

    /**
     * 消息服务
     */
    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;


    /**
     * 获取用户点赞消息数量
     *
     * @param request 请求
     * @return {@link BaseResponse}<{@link Long}>
     */
    @GetMapping("/like/num")
    @ApiOperation(value = "获取用户点赞消息数量")
    public BaseResponse<Long> getUserLikeMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long messageNum = messageService.getLikeNum(loginUser.getId());
        return ResultUtil.success(messageNum);
    }

    /**
     * 分页获取 用户点赞我 消息
     *
     * @param request     请求
     * @param currentPage 当前页码
     * @return {@link BaseResponse}<{@link Page}<{@link MessageVO}>>
     */
    @GetMapping("/like")
    @ApiOperation(value = "获取 点赞我的 消息")
    public BaseResponse<Page<MessageVO>> getUserLikeMessage(HttpServletRequest request, Long currentPage) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Page<MessageVO> messageVoPage = messageService.pageUserLikeMe(loginUser.getId(), currentPage);
        return ResultUtil.success(messageVoPage);
    }

    /**
     * 分页获取 我点赞用户 消息
     * @param request
     * @param currentPage
     * @return
     */
    @GetMapping("/like/my")
    @ApiOperation(value = "获取 我点赞的 消息")
    public BaseResponse<Page<MessageVO>> getMyLikeMessage(HttpServletRequest request, Long currentPage) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Page<MessageVO> messageVoPage = messageService.pageMyLikeUser(loginUser.getId(), currentPage);
        return ResultUtil.success(messageVoPage);
    }


}
