package com.ygoj.record.service;

import com.ygoj.common.Result;
import com.ygoj.record.Record;
import com.ygoj.record.RecordDetail;

import java.util.List;
import java.util.Map;

public interface RecordService {
//    public Record getRecordById(Long id);
//
//    void submit(String status, Long userId);

    Record getRecordinfoById(Long id);

    Result addRecord(Record record);

    List<Record> list(Long page, Long pageSize);
    
    List<RecordDetail> getRecordDetails(Long recordId);
    
    /**
     * 获取用户统计数据
     */
    Map<String, Object> getUserStatistics(Long userId);
    
    /**
     * 获取用户学习曲线数据
     */
    List<Map<String, Object>> getUserLearningCurve(Long userId, Integer days);
    
    /**
     * 获取用户按标签统计的数据
     */
    List<Map<String, Object>> getUserStatsByTag(Long userId);
    
    /**
     * 更新用户统计数据（判题完成后调用）
     */
    void updateUserStatistics(Long userId);
}
