package com.ygoj.record.controller;

import com.ygoj.common.Result;
import com.ygoj.record.Record;
import com.ygoj.record.feign.JudgerFeignClient;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.feign.UserFeignClient;
import com.ygoj.record.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RecordController {
    @Autowired
    private RecordService recordService;

    /**
     * 获取提交记录信息
     *
     * @param id 提交记录id
     * @return {@link Result}
     */
    @GetMapping("/recordinfo/{id}")
    public Result getRecordInfo(@PathVariable Long id) {
        Record record = recordService.getRecordinfoById(id);
        return Result.success(record);
    }

    /**
     * 添加提交记录
     *
     * @param record 提交记录
     * @return {@link Result}
     */
    @PostMapping("/submit")
    public Result addRecord(@RequestBody Record record) {
        return recordService.addRecord(record);
    }

    /**
     * 分页获取提交列表
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @return {@link Result}
     */
    @GetMapping("/list")
    public Result list(Long page, Long pageSize) {
        return Result.success(recordService.list(page, pageSize));
    }
    
    /**
     * 获取提交记录的测试点详情
     *
     * @param id 提交记录id
     * @return {@link Result}
     */
    @GetMapping("/recordinfo/{id}/details")
    public Result getRecordDetails(@PathVariable Long id) {
        return Result.success(recordService.getRecordDetails(id));
    }
}
