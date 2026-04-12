package com.ygoj.record.feign;

import com.ygoj.common.Result;
import com.ygoj.problem.Probleminfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-problem")
public interface ProblemFeignClient {

    @GetMapping("/probleminfo/{id}")
    public Result getProblemInfo(@PathVariable("id") Long id);

    @GetMapping("/getTestCase")
    public Result getTestCase(@RequestParam Long problemId);
    
    @GetMapping("/probleminfo/{id}/tag")
    public Result getProblemTags(@PathVariable("id") Long id);
}
