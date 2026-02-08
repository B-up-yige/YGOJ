package com.ygoj.record.feign.fallback;

import com.ygoj.problem.pojo.Problem;
import com.ygoj.record.feign.ProblemFeignClient;
import org.springframework.stereotype.Component;

@Component
public class ProblemFeignClientFallback implements ProblemFeignClient {
    @Override
    public Problem getProblemById(Long id) {
        return null;
    }
}
