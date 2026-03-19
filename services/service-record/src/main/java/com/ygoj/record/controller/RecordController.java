package com.ygoj.record.controller;

import com.ygoj.common.Result;
import com.ygoj.record.Record;
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
    @PostMapping("/add")
    public Result addRecord(Record record) {
        recordService.addRecord(record);
        return Result.success();
    }

    /**
     * 编辑提交记录状态
     *
     * @param id     提交记录id
     * @param status 状态
     * @return {@link Result}
     */
    @PutMapping("/editStatus")
    public Result editRecordStatus(Long id, String status) {
        recordService.editRecordStatus(id, status);
        return Result.success();
    }
}
