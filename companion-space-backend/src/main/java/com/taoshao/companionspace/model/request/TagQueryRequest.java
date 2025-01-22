package com.taoshao.companionspace.model.request;

import com.taoshao.companionspace.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author taoshao
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TagQueryRequest extends PageRequest implements Serializable {

    /**
     * 分类
     */
    private String category;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}