package com.ygoj.record.feign;

import com.ygoj.common.Result;
import com.ygoj.problem.Probleminfo;
import com.ygoj.user.Userinfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user")
public interface UserFeignClient {

    @GetMapping("/userinfo/{id}")
    public Result userinfo(@PathVariable("id") Long id);
}
