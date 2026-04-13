package com.ygoj.record.feign;

import com.ygoj.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-problem", path = "/contest")
public interface ContestFeignClient {

    @GetMapping("/{id}")
    Result getContestById(@PathVariable("id") Long id);
}
