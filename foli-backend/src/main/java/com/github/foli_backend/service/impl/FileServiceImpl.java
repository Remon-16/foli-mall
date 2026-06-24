package com.github.foli_backend.service.impl;

import cn.hutool.core.util.IdUtil;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件服务实现 File service implementation
 * 处理文件上传、校验、存储
 * Handles file upload, validation, and storage
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    /** 最大文件大小 10MB Max file size */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** 图片存储子目录 Images storage subdirectory */
    private static final String IMAGES_DIR = "images";

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 构造器注入 Constructor injection (default no-arg)
     */
    public FileServiceImpl() {
    }

    @Override
    public String upload(MultipartFile file) {
        // 检查文件是否为空 Check if file is empty
        if (file == null || file.isEmpty()) {
            BizCodeEnum.FILE_EMPTY.throwEx();
        }

        // 检查文件大小限制 Check file size limit
        if (file.getSize() > MAX_FILE_SIZE) {
            BizCodeEnum.FILE_TOO_LARGE.throwEx();
        }

        // 检查文件类型 仅允许图片 Check file type, only images allowed
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            BizCodeEnum.FILE_NOT_IMAGE.throwEx();
        }

        // 获取原始文件扩展名 Get original file extension
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成UUID文件名 Generate UUID filename
        String uuidFilename = IdUtil.fastSimpleUUID() + ext;

        // 构建目标存储路径 Build target storage path: uploadPath/images/
        String imagesDirPath = uploadPath + File.separator + IMAGES_DIR;
        File imagesDir = new File(imagesDirPath);
        if (!imagesDir.exists()) {
            boolean created = imagesDir.mkdirs();
            if (!created) {
                BizCodeEnum.FILE_UPLOAD_FAILED.throwEx();
            }
            log.info("Created upload directory: {}", imagesDir.getAbsolutePath());
        }

        // 保存文件 Save file
        File destFile = new File(imagesDir, uuidFilename);
        try {
            file.transferTo(destFile);
            log.info("File uploaded successfully: {} ({} bytes)", destFile.getAbsolutePath(), file.getSize());
        } catch (IOException e) {
            log.error("File upload failed", e);
            BizCodeEnum.FILE_UPLOAD_FAILED.throwEx();
        }

        // 返回文件访问URL Return file access URL
        return "/uploads/" + IMAGES_DIR + "/" + uuidFilename;
    }
}
