package com.ygoj.problem.feign;

import com.ygoj.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import com.ygoj.problem.config.FeignMultipartConfig;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "fileSystem", configuration = FeignMultipartConfig.class)
public interface FileSystemFeignClient {

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result uploadFile(@RequestPart MultipartFile file);

    @DeleteMapping("/file/delete/{fileId}")
    public Result deleteFile(@PathVariable String fileId);
}
