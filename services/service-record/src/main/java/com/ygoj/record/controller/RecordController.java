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

//    @Autowired
//    private RecordProperties recordProperties;
//
//    @GetMapping("/config")
//    public String config(){
//        return "test=" + recordProperties.getTest() + "; a=" + recordProperties.getA();
//    }
//
//    @GetMapping("/record/{id}")
////    @SentinelResource(value = "///record/{id}", fallback = "getRecordByIdFallback")
//    public Record getRecordById(@PathVariable("id") Long id){
//        Record record = recordService.getRecordById(id);
//        return record;
//    }
//
//    public Record getRecordByIdFallback(@PathVariable("id") Long id,
//                                        Throwable throwable){
//        return new Record();
//    }
//
//    @GetMapping("/submit")
//    public Result submit(@RequestParam(name = "status") String status, @RequestParam(name = "userId") Long userId){
//        recordService.submit(status, userId);
//        return Result.success();
//    }

    @GetMapping("/recordinfo/{id}")
    public Result getRecordInfo(@PathVariable Long id) {
        Record record = recordService.getRecordinfoById(id);
        return Result.success(record);
    }

    @PostMapping("/add")
    public Result addRecord(Record record) {
        recordService.addRecord(record);
        return Result.success();
    }

    @PutMapping("/editStatus")
    public Result editRecordStatus(Long id, String status) {
        recordService.editRecordStatus(id, status);
        return Result.success();
    }
}
