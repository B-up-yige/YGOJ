package com.ygoj.record.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygoj.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse, String s,
                       BlockException e) throws Exception {

        httpServletResponse.setStatus(429);
        httpServletResponse.setContentType("application/json;charset=utf-8");

        PrintWriter writer = httpServletResponse.getWriter();

        Result result = Result.error(500, s+"被sentinel限制了，原因：" + e.getClass());
        String json = objectMapper.writeValueAsString(result);

        writer.write(json);
        writer.flush();
        writer.close();
    }
}
