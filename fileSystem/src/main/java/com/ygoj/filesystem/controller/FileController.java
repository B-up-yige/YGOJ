package com.ygoj.filesystem.controller;

import com.ygoj.common.Result;
import com.ygoj.filesystem.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件控制器
 */
@RestController
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @return {@link Result}
     */
    @PostMapping("/file/upload")
    public Result uploadFile(@RequestParam MultipartFile file) {
        try {
            String fileId = fileService.uploadFile(file);
            return Result.success(fileId);
        } catch (IOException e) {
            return Result.error(500, "上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     *
     * @param fileId 文件 id
     * @return {@link ResponseEntity}<{byte[]}>
     */
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        try {
            byte[] content = fileService.downloadFile(fileId);
            
            // 获取原始文件名
            String originalFileName = fileService.getOriginalFileName(fileId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", originalFileName);
            
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * 删除文件
     *
     * @param fileId 文件 id
     * @return {@link Result}
     */
    @DeleteMapping("/file/delete/{fileId}")
    public Result deleteFile(@PathVariable String fileId) {
        try {
            fileService.deleteFile(fileId);
            return Result.success();
        } catch (IOException e) {
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }
}
