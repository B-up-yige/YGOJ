package com.ygoj.record.service;

import com.ygoj.record.Record;

import java.util.List;

public interface RecordService {
//    public Record getRecordById(Long id);
//
//    void submit(String status, Long userId);

    Record getRecordinfoById(Long id);

    void addRecord(Record record);

    void editRecordStatus(Long id, String status);

    List<Record> list(Long page, Long pageSize);
}
