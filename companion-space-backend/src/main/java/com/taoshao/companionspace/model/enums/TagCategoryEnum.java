package com.taoshao.companionspace.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签分类枚举
 *
 * @author taoshao
 */
public enum TagCategoryEnum {

    /**
     *
     */
    GENDER("性别"),
    GRADE("年级"),
    SPORT("运动"),
    STATUS("状态"),
    LEARNING_DIRECTION("学习方向"),
    INTEREST("兴趣爱好");

    private final String value;

    TagCategoryEnum(String value) {
        this.value = value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public String getValue() {
        return value;
    }
}
