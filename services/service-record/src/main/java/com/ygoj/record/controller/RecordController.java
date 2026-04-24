package com.ygoj.record.controller;

import com.ygoj.common.Result;
import com.ygoj.record.Record;
import com.ygoj.record.feign.JudgerFeignClient;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.feign.UserFeignClient;
import com.ygoj.record.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/record")
public class RecordController {
    @Autowired
    private RecordService recordService;

    /**
     * 获取提交记录信息(公开访问)
     *
     * @param id 提交记录id
     * @return {@link Result}
     */
    @GetMapping("/recordinfo/{id}")
    public Result getRecordInfo(@PathVariable Long id) {
        try {
            log.info("获取提交记录请求, recordId: {}", id);
            
            if (id == null) {
                return Result.error(400, "记录ID不能为空");
            }
            
            // 使用带信息的方法，返回用户昵称和题目标题
            com.ygoj.record.RecordWithInfo record = recordService.getRecordWithInfoById(id);
            if (record == null) {
                log.warn("提交记录不存在, recordId: {}", id);
                return Result.error(404, "提交记录不存在");
            }
            
            log.info("获取提交记录成功, recordId: {}", id);
            return Result.success(record);
        } catch (Exception e) {
            log.error("获取提交记录失败, recordId: {}", id, e);
            return Result.error(500, "获取提交记录失败: " + e.getMessage());
        }
    }

    /**
     * 添加提交记录(需要提交代码权限)
     *
     * @param record 提交记录
     * @return {@link Result}
     */
    @PreAuthorize("hasAuthority('PROBLEM_SUBMIT')")
    @PostMapping("/submit")
    public Result addRecord(@RequestBody Record record) {
        try {
            log.info("提交代码请求, userId: {}, problemId: {}, language: {}", 
                    record.getUserId(), record.getProblemId(), record.getLanguage());
            
            // 参数校验
            if (record == null) {
                return Result.error(400, "提交信息不能为空");
            }
            if (record.getUserId() == null) {
                return Result.error(400, "用户ID不能为空");
            }
            if (record.getProblemId() == null) {
                return Result.error(400, "题目ID不能为空");
            }
            if (record.getCode() == null || record.getCode().trim().isEmpty()) {
                return Result.error(400, "代码不能为空");
            }
            if (record.getLanguage() == null || record.getLanguage().trim().isEmpty()) {
                return Result.error(400, "编程语言不能为空");
            }
            
            return recordService.addRecord(record);
        } catch (Exception e) {
            log.error("提交代码失败", e);
            return Result.error(500, "提交代码失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取提交列表(公开访问)
     *
     * @param page           页面
     * @param pageSize       页面大小
     * @param contestId      比赛ID(可选)
     * @param problemId      题目ID(可选)
     * @param status         状态(可选)
     * @param userId         用户ID(可选)
     * @param mySubmissions  是否只看我的提交(可选)
     * @return {@link Result}
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Long page,
                       @RequestParam(required = false) Long pageSize,
                       @RequestParam(required = false) Long contestId,
                       @RequestParam(required = false) Long problemId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) Boolean mySubmissions) {
        try {
            log.debug("获取提交列表请求, page: {}, pageSize: {}, contestId: {}, problemId: {}, status: {}, userId: {}, mySubmissions: {}", 
                    page, pageSize, contestId, problemId, status, userId, mySubmissions);
            
            // 参数校验和默认值设置
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10L;
            }
            
            // 返回带题目名称和用户昵称的记录列表
            return Result.success(recordService.listWithInfo(page, pageSize, contestId, problemId, status, userId, mySubmissions));
        } catch (Exception e) {
            log.error("获取提交列表失败", e);
            return Result.error(500, "获取提交列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取提交记录的测试点详情(公开访问)
     *
     * @param id 提交记录id
     * @return {@link Result}
     */
    @GetMapping("/recordinfo/{id}/details")
    public Result getRecordDetails(@PathVariable Long id) {
        try {
            log.info("获取提交记录测试点详情, recordId: {}", id);
            
            if (id == null) {
                return Result.error(400, "记录ID不能为空");
            }
            
            return Result.success(recordService.getRecordDetails(id));
        } catch (Exception e) {
            log.error("获取测试点详情失败, recordId: {}", id, e);
            return Result.error(500, "获取测试点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计数据(公开访问)
     *
     * @param userId 用户ID
     * @return {@link Result}
     */
    @GetMapping("/statistics/{userId}")
    public Result getUserStatistics(@PathVariable Long userId) {
        try {
            log.info("获取用户统计数据, userId: {}", userId);
            
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            return Result.success(recordService.getUserStatistics(userId));
        } catch (Exception e) {
            log.error("获取用户统计数据失败, userId: {}", userId, e);
            return Result.error(500, "获取用户统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户学习曲线数据(公开访问)
     *
     * @param userId 用户ID
     * @param days   天数(默认30天)
     * @return {@link Result}
     */
    @GetMapping("/learning-curve/{userId}")
    public Result getUserLearningCurve(@PathVariable Long userId, @RequestParam(required = false, defaultValue = "30") Integer days) {
        try {
            log.info("获取用户学习曲线数据, userId: {}, days: {}", userId, days);
            
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            return Result.success(recordService.getUserLearningCurve(userId, days));
        } catch (Exception e) {
            log.error("获取用户学习曲线数据失败, userId: {}", userId, e);
            return Result.error(500, "获取用户学习曲线数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户按标签统计的数据(公开访问)
     *
     * @param userId 用户ID
     * @return {@link Result}
     */
    @GetMapping("/statistics/{userId}/by-tag")
    public Result getUserStatsByTag(@PathVariable Long userId) {
        try {
            log.info("获取用户按标签统计数据, userId: {}", userId);
            
            if (userId == null) {
                return Result.error(400, "用户ID不能为空");
            }
            
            return Result.success(recordService.getUserStatsByTag(userId));
        } catch (Exception e) {
            log.error("获取用户按标签统计数据失败, userId: {}", userId, e);
            return Result.error(500, "获取用户按标签统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户在比赛中的过题情况(公开访问)
     *
     * @param userId 用户ID
     * @param contestId 比赛ID
     * @return {@link Result}
     */
    @GetMapping("/contest-progress")
    public Result getUserContestProgress(@RequestParam Long userId, @RequestParam Long contestId) {
        try {
            log.info("获取用户比赛过题情况, userId: {}, contestId: {}", userId, contestId);
            return Result.success(recordService.getUserContestProgress(userId, contestId));
        } catch (Exception e) {
            log.error("获取用户比赛过题情况失败, userId: {}, contestId: {}", userId, contestId, e);
            return Result.error(500, "获取过题情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户在题集中的过题情况(公开访问)
     *
     * @param userId 用户ID
     * @param problemsetId 题集ID
     * @return {@link Result}
     */
    @GetMapping("/problemset-progress")
    public Result getUserProblemsetProgress(@RequestParam Long userId, @RequestParam Long problemsetId) {
        try {
            log.info("获取用户题集过题情况, userId: {}, problemsetId: {}", userId, problemsetId);
            return Result.success(recordService.getUserProblemsetProgress(userId, problemsetId));
        } catch (Exception e) {
            log.error("获取用户题集过题情况失败, userId: {}, problemsetId: {}", userId, problemsetId, e);
            return Result.error(500, "获取过题情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新判题(需要管理员权限)
     *
     * @param id 提交记录ID
     * @return {@link Result}
     */
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/rejudge/{id}")
    public Result rejudge(@PathVariable Long id) {
        try {
            log.info("重测请求, recordId: {}", id);
            
            if (id == null) {
                return Result.error(400, "记录ID不能为空");
            }
            
            return recordService.rejudge(id);
        } catch (Exception e) {
            log.error("重测失败, recordId: {}", id, e);
            return Result.error(500, "重测失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取比赛排行榜(公开访问)
     *
     * @param contestId 比赛ID
     * @return {@link Result}
     */
    @GetMapping("/contest-standings/{contestId}")
    public Result getContestStandings(@PathVariable Long contestId) {
        try {
            log.info("获取比赛排行榜请求, contestId: {}", contestId);
            
            if (contestId == null) {
                return Result.error(400, "比赛ID不能为空");
            }
            
            return Result.success(recordService.getContestStandings(contestId));
        } catch (Exception e) {
            log.error("获取比赛排行榜失败, contestId: {}", contestId, e);
            return Result.error(500, "获取比赛排行榜失败: " + e.getMessage());
        }
    }
}
