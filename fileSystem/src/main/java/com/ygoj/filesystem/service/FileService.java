package com.ygoj.filesystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @return 文件 ID
     * @throws IOException IO 异常
     */
    String uploadFile(MultipartFile file) throws IOException;
    
    /**
     * 下载文件
     * 
     * @param fileId 文件 id
     * @return byte[]
     * @throws IOException IO 异常
     */
    byte[] downloadFile(String fileId) throws IOException;
    
    /**
     * 删除文件
     * 
     * @param fileId 文件 id
     * @throws IOException IO 异常
     */
    void deleteFile(String fileId) throws IOException;
    
    /**
     * 获取原始文件名
     * 
     * @param fileId 文件 id
     * @return 原始文件名
     * @throws IOException IO 异常
     */
    String getOriginalFileName(String fileId) throws IOException;
}
