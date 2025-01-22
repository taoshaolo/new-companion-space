package com.taoshao.companionspace.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.DeleteRequest;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.model.request.TagQueryRequest;
import com.taoshao.companionspace.model.entity.Tag;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.enums.TagCategoryEnum;
import com.taoshao.companionspace.service.TagService;
import com.taoshao.companionspace.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签接口
 *
 * @author taoshao
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @Resource
    private UserService userService;

    // TagMap 缓存
    private final Cache<String, Map<String, List<Tag>>> tagMapCache = Caffeine.newBuilder().build();

    // 整个 TagMap 缓存 key
    private static final String FULL_TAG_MAP_KEY = "f";

    /**
     * 创建
     *
     * @param tag
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTag(@RequestBody Tag tag, HttpServletRequest request) {
        if (tag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String tagName = tag.getTagName();
        String category = tag.getCategory();
        if (StringUtils.isAllBlank(tagName, category)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        tagService.count();
        User loginUser = userService.getLoginUser(request);
        tag.setUserId(loginUser.getId());
        boolean result = tagService.save(tag);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 清除缓存
        tagMapCache.invalidate(FULL_TAG_MAP_KEY);
        return ResultUtil.success(tag.getId());
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        boolean b = tagService.removeById(id);
        // 清除缓存
        tagMapCache.invalidate(FULL_TAG_MAP_KEY);
        return ResultUtil.success(b);
    }

    /**
     * 获取所有标签分组
     *
     * @return
     */
    @GetMapping("/get/map")
    public BaseResponse<Map<String, List<Tag>>> getTagMap() {
        List<Tag> tagList = tagService.list();
        if (tagList == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 优先取缓存
        Map<String, List<Tag>> tagMap = tagMapCache.get(FULL_TAG_MAP_KEY, key -> tagList.stream().map(tag -> {
            // 精简
            Tag newTag = new Tag();
            newTag.setTagName(tag.getTagName());
            newTag.setCategory(tag.getCategory());
            newTag.setPostNum(tag.getPostNum());
            return newTag;
            // 按类别分组
        }).collect(Collectors.groupingBy(Tag::getCategory)));
        return ResultUtil.success(tagMap);
    }

    /**
     * 分页查询标签（仅管理员可见）
     *
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Tag>> listTagByPage(TagQueryRequest tagQueryRequest) {
        if (tagQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = tagQueryRequest.getPageNum();
        long size = tagQueryRequest.getPageSize();
        String category = tagQueryRequest.getCategory();
        String tagName = tagQueryRequest.getTagName();
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(category)) {
            queryWrapper.eq("category", category);
        }
        if (StringUtils.isNotBlank(tagName)) {
            queryWrapper.like("tagName", tagName);
        }
        // 默认按帖子使用数降序排序
        queryWrapper.orderByDesc("postNum");
        Page<Tag> tagPage = tagService.page(new Page<>(current, size), queryWrapper);
        return ResultUtil.success(tagPage);
    }

    /**
     * 查询标签（仅管理员可见）
     *
     * @return
     */
    @GetMapping("/category/list")
    public BaseResponse<List<String>> listTagCategory() {
        return ResultUtil.success(TagCategoryEnum.getValues());
    }

}
