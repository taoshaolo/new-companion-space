package com.taoshao.companionspace.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI聊天记忆
 *
 * @TableName chat_memory
 */
@Data
public class ChatMemoryVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 聊天记忆的名称
     */
    private String name;

    /**
     * 聊天记忆的唯一标识符
     */
    private String memoryId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 更新时间时间
     */
    private Date updateTime;

}