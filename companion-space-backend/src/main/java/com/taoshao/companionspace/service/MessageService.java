package com.taoshao.companionspace.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taoshao.companionspace.model.entity.Message;
import com.taoshao.companionspace.model.vo.BlogVO;
import com.taoshao.companionspace.model.vo.MessageVO;

import java.util.List;

/**
 * 消息服务
 *
 * @author taoshao
 * @description 针对表【message】的数据库操作Service
 * @createDate 2023-06-21 17:39:30
 * @date 2024/01/25
 */
public interface MessageService extends IService<Message> {


    /**
     * 获取点赞数量
     *
     * @param userId 用户id
     * @return long
     */
    long getLikeNum(Long userId);

    /**
     * 点赞
     *
     * @param userId 用户id
     * @return {@link List}<{@link MessageVO}>
     */
    List<MessageVO> getLike(Long userId);

    /**
     * 收到用户博客
     *
     * @param userId 用户id
     * @return {@link List}<{@link BlogVO}>
     */
    List<BlogVO> getUserBlog(Long userId);



    /**
     * 分页获取 用户点赞我 消息
     *
     * @param userId      用户id
     * @param currentPage 当前页码
     * @return {@link Page}<{@link MessageVO}>
     */
    Page<MessageVO> pageUserLikeMe(Long userId, Long currentPage);

    /**
     * 分页获取 我点赞用户 消息
     *
     * @param userId
     * @param currentPage
     * @return
     */
    Page<MessageVO> pageMyLikeUser(Long userId, Long currentPage);
}
