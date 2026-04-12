package com.ygoj.record;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_statistics")
public class UserStatistics {
    @TableId
    private Long userId;
    private Integer totalSubmissions;
    private Integer acceptedCount;
    private Integer wrongAnswerCount;
    private Integer timeLimitExceededCount;
    private Integer memoryLimitExceededCount;
    private Integer runtimeErrorCount;
    private Integer compilationErrorCount;
    private BigDecimal acceptanceRate;
    private LocalDateTime lastUpdateTime;
}
