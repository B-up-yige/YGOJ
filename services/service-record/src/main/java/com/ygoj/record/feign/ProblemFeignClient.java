package com.ygoj.record.feign;

import com.ygoj.problem.pojo.Probleminfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-problem")
public interface ProblemFeignClient {

    @GetMapping("/problem/{id}")
    public Probleminfo getProblemById(@PathVariable("id") Long id);
}
