package com.taoshao.companionspace.model.request;

import com.taoshao.companionspace.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: taoshao
 * @Date: 2023年03月25日 18:00
 * @Version: 1.0
 * @Description:
 */
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 8245489531033247232L;
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 搜索关键词
     */
    private String searchText;
}
