package com.ygoj.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ygoj.record.UserStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatistics> {
    
    /**
     * 获取用户统计数据（实时计算）
     */
    @Select("SELECT " +
            "COUNT(*) as total_submissions, " +
            "SUM(CASE WHEN status = 'Accepted' THEN 1 ELSE 0 END) as accepted_count, " +
            "SUM(CASE WHEN status = 'Wrong Answer' THEN 1 ELSE 0 END) as wrong_answer_count, " +
            "SUM(CASE WHEN status = 'Time Limit Exceeded' THEN 1 ELSE 0 END) as time_limit_exceeded_count, " +
            "SUM(CASE WHEN status = 'Memory Limit Exceeded' THEN 1 ELSE 0 END) as memory_limit_exceeded_count, " +
            "SUM(CASE WHEN status = 'Runtime Error' THEN 1 ELSE 0 END) as runtime_error_count, " +
            "SUM(CASE WHEN status = 'Compilation Error' THEN 1 ELSE 0 END) as compilation_error_count " +
            "FROM record WHERE user_id = #{userId}")
    Map<String, Object> getUserStatsFromRecords(@Param("userId") Long userId);
    
    /**
     * 获取用户每日统计数据（用于学习曲线）
     */
    @Select("SELECT DATE(submit_time) as stat_date, " +
            "COUNT(*) as submissions, " +
            "SUM(CASE WHEN status = 'Accepted' THEN 1 ELSE 0 END) as accepted " +
            "FROM record " +
            "WHERE user_id = #{userId} AND submit_time >= #{startDate} " +
            "GROUP BY DATE(submit_time) " +
            "ORDER BY stat_date ASC")
    List<Map<String, Object>> getDailyStats(@Param("userId") Long userId, @Param("startDate") String startDate);
    
    /**
     * 获取用户排名（基于通过率）
     */
    @Select("SELECT COUNT(*) + 1 as rank FROM (" +
            "SELECT user_id, " +
            "SUM(CASE WHEN status = 'Accepted' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) as rate " +
            "FROM record " +
            "GROUP BY user_id " +
            "HAVING COUNT(*) >= 5" +
            ") as user_rates " +
            "WHERE rate > (" +
            "SELECT SUM(CASE WHEN status = 'Accepted' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) " +
            "FROM record WHERE user_id = #{userId}" +
            ")")
    Integer getUserRank(@Param("userId") Long userId);
    
    /**
     * 获取用户按题目统计的提交数据（不包含标签，标签通过Feign获取）
     */
    @Select("SELECT problem_id, " +
            "COUNT(id) as total_submissions, " +
            "SUM(CASE WHEN status = 'Accepted' THEN 1 ELSE 0 END) as accepted_count " +
            "FROM record " +
            "WHERE user_id = #{userId} " +
            "GROUP BY problem_id " +
            "ORDER BY total_submissions DESC")
    List<Map<String, Object>> getStatsByProblem(@Param("userId") Long userId);
}
