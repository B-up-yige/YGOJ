package com.ygoj.filesystem.controller;

import com.ygoj.common.Result;
import com.ygoj.filesystem.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件控制器
 */
@RestController
@Slf4j
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传文件(内部服务调用 - 需要登录)
     *
     * @param file 文件
     * @return {@link Result}
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/file/upload")
    public Result uploadFile(@RequestParam MultipartFile file) {
        log.info("文件上传请求, fileName: {}, size: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        try {
            String fileId = fileService.uploadFile(file);
            log.info("文件上传成功, fileId: {}", fileId);
            return Result.success(fileId);
        } catch (IOException e) {
            log.error("文件上传失败, fileName: {}", file.getOriginalFilename(), e);
            return Result.error(400, "上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载文件(公开接口 - 通过 fileId 的随机性保证安全)
     *
     * @param fileId 文件 id
     * @return {@link ResponseEntity}<{byte[]}>}
     */
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        log.info("文件下载请求, fileId: {}", fileId);
        try {
            byte[] content = fileService.downloadFile(fileId);
            
            // 获取原始文件名
            String originalFileName = fileService.getOriginalFileName(fileId);
            log.info("文件下载成功, fileId: {}, fileName: {}", fileId, originalFileName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", originalFileName);
            
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.warn("文件下载失败, fileId: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * 删除文件(内部服务调用 - 需要登录)
     *
     * @param fileId 文件 id
     * @return {@link Result}
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/file/delete/{fileId}")
    public Result deleteFile(@PathVariable String fileId) {
        log.info("文件删除请求, fileId: {}", fileId);
        try {
            fileService.deleteFile(fileId);
            log.info("文件删除成功, fileId: {}", fileId);
            return Result.success();
        } catch (IOException e) {
            log.error("文件删除失败, fileId: {}", fileId, e);
            return Result.error(400, "删除失败：" + e.getMessage());
        }
    }
}
