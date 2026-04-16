package com.ygoj.record.service;

import com.ygoj.common.Result;
import com.ygoj.record.Record;
import com.ygoj.record.RecordDetail;
import com.ygoj.record.RecordWithInfo;

import java.util.List;
import java.util.Map;

public interface RecordService {
//    public Record getRecordById(Long id);
//
//    void submit(String status, Long userId);

    Record getRecordinfoById(Long id);

    Result addRecord(Record record);

    List<Record> list(Long page, Long pageSize, Long contestId);
    
    /**
     * 获取带题目名称和用户昵称的记录列表
     */
    List<RecordWithInfo> listWithInfo(Long page, Long pageSize, Long contestId);
    
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
    void updateUserStatistics(Long recordId, String status);
    
    /**
     * 获取用户在比赛中的过题情况
     * @param userId 用户ID
     * @param contestId 比赛ID
     * @return Map<problemId, status>
     */
    Map<Long, String> getUserContestProgress(Long userId, Long contestId);
    
    /**
     * 获取用户在题集中的过题情况
     * @param userId 用户ID
     * @param problemsetId 题集ID
     * @return Map<problemId, status>
     */
    Map<Long, String> getUserProblemsetProgress(Long userId, Long problemsetId);
    
    /**
     * 重新判题
     *
     * @param recordId 提交记录ID
     * @return {@link Result}
     */
    Result rejudge(Long recordId);
}
