package com.ygoj.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ygoj.record.UserDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDailyStatsMapper extends BaseMapper<UserDailyStats> {
    
    /**
     * 查询或创建今日统计记录
     */
    @Select("SELECT id FROM user_daily_stats WHERE user_id = #{userId} AND stat_date = CURDATE()")
    Long findTodayStatsId(@Param("userId") Long userId);
}
