package com.taoshao.companionspace.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.BlogAddRequest;
import com.taoshao.companionspace.model.request.BlogUpdateRequest;
import com.taoshao.companionspace.model.vo.BlogVO;
import com.taoshao.companionspace.service.BlogService;
import com.taoshao.companionspace.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 博客控制器
 *
 * @author taoshao
 * @date 2023/06/11
 */
@RestController
@RequestMapping("/blog")
@Api(tags = "博文管理模块")
@Log4j2
public class BlogController {
    /**
     * 博客服务
     */
    @Resource
    private BlogService blogService;

    /**
     * 用户服务
     */
    @Resource
    private UserService userService;

    /**
     * 博客列表页面
     *
     * @param currentPage 当前页面
     * @param title 题目
     * @param request     请求
     * @return {@link BaseResponse}<{@link Page}<{@link BlogVO}>>
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取博文")
    public BaseResponse<Page<BlogVO>> listBlogPage(long currentPage, String title, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return ResultUtil.success(blogService.pageBlog(currentPage, title, null));
        } else {
            return ResultUtil.success(blogService.pageBlog(currentPage, title, loginUser.getId()));
        }
    }

    /**
     * 添加博客
     *
     * @param blogAddRequest 博客添加请求
     * @param request        请求
     * @return {@link BaseResponse}<{@link String}>
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加博文")
    public BaseResponse<String> addBlog(BlogAddRequest blogAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (StringUtils.isAnyBlank(blogAddRequest.getTitle(), blogAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        blogService.addBlog(blogAddRequest, loginUser);
        return ResultUtil.success("添加成功");
    }

    /**
     * 我博客列表
     *
     * @param currentPage 当前页面
     * @param request     请求
     * @return {@link BaseResponse}<{@link Page}<{@link BlogVO}>>
     */
    @GetMapping("/list/my/blog")
    @ApiOperation(value = "获取我写的博文")
    public BaseResponse<Page<BlogVO>> listMyBlogs(long currentPage, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Page<BlogVO> blogPage = blogService.listMyBlogs(currentPage, loginUser.getId());
        return ResultUtil.success(blogPage);
    }

    /**
     * 点赞博客
     *
     * @param id      id
     * @param request 请求
     * @return {@link BaseResponse}<{@link String}>
     */
    @PutMapping("/like/{id}")
    @ApiOperation(value = "点赞博文")
    public BaseResponse<String> likeBlog(@PathVariable long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        blogService.likeBlog(id, loginUser.getId());
        return ResultUtil.success("成功");
    }

    /**
     * 通过id获取博客
     *
     * @param id      id
     * @param request 请求
     * @return {@link BaseResponse}<{@link BlogVO}>
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取博文")
    public BaseResponse<BlogVO> getBlogById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(blogService.getBlogById(id, loginUser.getId()));
    }

    /**
     * 删除博客通过id
     *
     * @param id      id
     * @param request 请求
     * @return {@link BaseResponse}<{@link String}>
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除博文")
    public BaseResponse<String> deleteBlogById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean admin = userService.isAdmin(loginUser);
        blogService.deleteBlog(id, loginUser.getId(), admin);
        return ResultUtil.success("删除成功");
    }

    /**
     * 更新博客
     *
     * @param blogUpdateRequest 博客更新请求
     * @param request           请求
     * @return {@link BaseResponse}<{@link String}>
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新博文")
    public BaseResponse<String> updateBlog(BlogUpdateRequest blogUpdateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean admin = userService.isAdmin(loginUser);
        blogService.updateBlog(blogUpdateRequest, loginUser.getId(), admin);
        return ResultUtil.success("更新成功");
    }
}
