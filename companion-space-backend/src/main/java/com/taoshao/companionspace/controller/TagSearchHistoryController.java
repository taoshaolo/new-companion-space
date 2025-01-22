package com.taoshao.companionspace.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.DeleteRequest;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.model.request.TagSearchHistoryQueryRequest;
import com.taoshao.companionspace.model.entity.TagSearchHistory;
import com.taoshao.companionspace.service.TagSearchHistoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 标签筛选记录接口
 *
 * @author taoshao
 */
@RestController
@RequestMapping("/tag_search_history")
public class TagSearchHistoryController {

    @Resource
    private TagSearchHistoryService tagSearchHistoryService;

    /**
     * 创建
     *
     * @param tagSearchHistory
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addTagSearchHistory(@RequestBody TagSearchHistory tagSearchHistory) {
        if (tagSearchHistory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String tagName = tagSearchHistory.getTagName();
        return ResultUtil.success(tagSearchHistoryService.addTagSearchHistory(tagName));
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTagSearchHistory(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        boolean b = tagSearchHistoryService.removeById(id);
        return ResultUtil.success(b);
    }

    /**
     * 分页获取列表
     *
     * @param tagSearchHistoryQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<TagSearchHistory>> listTagSearchHistoryByPage(
            TagSearchHistoryQueryRequest tagSearchHistoryQueryRequest) {
        long current = 1;
        long size = 10;
        QueryWrapper<TagSearchHistory> queryWrapper = new QueryWrapper<>();
        if (tagSearchHistoryQueryRequest != null) {
            // 根据标签名称模糊查询
            String tagName = tagSearchHistoryQueryRequest.getTagName();
            if (StringUtils.isNotBlank(tagName)) {
                queryWrapper.like("tagName", tagName);
            }
        }
        Page<TagSearchHistory> tagSearchHistoryPage = tagSearchHistoryService.page(new Page<>(current, size),
                queryWrapper);
        return ResultUtil.success(tagSearchHistoryPage);
    }

    // endregion
}
