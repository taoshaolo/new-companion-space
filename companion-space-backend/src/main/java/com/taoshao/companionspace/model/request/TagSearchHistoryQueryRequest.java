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
public class TagSearchHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * 筛选标签名称
     */
    private String tagName;

    private static final long serialVersionUID = 1L;
}