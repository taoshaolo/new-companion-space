package com.taoshao.companionspace.controller;

import cn.hutool.core.io.FileUtil;
import com.taoshao.companionspace.common.BaseResponse;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.common.ResultUtil;
import com.taoshao.companionspace.config.CosClientConfig;
import com.taoshao.companionspace.constant.FileConstant;
import com.taoshao.companionspace.exception.BusinessException;
import com.taoshao.companionspace.manager.CosManager;
import com.taoshao.companionspace.model.entity.User;
import com.taoshao.companionspace.model.enums.FileUploadBizEnum;
import com.taoshao.companionspace.model.file.UploadFileRequest;
import com.taoshao.companionspace.service.UserService;
import com.taoshao.companionspace.utils.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import static com.taoshao.companionspace.constant.SystemConstant.DEFAULT_BUFFER_SIZE;
import static com.taoshao.companionspace.constant.SystemConstant.FILE_END;


/**
 * 文件控制器
 *
 * @author taoshao
 * @date 2023/06/11
 */
@Slf4j
@RestController
@RequestMapping("/common")
@CrossOrigin({"http://localhost:3000", "http://taoshao.icu"})
@Api(tags = "文件管理模块")
public class FileController {

    /**
     * 基本路径
     */
    @Value("${taoshao.img}")
    private String basePath;

    /**
     * 用户服务
     */
    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;


    @Value("${server.servlet.session.cookie.domain}")
    private String host;

    @Value("${server.port}")
    private String port;

    /**
     * 上传
     *
     * @param file    文件
     * @param request 请求
     * @return {@link BaseResponse}<{@link String}>
     */
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public BaseResponse<String> upload(MultipartFile file, HttpServletRequest request) {
        if (file == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "请登录");
        }
        if (true) {
            String fileName = FileUtils.uploadFile2Local(file);
            String fileUrl = "http://" + host + ":" + port + "/api/common/image/" + fileName;
            User user = new User();
            user.setId(loginUser.getId());
            user.setUserAvatarUrl(fileUrl);
            boolean success = userService.updateById(user);
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
            }
            return ResultUtil.success(fileUrl);
        } else {
            String filename = FileUtils.uploadFile2Cloud(file);
//            String fileUrl = qiniuUrl + filename;
            User user = new User();
            user.setId(loginUser.getId());
//            user.setUserAvatarUrl(fileUrl);
            boolean success = userService.updateById(user);
            if (!success) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
            }
            return ResultUtil.success("");
//            return ResultUtil.success(fileUrl);
        }
    }

    /**
     * 下载
     *
     * @param name     名字
     * @param response 响应
     */
    @GetMapping("/image/{name}")
    @ApiOperation(value = "文件下载")
    public void download(@PathVariable String name, HttpServletResponse response) {
        try {
            //获取指定文件
            File img = new File(System.getProperty("user.dir") + basePath + name);
            //获取输入流
            FileInputStream inputStream = new FileInputStream(img);
            //获取输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //指定response类型
            response.setContentType("image/jpeg");
            int len = 0;
            //设置缓冲区大小
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            //将文件从输入流读到缓冲区，输出流读取缓冲区内容
            while ((len = inputStream.read(bytes)) != FILE_END) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/uploadToCOS")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtil.success(cosClientConfig.getHost() + "/" + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
