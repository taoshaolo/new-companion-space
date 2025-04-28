package com.taoshao.companionspace.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.model.entity.ChatMemory;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.vo.ChatMemoryVO;
import com.taoshao.companionspace.service.ChatMemoryService;
import com.taoshao.companionspace.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * AI聊天记忆(ChatMemory)表控制层
 *
 * @author makejava
 * @since 2025-04-27 19:46:16
 */
@RestController
@RequestMapping("/chatMemory")
public class ChatMemoryController {
    /**
     * 服务对象
     */
    @Resource
    private ChatMemoryService chatMemoryService;


    /**
     * 查询所有自己的会话
     *
     ** @return 所有数据
     */
    @GetMapping
    public BaseResponse<List<ChatMemoryVO>> selectAll(HttpServletRequest request) {

        return ResultUtil.success(this.chatMemoryService.selectAll(request));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param memoryId
     * @return 单条数据
     */
    @GetMapping("{memoryId}")
    public BaseResponse<ChatMemory> selectOne(@PathVariable String memoryId) {
        return ResultUtil.success(this.chatMemoryService.selectOne(memoryId));
    }

    /**
     * 新增数据
     *
     * @param chatMemory 实体对象
     * @return 新增结果
     */
    @PostMapping
    public BaseResponse insert(@RequestBody ChatMemory chatMemory,  HttpServletRequest request) {
        return ResultUtil.success(this.chatMemoryService.saveChatMemory(chatMemory,request));
    }

    /**
     * 修改数据
     *
     * @param chatMemory 实体对象
     * @return 修改结果
     */
    @PutMapping
    public BaseResponse update(@RequestBody ChatMemory chatMemory) {
        return ResultUtil.success(this.chatMemoryService.updateById(chatMemory));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public BaseResponse delete(@RequestParam("idList") List<Long> idList) {
        return ResultUtil.success(this.chatMemoryService.removeByIds(idList));
    }
}

