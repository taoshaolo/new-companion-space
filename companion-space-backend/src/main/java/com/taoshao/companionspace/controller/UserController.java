package com.taoshao.companionspace.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.DeleteRequest;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.constant.UserConstant;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.request.*;
import com.taoshao.companionspace.model.vo.UserVO;
import com.taoshao.companionspace.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.taoshao.companionspace.constant.RedisConstant.MATCH_USER;

/**
 * @Author: taoshao
 * @Date: 2023年03月08日 23:21
 * @Version: 1.0
 * @Description:
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 登录
     *
     * @param loginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtil.success(user, "登录成功");
    }

    /**
     * 管理员登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求
     * @return {@link BaseResponse}<{@link User}>
     */
    @PostMapping("/admin/login")
    public BaseResponse<User> adminLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.adminLogin(userAccount, userPassword, request);
        return ResultUtil.success(user, "登录成功");
    }

    /**
     * 注册
     *
     * @param registerRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = registerRequest.getUsername();
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegistration(username, userAccount, userPassword, checkPassword);
        return ResultUtil.success(result, "注册成功");
    }
    /**
     * 获取当前用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        Long userId = currentUser.getId();
        User user = userService.getById(userId);
        return ResultUtil.success(userService.getSafetyUser(user));
    }

    @GetMapping("/admin/current")
    public BaseResponse<User> getAdminCurrentUser(HttpServletRequest request) {
        User adminUser = (User) request.getSession().getAttribute(UserConstant.ADMIN_LOGIN_USER_STATUS);
        if (adminUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "请先登录");
        }
        return ResultUtil.success(adminUser);
    }

    /**
     * 查询所有用户列表
     *
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchList(HttpServletRequest request) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER_STATUS);
        if (loginUser != null) {
            List<User> userList = (List<User>) valueOperations.get(userService.redisFormat(loginUser.getId()));
            if (userList != null) {
                // 打乱并固定第一个用户
                return ResultUtil.success(fixTheFirstUser(userList));
            }
        } else {
            List<User> userList = (List<User>) valueOperations.get("companionspace:user:notLogin");
            if (userList != null) {
                // 打乱并固定第一个用户
                return ResultUtil.success(fixTheFirstUser(userList));
            }
        }
        List<User> result = null;
        try {
            if (loginUser != null) {
                List<User> userList = userService.list();
                // 打乱并固定第一个用户
                result = fixTheFirstUser(userList).stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                redisTemplate.opsForValue().set(userService.redisFormat(loginUser.getId()), result, 1 + RandomUtil.randomInt(1, 2) / 10, TimeUnit.MINUTES);
            } else {
                // 未登录只能查看20条
                Page<User> userPage = new Page<>(1, 20);
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                Page<User> page = userService.page(userPage, userLambdaQueryWrapper);
                result = fixTheFirstUser(page.getRecords()).stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                redisTemplate.opsForValue().set("companionspace:user:notLogin", result, 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtil.success(result);
    }

    /**
     * 固定第一个用户，打乱其他顺序
     *
     * @param userList
     * @return
     */
    private List<User> fixTheFirstUser(List<User> userList) {
        // 取出第一个元素
        User firstUser = userList.get(0);
        // 将剩下的元素打乱顺序
        userList = userList.subList(1, userList.size());
        Collections.shuffle(userList);
        userList.add(0, firstUser);
        return userList;
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        int size = userQueryRequest.getPageSize();
        int current = userQueryRequest.getPageNum();
        Long id = userQueryRequest.getId();
        String username = userQueryRequest.getUsername();
        String userAccount = userQueryRequest.getUserAccount();
        String searchText = userQueryRequest.getSearchText();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);

        queryWrapper.like(!StrUtil.isBlank(username), "username", username);
        queryWrapper.like(!StrUtil.isBlank(userAccount), "userAccount", userAccount);
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("userDesc", searchText)
                    .or()
                    .like("contactInfo", searchText)
                    .or()
                    .like("email", searchText)
            );
        }
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtil.success(userVOPage);
    }


    /**
     * 搜索用户
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search")
    public BaseResponse<List<User>> userQuery(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        List<User> users = userService.userQuery(userQueryRequest, request);
        return ResultUtil.success(users);
    }

    /**
     * 删除用户（管理员）
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        Long id = deleteRequest.getId();
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = userService.removeById(id);
        if (remove) {
            User loginUser = (User) request.getSession().getAttribute(UserConstant.LOGIN_USER_STATUS);
            redisTemplate.delete(userService.redisFormat(loginUser.getId()));
        }
        return ResultUtil.success(remove);
    }

    /**
     * 退出
     *
     * @param request
     * @return
     */
    @PostMapping("/loginOut")
    public BaseResponse<Integer> loginOut(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(userService.loginOut(request));
    }

    @PostMapping("/admin/loginOut")
    public BaseResponse<Integer> adminLoginOut(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtil.success(userService.adminLoginOut(request));
    }

    /**
     * 根据 id 获取用户信息
     *
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public BaseResponse<User> getUserById(@PathVariable("id") Integer id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = this.userService.getById(id);
        return ResultUtil.success(user);
    }

    /**
     * 根据标签查询用户
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) Set<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签参数错误");
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtil.success(userList);
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> getUpdateUserById(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateId = userService.updateUser(user, currentUser);
        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
        return ResultUtil.success(updateId);
    }

    /**
     * 更新标签
     *
     * @param tagRequest
     * @param request
     * @return
     */
    @PostMapping("/update/tags")
    public BaseResponse<Integer> updateTagById(@RequestBody UpdateTagRequest tagRequest, HttpServletRequest request) {
        if (tagRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateTag = userService.updateTagById(tagRequest, currentUser);
        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
        return ResultUtil.success(updateTag);
    }

    /**
     * 修改密码
     *
     * @param updatePassword
     * @param request
     * @return
     */
    @PostMapping("/update/password")
    public BaseResponse<Integer> updatePassword(@RequestBody UserUpdatePassword updatePassword, HttpServletRequest request) {
        if (updatePassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateTag = userService.updatePasswordById(updatePassword, currentUser);
        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
        return ResultUtil.success(updateTag);
    }

    /**
     * 获取好友信息
     *
     * @param request
     * @return
     */
    @GetMapping("/friends")
    public BaseResponse<List<User>> getFriends(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        List<User> getUser = userService.getFriendsById(currentUser);
        return ResultUtil.success(getUser);
    }

    /**
     * 删除好友信息
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/deleteFriend/{id}")
    public BaseResponse<Boolean> deleteFriend(@PathVariable("id") Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友不存在");
        }
        User currentUser = userService.getLoginUser(request);
        boolean deleteFriend = userService.deleteFriend(currentUser, id);
        return ResultUtil.success(deleteFriend);
    }

    /**
     * 搜索好友
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/searchFriend")
    public BaseResponse<List<User>> searchFriend(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        User currentUser = userService.getLoginUser(request);
        List<User> searchFriend = userService.searchFriend(userQueryRequest, currentUser);
        return ResultUtil.success(searchFriend);
    }

    /**
     * 获取最匹配的用户
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超出范围");
        }
        User loginUser = userService.getLoginUser(request);
        String key = MATCH_USER + loginUser.getId();
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //1.有缓存，查缓存
        List<User> matchUsers = (List<User>) valueOperations.get(key);
        if (!CollectionUtils.isEmpty(matchUsers)) {
            return ResultUtil.success(matchUsers);
        }
        //2.没缓存，查数据库
        List<User> userList = userService.matchUsers(num, loginUser);
        //3.最后存 redis
        try {
            valueOperations.set(key, userList, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtil.success(userList);
    }

    /**
     * 根据地理位置获取最匹配的用户
     * @param userMatchByGeoRequest
     * @param request
     * @return
     */
    @GetMapping("/matchByGeo")
    public BaseResponse<List<User>> matchUsersByGeo(UserMatchByGeoRequest userMatchByGeoRequest, HttpServletRequest request) {
        if (userMatchByGeoRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> userList = userService.matchUsersByGeo(userMatchByGeoRequest, loginUser);
        return ResultUtil.success(userList);
    }

//    saveGeo
    @PostMapping("/saveGeo")
    public BaseResponse<Boolean> saveGeo(@RequestBody UserGeoRequest userGeoRequest, HttpServletRequest request) {
        if (userGeoRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean saveGeo = userService.saveGeo(userGeoRequest, loginUser);
        return ResultUtil.success(saveGeo);
    }
}
