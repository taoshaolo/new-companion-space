package com.taoshao.companionspace.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taoshao.companionspace.model.entity.Blog;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.BlogAddRequest;
import com.taoshao.companionspace.model.request.BlogUpdateRequest;
import com.taoshao.companionspace.model.vo.BlogVO;

/**
* @author taoshao
* @description 针对表【blog】的数据库操作Service
* @createDate 2023-06-03 15:54:34
*/
public interface BlogService extends IService<Blog> {

    /**
     * 添加博客
     *
     * @param blogAddRequest 博客添加请求
     * @param loginUser      登录用户
     * @return {@link Long}
     */
    Long addBlog(BlogAddRequest blogAddRequest, User loginUser);

    /**
     * 列出我博客
     *
     * @param currentPage 当前页码
     * @param id          id
     * @return {@link Page}<{@link BlogVO}>
     */
    Page<BlogVO> listMyBlogs(long currentPage, Long id);

    /**
     * 点赞博客
     *
     * @param blogId 博客id
     * @param userId 用户id
     */
    void likeBlog(long blogId, Long userId);

    /**
     * 分页博客
     *
     * @param currentPage 当前页码
     * @param title       标题
     * @param id          id
     * @return {@link Page}<{@link BlogVO}>
     */
    Page<BlogVO> pageBlog(long currentPage, String title, Long id);

    /**
     * 收到博客通过id
     *
     * @param blogId 博客id
     * @param userId 用户id
     * @return {@link BlogVO}
     */
    BlogVO getBlogById(long blogId, Long userId);

    /**
     * 删除博客
     *
     * @param blogId  博客id
     * @param userId  用户id
     * @param isAdmin 是否为管理员
     */
    void deleteBlog(Long blogId, Long userId, boolean isAdmin);

    /**
     * 更新博客
     *
     * @param blogUpdateRequest 博客更新请求
     * @param userId            用户id
     * @param isAdmin           是否为管理员
     */
    void updateBlog(BlogUpdateRequest blogUpdateRequest, Long userId, boolean isAdmin);
}
