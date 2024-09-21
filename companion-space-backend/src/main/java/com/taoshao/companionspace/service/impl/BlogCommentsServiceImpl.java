package com.taoshao.companionspace.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.mapper.BlogCommentsMapper;
import com.taoshao.companionspace.model.entity.*;
import com.taoshao.companionspace.model.enums.MessageTypeEnum;
import com.taoshao.companionspace.model.request.AddCommentRequest;
import com.taoshao.companionspace.model.vo.BlogCommentsVO;
import com.taoshao.companionspace.model.vo.BlogVO;
import com.taoshao.companionspace.model.vo.UserVO;
import com.taoshao.companionspace.service.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.taoshao.companionspace.constant.RedisConstant.MESSAGE_LIKE_NUM_KEY;
import static com.taoshao.companionspace.constant.RedissonConstant.*;
import static com.taoshao.companionspace.constant.SystemConstant.PAGE_SIZE;

/**
 * @author taoshao
 * @description 针对表【blog_comments】的数据库操作Service实现
 * @createDate 2023-06-08 12:44:45
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
        implements BlogCommentsService {

    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;

    @Resource
    private CommentLikeService commentLikeService;

    @Resource
    private MessageService messageService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;
    
    

    @Value("${server.servlet.session.cookie.domain}")
    private String host;

    @Value("${server.port}")
    private String port;

    @Override
    @Transactional
    public void addComment(AddCommentRequest addCommentRequest, Long userId) {
        BlogComments blogComments = new BlogComments();
        blogComments.setUserId(userId);
        blogComments.setBlogId(addCommentRequest.getBlogId());
        blogComments.setContent(addCommentRequest.getContent());
        blogComments.setLikedNum(0);
        blogComments.setStatus(0);
        this.save(blogComments);
        Blog blog = blogService.getById(addCommentRequest.getBlogId());
        blogService.update().eq("id", addCommentRequest.getBlogId())
                .set("commentsNum", blog.getCommentsNum() + 1).update();
    }

    @Override
    public List<BlogCommentsVO> listComments(long blogId, long userId) {
        LambdaQueryWrapper<BlogComments> blogCommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogCommentsLambdaQueryWrapper.eq(BlogComments::getBlogId, blogId);
        List<BlogComments> blogCommentsList = this.list(blogCommentsLambdaQueryWrapper);
        return blogCommentsList.stream().map((comment) -> {
            BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
            BeanUtils.copyProperties(comment, blogCommentsVO);
            User user = userService.getById(comment.getUserId());
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            blogCommentsVO.setCommentUser(userVO);
            LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, comment.getId())
                    .eq(CommentLike::getUserId, userId);
            long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
            blogCommentsVO.setIsLiked(count > 0);
            return blogCommentsVO;
        }).collect(Collectors.toList());
    }

    @Override
    public BlogCommentsVO getComment(long commentId, Long userId) {
        BlogComments comments = this.getById(commentId);
        if (comments == null) {
            return null;
        }
        BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
        BeanUtils.copyProperties(comments, blogCommentsVO);

        LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, userId).eq(CommentLike::getCommentId, commentId);
        long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
        blogCommentsVO.setIsLiked(count > 0);
        return blogCommentsVO;
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        RLock lock = redissonClient.getLock(COMMENTS_LIKE_LOCK + commentId + ":" + userId);
        try {
            if (lock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.MILLISECONDS)) {
                BlogComments comments = this.getById(commentId);
                if (comments == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论不存在");
                }
                LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId);
                long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
                if (count == 0) {
                    doLikeComment(commentId, userId);
                } else {
                    commentLikeService.remove(commentLikeLambdaQueryWrapper);
                    doUnLikeComment(commentId, userId);
                }
            }
        } catch (Exception e) {
            log.error("LikeBlog error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 点赞评论
     *
     * @param commentId 评论id
     * @param userId    用户id
     */
    public void doLikeComment(Long commentId, Long userId) {
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        commentLikeService.save(commentLike);
        BlogComments blogComments = this.getById(commentId);
        this.update().eq("id", commentId)
                .set("likedNum", blogComments.getLikedNum() + 1)
                .update();
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + blogComments.getUserId();
        Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasKey)) {
            stringRedisTemplate.opsForValue().increment(likeNumKey);
        } else {
            stringRedisTemplate.opsForValue().set(likeNumKey, "1");
        }
        Message message = new Message();
        message.setType(MessageTypeEnum.BLOG_COMMENT_LIKE.getValue());
        message.setFromId(userId);
        message.setToId(blogComments.getUserId());
        message.setData(String.valueOf(blogComments.getId()));
        messageService.save(message);
    }

    /**
     * 取消喜欢评论
     *
     * @param commentId 评论id
     * @param userId    用户id
     */
    public void doUnLikeComment(Long commentId, Long userId) {
        BlogComments blogComments = this.getById(commentId);
        this.update().eq("id", commentId)
                .set("likedNum", blogComments.getLikedNum() - 1)
                .update();
        LambdaQueryWrapper<Message> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper
                .eq(Message::getType, MessageTypeEnum.BLOG_COMMENT_LIKE.getValue())
                .eq(Message::getFromId, userId)
                .eq(Message::getToId, blogComments.getUserId())
                .eq(Message::getData, String.valueOf(blogComments.getId()));
        messageService.remove(messageQueryWrapper);
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + blogComments.getUserId();
        String upNumStr = stringRedisTemplate.opsForValue().get(likeNumKey);
        if (!StrUtil.isNullOrUndefined(upNumStr) && Long.parseLong(upNumStr) != 0) {
            stringRedisTemplate.opsForValue().decrement(likeNumKey);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId, boolean isAdmin) {
        BlogComments blogComments = this.getById(id);
        if (isAdmin) {
            this.removeById(id);
            Integer commentsNum = blogService.getById(blogComments.getBlogId()).getCommentsNum();
            blogService.update().eq("id", blogComments.getBlogId()).set("commentsNum", commentsNum - 1).update();
            return;
        }
        if (blogComments == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!blogComments.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        this.removeById(id);
        Integer commentsNum = blogService.getById(blogComments.getBlogId()).getCommentsNum();
        blogService.update().eq("id", blogComments.getBlogId()).set("commentsNum", commentsNum - 1).update();
    }

    @Override
    public List<BlogCommentsVO> listMyComments(Long id) {
        LambdaQueryWrapper<BlogComments> blogCommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogCommentsLambdaQueryWrapper.eq(BlogComments::getUserId, id);
        List<BlogComments> blogCommentsList = this.list(blogCommentsLambdaQueryWrapper);
        return blogCommentsList.stream().map((item) -> {
            BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
            BeanUtils.copyProperties(item, blogCommentsVO);
            User user = userService.getById(item.getUserId());
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            blogCommentsVO.setCommentUser(userVO);

            Long blogId = blogCommentsVO.getBlogId();
            Blog blog = blogService.getById(blogId);
            return getBlogCommentsVO(id, item, blogCommentsVO, blog);
        }).collect(Collectors.toList());
    }

    private BlogCommentsVO getBlogCommentsVO(Long id, BlogComments item, BlogCommentsVO blogCommentsVO, Blog blog) {
        BlogVO blogVO = getBlogImgs(blog);
        Long authorId = blogVO.getUserId();
        User author = userService.getById(authorId);
        UserVO authorVO = new UserVO();
        BeanUtils.copyProperties(author, authorVO);
        blogVO.setAuthor(authorVO);
        blogCommentsVO.setBlog(blogVO);
        LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, id).eq(CommentLike::getCommentId, item.getId());
        long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
        blogCommentsVO.setIsLiked(count > 0);
        return blogCommentsVO;
    }

    @Override
    public Page<BlogCommentsVO> pageMyComments(Long id, Long currentPage) {
        LambdaQueryWrapper<BlogComments> blogCommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogCommentsLambdaQueryWrapper.eq(BlogComments::getUserId, id);
        Page<BlogComments> blogCommentsPage = this.page(new Page<>(currentPage, PAGE_SIZE),
                blogCommentsLambdaQueryWrapper);
        if (blogCommentsPage == null || blogCommentsPage.getSize() == 0) {
            return new Page<>();
        }
        Page<BlogCommentsVO> blogCommentsVoPage = new Page<>();
        BeanUtils.copyProperties(blogCommentsPage, blogCommentsVoPage);
        List<BlogCommentsVO> blogCommentsVOList = blogCommentsPage.getRecords().stream().map((item) -> {
            BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
            BeanUtils.copyProperties(item, blogCommentsVO);
            User user = userService.getById(item.getUserId());
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            blogCommentsVO.setCommentUser(userVO);
            Long blogId = blogCommentsVO.getBlogId();
            Blog blog = blogService.getById(blogId);
            if (blog == null) {
                return null;
            }
            return getBlogCommentsVO(id, item, blogCommentsVO, blog);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        blogCommentsVoPage.setRecords(blogCommentsVOList);
        Collections.sort(blogCommentsVOList);
        return blogCommentsVoPage;
    }

    @Override
    public List<BlogCommentsVO> pageMyCommented(Long id, Long currentPage) {
        LambdaQueryWrapper<Blog> blogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        blogLambdaQueryWrapper.eq(Blog::getUserId, id);
        List<Blog> blogList = blogService.list(blogLambdaQueryWrapper);
        if (blogList.isEmpty()) {
            return new ArrayList<>();
        }
        List<BlogCommentsVO> blogCommentsVOS = new ArrayList<>();
        blogList.forEach((blog) -> {
            Long blogId = blog.getId();
            LambdaQueryWrapper<BlogComments> blogCommentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
            blogCommentsLambdaQueryWrapper.eq(BlogComments::getBlogId, blogId);
            List<BlogComments> blogCommentsList = this.list(blogCommentsLambdaQueryWrapper);
            List<BlogCommentsVO> blogCommentsVOList = blogCommentsList.stream().map((item) -> {
                BlogCommentsVO blogCommentsVO = new BlogCommentsVO();
                BeanUtils.copyProperties(item, blogCommentsVO);
                User user = userService.getById(item.getUserId());
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                blogCommentsVO.setCommentUser(userVO);
                Blog myBlog = blogService.getById(item.getBlogId());
                if (myBlog == null) {
                    return null;
                }
                BlogVO blogVO = getBlogImgs(myBlog);
                User author = userService.getById(id);
                UserVO authorVO = new UserVO();
                BeanUtils.copyProperties(author, authorVO);
                blogVO.setAuthor(authorVO);
                blogCommentsVO.setBlog(blogVO);
                LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                commentLikeLambdaQueryWrapper.eq(CommentLike::getUserId, id)
                        .eq(CommentLike::getCommentId, item.getId());
                long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
                blogCommentsVO.setIsLiked(count > 0);
                return blogCommentsVO;
            }).collect(Collectors.toList());
            blogCommentsVOS.addAll(blogCommentsVOList);
        });
        Collections.sort(blogCommentsVOS);
        return blogCommentsVOS;
    }

    private BlogVO getBlogImgs(Blog myBlog) {
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(myBlog, blogVO);
        String images = blogVO.getImages();
        if (images == null) {
            blogVO.setCoverImage(null);
        } else {
            String[] imgStr = images.split(",");
            String imgUrlTmp = imgStr[0];
            if (true) {
                String fileUrl = "http://" + host + ":" + port + "/api/common/image/" + imgUrlTmp;
                blogVO.setCoverImage(fileUrl);
            } else {
//                blogVO.setCoverImage(qiniuUrl + imgStr[0]);
            }
        }
        return blogVO;
    }
}




