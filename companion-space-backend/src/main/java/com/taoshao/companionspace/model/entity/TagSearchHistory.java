package com.taoshao.companionspace.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签筛选记录
 * @TableName tag_search_history
 */
@TableName(value ="tag_search_history")
@Data
public class TagSearchHistory implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 筛选标签名称
     */
    private String tagName;

    /**
     * 次数
     */
    private Integer num;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}