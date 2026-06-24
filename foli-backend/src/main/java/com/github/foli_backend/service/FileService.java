package com.github.foli_backend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口 File service interface
 * 提供文件上传功能 Provides file upload functionality
 */
public interface FileService {

    /**
     * 上传文件 Upload file
     * 将文件保存到服务器本地并返回访问URL
     * Saves file to local server and returns access URL
     * @param file 上传的文件 Uploaded file
     * @return 文件访问URL路径 File access URL path, e.g. "/uploads/images/uuid.png"
     */
    String upload(MultipartFile file);
}
