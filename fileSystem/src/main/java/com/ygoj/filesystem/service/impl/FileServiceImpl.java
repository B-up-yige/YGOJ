package com.ygoj.filesystem.service.impl;

import com.ygoj.filesystem.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件服务实现类（纯文件系统存储）
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    
    /**
     * 文件存储根目录
     */
    @Value("${filesystem.root.path:./testcases}")
    private String rootPath;
    
    /**
     * 实际的文件存储根路径（绝对路径）
     */
    private String actualRootPath;
    
    @PostConstruct
    public void init() {
        // 将路径设置为 resources 目录下的指定文件夹
        // 格式：{项目目录}/src/main/resources/{配置路径}
        String projectRoot = System.getProperty("user.dir");
        actualRootPath = Paths.get(projectRoot, rootPath).toString();
        log.info("文件存储根目录：{}", actualRootPath);
        
        // 确保根目录存在
        File root = new File(actualRootPath);
        if (!root.exists()) {
            root.mkdirs();
            log.info("创建文件存储根目录：{}", actualRootPath);
        }
    }
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @return 文件 ID
     * @throws IOException IO 异常
     */
    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        try {
            if (file == null) {
                log.warn("上传文件为空对象");
                throw new IllegalArgumentException("文件不能为空");
            }
            
            if (file.isEmpty()) {
                log.warn("上传文件内容为空");
                throw new IllegalArgumentException("文件内容不能为空");
            }
            
            // 生成唯一文件 ID
            String fileId = UUID.randomUUID().toString();
            log.debug("生成文件ID: {}", fileId);
            
            // 以 fileId 作为目录名
            Path fileDir = Paths.get(actualRootPath, fileId);
            
            // 确保目录存在
            if (!Files.exists(fileDir)) {
                Files.createDirectories(fileDir);
                log.debug("创建文件目录: {}", fileDir.toAbsolutePath());
            }
            
            // 保存文件：{fileId}/{原始文件名}
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "unnamed_file";
            }
            Path filePath = fileDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            
            log.info("文件上传成功：fileId={}, fileName={}, filePath={}", fileId, fileName, filePath.toAbsolutePath());
            
            return fileId;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("文件上传IO异常", e);
            throw e;
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new IOException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 下载文件
     * 
     * @param fileId 文件 id
     * @return byte[]
     * @throws IOException IO 异常
     */
    @Override
    public byte[] downloadFile(String fileId) throws IOException {
        try {
            log.debug("下载文件, fileId: {}", fileId);
            
            if (fileId == null || fileId.trim().isEmpty()) {
                throw new IllegalArgumentException("文件ID不能为空");
            }
            
            // 通过 fileId 直接构建文件路径
            String filePath = buildFilePath(fileId);
            
            if (filePath == null) {
                log.warn("文件不存在, fileId: {}", fileId);
                throw new IOException("文件不存在：" + fileId);
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("文件不存在, fileId: {}", fileId);
                throw new IOException("文件不存在：" + fileId);
            }
            
            byte[] content = Files.readAllBytes(path);
            log.debug("文件读取成功, size: {} bytes", content.length);
            return content;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("文件下载IO异常, fileId: {}", fileId, e);
            throw e;
        } catch (Exception e) {
            log.error("文件下载异常, fileId: {}", fileId, e);
            throw new IOException("文件下载失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文件
     * 
     * @param fileId 文件 id
     * @throws IOException IO 异常
     */
    @Override
    public void deleteFile(String fileId) throws IOException {
        try {
            log.debug("删除文件, fileId: {}", fileId);
            
            if (fileId == null || fileId.trim().isEmpty()) {
                throw new IllegalArgumentException("文件ID不能为空");
            }
            
            // 通过 fileId 直接构建文件路径
            String filePath = buildFilePath(fileId);
            
            if (filePath == null) {
                log.warn("文件不存在，无需删除, fileId: {}", fileId);
                return;
            }
            
            try {
                Files.deleteIfExists(Paths.get(filePath));
                
                // 删除空目录
                Path dir = Paths.get(filePath).getParent();
                if (Files.exists(dir) && Files.isDirectory(dir)) {
                    Files.deleteIfExists(dir);
                }
                
                log.info("文件删除成功：fileId={}", fileId);
            } catch (IOException e) {
                log.error("删除物理文件失败, fileId: {}", fileId, e);
                throw e;
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("文件删除IO异常, fileId: {}", fileId, e);
            throw e;
        } catch (Exception e) {
            log.error("文件删除异常, fileId: {}", fileId, e);
            throw new IOException("文件删除失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 通过 fileId 构建文件路径
     * 格式：{rootPath}/{fileId}/{原始文件名}
     */
    private String buildFilePath(String fileId) {
        String fileDir = Paths.get(actualRootPath, fileId).toString();
        
        // 获取目录下的第一个文件（每个目录只有一个文件）
        Path dir = Paths.get(fileDir);
        if (!Files.exists(dir)) {
            return null;
        }
        
        try {
            return Files.list(dir)
                .map(Path::toString)
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 获取原始文件名
     */
    @Override
    public String getOriginalFileName(String fileId) throws IOException {
        try {
            log.debug("获取原始文件名, fileId: {}", fileId);
            
            if (fileId == null || fileId.trim().isEmpty()) {
                throw new IllegalArgumentException("文件ID不能为空");
            }
            
            String filePath = buildFilePath(fileId);
            if (filePath == null) {
                log.warn("文件不存在, fileId: {}", fileId);
                throw new IOException("文件不存在：" + fileId);
            }
            
            String fileName = Paths.get(filePath).getFileName().toString();
            log.debug("获取文件名成功: {}", fileName);
            return fileName;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("获取文件名IO异常, fileId: {}", fileId, e);
            throw e;
        } catch (Exception e) {
            log.error("获取文件名异常, fileId: {}", fileId, e);
            throw new IOException("获取文件名失败: " + e.getMessage(), e);
        }
    }
}
