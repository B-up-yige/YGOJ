package com.ygoj.judger.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "fileSystem")
public interface FileSystemFeignClient {
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId);
}
