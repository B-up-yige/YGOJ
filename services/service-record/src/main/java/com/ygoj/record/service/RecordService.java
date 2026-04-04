package com.ygoj.record.service;

import com.ygoj.common.Result;
import com.ygoj.record.Record;

import java.util.List;

public interface RecordService {
//    public Record getRecordById(Long id);
//
//    void submit(String status, Long userId);

    Record getRecordinfoById(Long id);

    Result addRecord(Record record);

    List<Record> list(Long page, Long pageSize);
}
