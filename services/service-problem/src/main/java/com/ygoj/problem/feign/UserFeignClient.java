package com.ygoj.problem.feign;

import com.ygoj.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user", path = "/user")
public interface UserFeignClient {

    @GetMapping("/userinfo/{id}")
    public Result userinfo(@PathVariable("id") Long id) ;
}
