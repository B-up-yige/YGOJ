package com.ygoj.record.controller;

import com.ygoj.common.Result;
import com.ygoj.record.pojo.Record;
import com.ygoj.record.properties.RecordProperties;
import com.ygoj.record.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecordController {
    @Autowired
    private RecordService recordService;

    @Autowired
    private RecordProperties recordProperties;

    @GetMapping("/config")
    public String config(){
        return "test=" + recordProperties.getTest() + "; a=" + recordProperties.getA();
    }

    @GetMapping("/record/{id}")
//    @SentinelResource(value = "///record/{id}", fallback = "getRecordByIdFallback")
    public Record getRecordById(@PathVariable("id") Long id){
        Record record = recordService.getRecordById(id);
        return record;
    }

    public Record getRecordByIdFallback(@PathVariable("id") Long id,
                                        Throwable throwable){
        return new Record();
    }

    @GetMapping("/submit")
    public Result submit(@RequestParam(name = "status") String status, @RequestParam(name = "userId") Long userId){
        recordService.submit(status, userId);
        return Result.success();
    }
}
