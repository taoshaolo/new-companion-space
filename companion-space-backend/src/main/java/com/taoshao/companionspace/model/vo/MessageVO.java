package com.taoshao.companionspace.model.vo;

import com.taoshao.companionspace.model.entity.Message;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息vo
 *
 * @author taoshao
 * @date 2023/06/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "消息返回")
public class MessageVO extends Message {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = 4353136955942044222L;
    /**
     * 发送用户
     */
    @ApiModelProperty(value = "发送用户的信息")
    private UserVO fromUser;
    /**
     * 接收用户
     */
    @ApiModelProperty(value = "接收用户的信息")
    private UserVO toUser;
    /**
     * 博客
     */
    @ApiModelProperty(value = "博客")
    private BlogVO blog;
    /**
     * 评论
     */
    @ApiModelProperty(value = "评论")
    private BlogCommentsVO comment;
}
