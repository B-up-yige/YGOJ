package com.ygoj.record.feign;

import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-judger")
public interface JudgerFeignClient {

    @PostMapping("/judge")
    public Result judge(@RequestBody SandboxExecuteRequest sandboxExecuteRequest);
}
