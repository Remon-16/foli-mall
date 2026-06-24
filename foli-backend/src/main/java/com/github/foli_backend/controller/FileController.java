package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器 File upload controller
 * 提供文件上传接口 Provides file upload endpoint
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件上传 Files")
public class FileController {

    private final FileService fileService;

    /**
     * 构造器注入 Constructor injection
     */
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 上传文件 Upload file
     * 仅登录用户可上传 支持图片格式 最大10MB
     * Only logged-in users can upload, supports image formats, max 10MB
     * @param file 上传的文件 Uploaded file (multipart/form-data, field name = "file")
     * @return 文件访问URL File access URL
     */
    @PostMapping("/upload")
    @RequireLogin
    @Operation(summary = "上传文件 Upload file", description = "上传图片文件 支持常见图片格式 最大10MB Upload image file, supports common image formats, max 10MB")
    public Result<String> upload(
            @Parameter(description = "上传的图片文件 Image file to upload")
            @RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return Result.success(url);
    }
}
