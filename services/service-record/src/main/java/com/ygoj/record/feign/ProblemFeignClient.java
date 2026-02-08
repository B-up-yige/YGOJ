package com.ygoj.record.feign;

import com.ygoj.problem.pojo.Problem;
import com.ygoj.record.feign.fallback.ProblemFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-problem", fallback = ProblemFeignClientFallback.class)
public interface ProblemFeignClient {

    @GetMapping("/problem/{id}")
    public Problem getProblemById(@PathVariable("id") Long id);
}
