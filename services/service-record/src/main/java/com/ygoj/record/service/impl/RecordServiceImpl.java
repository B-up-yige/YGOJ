package com.ygoj.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;
import com.ygoj.record.RecordDetail;
import com.ygoj.record.RecordWithInfo;
import com.ygoj.record.UserDailyStats;
import com.ygoj.record.feign.JudgerFeignClient;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.feign.UserFeignClient;
import com.ygoj.record.mapper.RecordDetailMapper;
import com.ygoj.record.mapper.RecordMapper;
import com.ygoj.record.mapper.UserDailyStatsMapper;
import com.ygoj.record.mapper.UserStatisticsMapper;
import com.ygoj.record.Record;
import com.ygoj.record.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecordServiceImpl implements RecordService {
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private RecordDetailMapper recordDetailMapper;
    @Autowired
    private UserStatisticsMapper userStatisticsMapper;
    @Autowired
    private UserDailyStatsMapper userDailyStatsMapper;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private ProblemFeignClient problemFeignClient;
    @Autowired
    private JudgerFeignClient judgerFeignClient;
    @Autowired
    private Environment env;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 通过提交记录id获取提交记录信息
     *
     * @param id 提交记录id
     * @return {@link Record}
     */
    @Override
    public Record getRecordinfoById(Long id) {
        try {
            log.debug("获取提交记录, recordId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("记录ID不能为空");
            }
            
            Record record = recordMapper.selectById(id);
            return record;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取提交记录异常, recordId: {}", id, e);
            throw new RuntimeException("获取提交记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加提交记录
     *
     * @param record 提交记录
     * @return
     */
    @Override
    @GlobalTransactional
    public Result addRecord(Record record) {
        try {
            log.info("开始处理提交请求, userId: {}, problemId: {}, language: {}", 
                    record.getUserId(), record.getProblemId(), record.getLanguage());
            
            //1 校验数据
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

            //TODO 语言是否存在
            //用户是否存在
            Result userinfo = userFeignClient.userinfo(record.getUserId());
            if(userinfo.getData() == null){
                log.warn("提交失败: 用户不存在, userId: {}", record.getUserId());
                return Result.error(400, "用户不存在");
            }
            //题目是否存在
            Result problem = problemFeignClient.getProblemInfo(record.getProblemId());
            if(problem.getData() == null){
                log.warn("提交失败: 题目不存在, problemId: {}", record.getProblemId());
                return Result.error(400, "题目不存在");
            }

            // 如果是在比赛中提交，需要验证比赛时间
            if (record.getContestId() != null) {
                Result contestResult = problemFeignClient.getContestById(record.getContestId());
                if (contestResult.getData() == null) {
                    log.warn("提交失败: 比赛不存在, contestId: {}", record.getContestId());
                    return Result.error(400, "比赛不存在");
                }
                
                // 解析比赛信息
                com.fasterxml.jackson.databind.JsonNode contestData = objectMapper.convertValue(
                    contestResult.getData(), 
                    com.fasterxml.jackson.databind.JsonNode.class
                );
                
                String startTimeStr = contestData.get("startTime").asText();
                String endTimeStr = contestData.get("endTime").asText();
                
                // 兼容两种时间格式："2026-04-13T19:53:03" 和 "2026-04-13 19:53:03"
                LocalDateTime startTime = startTimeStr.contains("T") 
                    ? LocalDateTime.parse(startTimeStr)
                    : LocalDateTime.parse(startTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime endTime = endTimeStr.contains("T")
                    ? LocalDateTime.parse(endTimeStr)
                    : LocalDateTime.parse(endTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime now = LocalDateTime.now();
                
                if (now.isBefore(startTime)) {
                    log.warn("提交失败: 比赛尚未开始, contestId: {}, startTime: {}", record.getContestId(), startTime);
                    return Result.error(400, "比赛尚未开始，开始时间: " + startTime);
                }
                
                if (now.isAfter(endTime)) {
                    log.warn("提交失败: 比赛已结束, contestId: {}, endTime: {}", record.getContestId(), endTime);
                    return Result.error(400, "比赛已结束，结束时间: " + endTime);
                }
                
                log.info("比赛时间验证通过, contestId: {}, startTime: {}, endTime: {}", 
                    record.getContestId(), startTime, endTime);
            }

            //2 添加信息到数据库
            record.setStatus("waiting");
            recordMapper.insert(record);
            log.info("提交记录已保存, recordId: {}", record.getId());

            //3 请求judger接口
            SandboxExecuteRequest sandboxExecuteRequest = new SandboxExecuteRequest();
            sandboxExecuteRequest.setRecordId(record.getId());
            sandboxExecuteRequest.setCode(record.getCode());
            sandboxExecuteRequest.setLanguage(record.getLanguage());

            //获取运行配置
            String language = record.getLanguage();
            sandboxExecuteRequest.setCompileCommand(
                    env.getProperty(String.format("language.%s.compile", language))
            );
            sandboxExecuteRequest.setRunCommand(
                    env.getProperty(String.format("language.%s.run", language))
            );
            sandboxExecuteRequest.setImage(
                    env.getProperty(String.format("language.%s.image", language))
            );
            sandboxExecuteRequest.setFileName(
                    env.getProperty(String.format("language.%s.fileName", language))
            );

            //获取测试用例
            Result testCaseResult = problemFeignClient.getTestCase(record.getProblemId());
            List<Testcase> testcaseList = objectMapper.convertValue(
                testCaseResult.getData(), 
                objectMapper.getTypeFactory().constructParametricType(List.class, Testcase.class)
            );
            
            if (testcaseList == null || testcaseList.isEmpty()) {
                log.warn("提交失败: 题目没有测试用例, problemId: {}", record.getProblemId());
                return Result.error(400, "题目没有测试用例");
            }
            
            List<String> inputList = testcaseList.stream().map(Testcase::getInputFileId).toList();
            List<String> outputList = testcaseList.stream().map(Testcase::getOutputFileId).toList();

            sandboxExecuteRequest.setInputList(inputList);
            sandboxExecuteRequest.setOutputList(outputList);

            Probleminfo probleminfo = objectMapper.convertValue(problem.getData(), Probleminfo.class);
            sandboxExecuteRequest.setTimeLimit(probleminfo.getTimeLimit());
            sandboxExecuteRequest.setMemoryLimit(probleminfo.getMemoryLimit());

            //调用judger
            log.info("发送判题请求到judger服务, recordId: {}", record.getId());
            Result judge = judgerFeignClient.judge(sandboxExecuteRequest);
            log.info("判题请求发送成功, recordId: {}", record.getId());

            return Result.success();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加提交记录异常", e);
            return Result.error(500, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取提交列表
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @param contestId 比赛ID（可选）
     * @return {@link List<Record>}
     */
    @Override
    public List<Record> list(Long page, Long pageSize, Long contestId) {
        try {
            log.debug("分页查询提交记录, page: {}, pageSize: {}, contestId: {}", 
                    page, pageSize, contestId);
            
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10L;
            }
            
            Page<Record> recordPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
            
            // 根据上下文过滤
            if (contestId != null) {
                wrapper.eq(Record::getContestId, contestId);
            }
            
            wrapper.orderByDesc(Record::getSubmitTime);
            return recordMapper.selectPage(recordPage, wrapper).getRecords();
        } catch (Exception e) {
            log.error("分页查询提交记录异常", e);
            throw new RuntimeException("分页查询提交记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取带题目名称和用户昵称的记录列表
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @param contestId 比赛ID（可选）
     * @return {@link List<RecordWithInfo>}
     */
    @Override
    public List<RecordWithInfo> listWithInfo(Long page, Long pageSize, Long contestId) {
        try {
            log.debug("分页查询提交记录(带信息), page: {}, pageSize: {}, contestId: {}", 
                    page, pageSize, contestId);
            
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10L;
            }
            
            // 先获取记录列表
            List<Record> records = list(page, pageSize, contestId);
            
            // 转换为RecordWithInfo并填充题目名称和用户昵称
            List<RecordWithInfo> result = new ArrayList<>();
            for (Record record : records) {
                RecordWithInfo recordWithInfo = new RecordWithInfo();
                // 复制基本字段
                recordWithInfo.setId(record.getId());
                recordWithInfo.setUserId(record.getUserId());
                recordWithInfo.setProblemId(record.getProblemId());
                recordWithInfo.setContestId(record.getContestId());
                recordWithInfo.setCode(record.getCode());
                recordWithInfo.setStatus(record.getStatus());
                recordWithInfo.setLanguage(record.getLanguage());
                recordWithInfo.setCompileTime(record.getCompileTime());
                recordWithInfo.setCompileMemory(record.getCompileMemory());
                recordWithInfo.setCompileStdout(record.getCompileStdout());
                recordWithInfo.setCompileStderr(record.getCompileStderr());
                recordWithInfo.setSubmitTime(record.getSubmitTime());
                
                // 通过Feign获取用户信息
                try {
                    Result userInfoResult = userFeignClient.userinfo(record.getUserId());
                    if (userInfoResult != null && userInfoResult.getData() != null) {
                        com.fasterxml.jackson.databind.JsonNode userInfo = objectMapper.convertValue(
                            userInfoResult.getData(), com.fasterxml.jackson.databind.JsonNode.class);
                        recordWithInfo.setUserName(userInfo.get("nickname").asText());
                    }
                } catch (Exception e) {
                    log.warn("获取用户信息失败, userId: {}", record.getUserId(), e);
                    recordWithInfo.setUserName("未知用户");
                }
                
                // 通过Feign获取题目信息
                try {
                    Result problemInfoResult = problemFeignClient.getProblemInfo(record.getProblemId());
                    if (problemInfoResult != null && problemInfoResult.getData() != null) {
                        com.fasterxml.jackson.databind.JsonNode problemInfo = objectMapper.convertValue(
                            problemInfoResult.getData(), com.fasterxml.jackson.databind.JsonNode.class);
                        recordWithInfo.setProblemTitle(problemInfo.get("title").asText());
                    }
                } catch (Exception e) {
                    log.warn("获取题目信息失败, problemId: {}", record.getProblemId(), e);
                    recordWithInfo.setProblemTitle("未知题目");
                }
                
                result.add(recordWithInfo);
            }
            
            return result;
        } catch (Exception e) {
            log.error("分页查询提交记录(带信息)异常", e);
            throw new RuntimeException("分页查询提交记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<RecordDetail> getRecordDetails(Long recordId) {
        try {
            log.debug("获取测试点详情, recordId: {}", recordId);
            
            if (recordId == null) {
                throw new IllegalArgumentException("记录ID不能为空");
            }
            
            LambdaQueryWrapper<RecordDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecordDetail::getRecordId, recordId);
            return recordDetailMapper.selectList(wrapper);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取测试点详情异常, recordId: {}", recordId, e);
            throw new RuntimeException("获取测试点详情失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        try {
            log.debug("获取用户统计数据, userId: {}", userId);
            
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 从 record 表实时计算统计数据
            Map<String, Object> stats = userStatisticsMapper.getUserStatsFromRecords(userId);
            
            // 处理stats为null的情况（用户无任何提交记录）
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("total_submissions", 0L);
                stats.put("accepted_count", 0L);
                stats.put("wrong_answer_count", 0L);
                stats.put("time_limit_exceeded_count", 0L);
                stats.put("memory_limit_exceeded_count", 0L);
                stats.put("runtime_error_count", 0L);
                stats.put("compilation_error_count", 0L);
            }
            
            // 处理null值（用户无提交记录时）
            Long totalSubmissions = stats.get("total_submissions") != null ? 
                ((Number) stats.get("total_submissions")).longValue() : 0L;
            Long acceptedCount = stats.get("accepted_count") != null ? 
                ((Number) stats.get("accepted_count")).longValue() : 0L;
            Long wrongAnswerCount = stats.get("wrong_answer_count") != null ? 
                ((Number) stats.get("wrong_answer_count")).longValue() : 0L;
            Long timeLimitExceededCount = stats.get("time_limit_exceeded_count") != null ? 
                ((Number) stats.get("time_limit_exceeded_count")).longValue() : 0L;
            Long memoryLimitExceededCount = stats.get("memory_limit_exceeded_count") != null ? 
                ((Number) stats.get("memory_limit_exceeded_count")).longValue() : 0L;
            Long runtimeErrorCount = stats.get("runtime_error_count") != null ? 
                ((Number) stats.get("runtime_error_count")).longValue() : 0L;
            Long compilationErrorCount = stats.get("compilation_error_count") != null ? 
                ((Number) stats.get("compilation_error_count")).longValue() : 0L;
            
            // 计算通过率
            double acceptanceRate = totalSubmissions > 0 ? (acceptedCount * 100.0 / totalSubmissions) : 0.0;
            
            // 获取排名
            Integer rank = userStatisticsMapper.getUserRank(userId);
            if (rank == null) {
                rank = 0;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalSubmissions", totalSubmissions);
            result.put("acceptedCount", acceptedCount);
            result.put("wrongAnswerCount", wrongAnswerCount);
            result.put("timeLimitExceededCount", timeLimitExceededCount);
            result.put("memoryLimitExceededCount", memoryLimitExceededCount);
            result.put("runtimeErrorCount", runtimeErrorCount);
            result.put("compilationErrorCount", compilationErrorCount);
            result.put("acceptanceRate", Math.round(acceptanceRate * 100.0) / 100.0);
            result.put("rank", rank);
            
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户统计数据异常, userId: {}", userId, e);
            throw new RuntimeException("获取用户统计数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserLearningCurve(Long userId, Integer days) {
        try {
            log.debug("获取用户学习曲线数据, userId: {}, days: {}", userId, days);
            
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            if (days == null || days < 1) {
                days = 30; // 默认30天
            }
            
            // 计算起始日期
            String startDate = LocalDate.now().minusDays(days).toString();
            
            // 获取每日统计数据
            List<Map<String, Object>> dailyStats = userStatisticsMapper.getDailyStats(userId, startDate);
            
            return dailyStats;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户学习曲线数据异常, userId: {}", userId, e);
            throw new RuntimeException("获取用户学习曲线数据失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserStatsByTag(Long userId) {
        try {
            log.debug("获取用户按标签统计数据, userId: {}", userId);
            
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 1. 获取用户按题目统计的数据
            List<Map<String, Object>> problemStats = userStatisticsMapper.getStatsByProblem(userId);
            
            if (problemStats == null || problemStats.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 按标签聚合统计数据
            Map<String, Map<String, Object>> tagStatsMap = new LinkedHashMap<>();
            
            for (Map<String, Object> problemStat : problemStats) {
                Long problemId = ((Number) problemStat.get("problem_id")).longValue();
                Long totalSubmissions = problemStat.get("total_submissions") != null ? 
                    ((Number) problemStat.get("total_submissions")).longValue() : 0L;
                Long acceptedCount = problemStat.get("accepted_count") != null ? 
                    ((Number) problemStat.get("accepted_count")).longValue() : 0L;
                
                // 通过 Feign 获取题目标签
                try {
                    Result tagsResult = problemFeignClient.getProblemTags(problemId);
                    if (tagsResult != null && tagsResult.getData() != null) {
                        List<Map<String, Object>> tagsList = (List<Map<String, Object>>) tagsResult.getData();
                        
                        // 如果题目没有标签，使用 "未分类"
                        if (tagsList.isEmpty()) {
                            aggregateTagStats(tagStatsMap, "未分类", totalSubmissions, acceptedCount);
                        } else {
                            // 将统计数据分配到每个标签
                            for (Map<String, Object> tagObj : tagsList) {
                                String tag = (String) tagObj.get("tag");
                                if (tag != null && !tag.trim().isEmpty()) {
                                    aggregateTagStats(tagStatsMap, tag, totalSubmissions, acceptedCount);
                                }
                            }
                        }
                    } else {
                        aggregateTagStats(tagStatsMap, "未分类", totalSubmissions, acceptedCount);
                    }
                } catch (Exception e) {
                    log.warn("获取题目 {} 的标签失败，归类为未分类", problemId, e);
                    aggregateTagStats(tagStatsMap, "未分类", totalSubmissions, acceptedCount);
                }
            }
            
            // 3. 转换为列表并计算通过率
            List<Map<String, Object>> result = new ArrayList<>(tagStatsMap.values());
            
            // 按提交次数排序
            result.sort((a, b) -> {
                Long submissionsA = ((Number) a.get("total_submissions")).longValue();
                Long submissionsB = ((Number) b.get("total_submissions")).longValue();
                return submissionsB.compareTo(submissionsA);
            });
            
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户按标签统计数据异常, userId: {}", userId, e);
            throw new RuntimeException("获取用户按标签统计数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 聚合计标签统计数据
     */
    private void aggregateTagStats(Map<String, Map<String, Object>> tagStatsMap, String tag, 
                                   Long totalSubmissions, Long acceptedCount) {
        if (!tagStatsMap.containsKey(tag)) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("tag", tag);
            stat.put("total_submissions", 0L);
            stat.put("accepted_count", 0L);
            stat.put("acceptance_rate", 0.0);
            tagStatsMap.put(tag, stat);
        }
        
        Map<String, Object> stat = tagStatsMap.get(tag);
        Long currentSubmissions = ((Number) stat.get("total_submissions")).longValue();
        Long currentAccepted = ((Number) stat.get("accepted_count")).longValue();
        
        stat.put("total_submissions", currentSubmissions + totalSubmissions);
        stat.put("accepted_count", currentAccepted + acceptedCount);
        
        // 计算通过率
        Long newTotal = currentSubmissions + totalSubmissions;
        Long newAccepted = currentAccepted + acceptedCount;
        double rate = newTotal > 0 ? (newAccepted * 100.0 / newTotal) : 0.0;
        stat.put("acceptance_rate", Math.round(rate * 100.0) / 100.0);
    }
    
    @Override
    public void updateUserStatistics(Long recordId, String status) {
        try {
            log.debug("更新用户统计数据, recordId: {}, status: {}", recordId, status);
            
            if (recordId == null || status == null) {
                return;
            }
            
            // 1. 获取记录的用户ID和题目ID
            Record record = recordMapper.selectById(recordId);
            if (record == null) {
                log.warn("记录不存在, recordId: {}", recordId);
                return;
            }
            
            // 2. 更新 user_daily_stats 表（直接使用传入的status，避免事务未提交导致的数据不一致）
            updateDailyStats(record.getUserId(), record.getProblemId(), status);
            
            log.debug("用户统计数据更新完成, recordId: {}, userId: {}", recordId, record.getUserId());
        } catch (Exception e) {
            log.error("更新用户统计数据异常, recordId: {}", recordId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 更新每日统计数据
     */
    /**
     * 更新每日统计数据
     */
    private void updateDailyStats(Long userId, Long problemId, String status) {
        try {
            // 查询今日是否已有统计记录
            Long statsId = userDailyStatsMapper.findTodayStatsId(userId);
            
            UserDailyStats dailyStats;
            boolean isAccepted = "AC".equals(status);
            
            if (statsId != null) {
                // 更新现有记录
                dailyStats = userDailyStatsMapper.selectById(statsId);
                dailyStats.setSubmissions(dailyStats.getSubmissions() + 1);
                if (isAccepted) {
                    dailyStats.setAccepted(dailyStats.getAccepted() + 1);
                    // 检查该题目是否首次通过（需要查询历史记录）
                    LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Record::getUserId, userId)
                           .eq(Record::getProblemId, problemId)
                           .eq(Record::getStatus, "AC")
                           .lt(Record::getId, getCurrentRecordId()); // 排除当前记录
                    Long acceptedCount = recordMapper.selectCount(wrapper);
                    if (acceptedCount == 0) {
                        // 首次通过该题
                        dailyStats.setProblemsSolved(dailyStats.getProblemsSolved() + 1);
                    }
                }
                userDailyStatsMapper.updateById(dailyStats);
            } else {
                // 创建新记录
                dailyStats = new UserDailyStats();
                dailyStats.setUserId(userId);
                dailyStats.setStatDate(LocalDate.now());
                dailyStats.setSubmissions(1);
                dailyStats.setAccepted(isAccepted ? 1 : 0);
                dailyStats.setProblemsSolved(isAccepted ? 1 : 0);
                userDailyStatsMapper.insert(dailyStats);
            }
            
            log.debug("每日统计数据更新成功, userId: {}, date: {}, status: {}", userId, LocalDate.now(), status);
        } catch (Exception e) {
            log.error("更新每日统计数据失败, userId: {}", userId, e);
        }
    }
    
    /**
     * 获取当前最大的record ID（用于判断是否首次通过）
     */
    private Long getCurrentRecordId() {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Record::getId).last("LIMIT 1");
        Record record = recordMapper.selectOne(wrapper);
        return record != null ? record.getId() : 0L;
    }
    
    @Override
    public Map<Long, String> getUserContestProgress(Long userId, Long contestId) {
        try {
            log.debug("获取用户比赛过题情况, userId: {}, contestId: {}", userId, contestId);
            
            if (userId == null || contestId == null) {
                return new HashMap<>();
            }
            
            // 查询用户在比赛中所有题目的提交记录
            LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Record::getUserId, userId)
                   .eq(Record::getContestId, contestId)
                   .orderByAsc(Record::getSubmitTime);
            List<Record> records = recordMapper.selectList(wrapper);
            
            // 对每个题目，如果有AC则返回AC，否则返回最新状态
            Map<Long, String> progress = new HashMap<>();
            Map<Long, Record> latestRecords = new HashMap<>();
            
            for (Record record : records) {
                Long problemId = record.getProblemId();
                String status = record.getStatus();
                
                // 如果已经有AC记录，保持AC
                if ("AC".equals(progress.get(problemId))) {
                    continue;
                }
                
                // 如果当前是AC，直接设置为AC
                if ("AC".equals(status)) {
                    progress.put(problemId, "AC");
                } else {
                    // 否则更新为最新状态
                    progress.put(problemId, status);
                }
            }
            
            return progress;
        } catch (Exception e) {
            log.error("获取用户比赛过题情况异常, userId: {}, contestId: {}", userId, contestId, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<Long, String> getUserProblemsetProgress(Long userId, Long problemsetId) {
        try {
            log.debug("获取用户题集过题情况, userId: {}, problemsetId: {}", userId, problemsetId);
            
            if (userId == null || problemsetId == null) {
                return new HashMap<>();
            }
            
            // 通过 Feign 调用获取题集中的题目列表
            Result problemsetProblemsResult = problemFeignClient.getProblemsetProblems(problemsetId);
            if (problemsetProblemsResult.getData() == null) {
                return new HashMap<>();
            }
            
            // 解析题目列表
            List<Map<String, Object>> problemsetProblems = objectMapper.convertValue(
                problemsetProblemsResult.getData(),
                objectMapper.getTypeFactory().constructParametricType(List.class, Map.class)
            );
            
            if (problemsetProblems.isEmpty()) {
                return new HashMap<>();
            }
            
            List<Long> problemIds = problemsetProblems.stream()
                .map(p -> ((Number) p.get("problemId")).longValue())
                .collect(java.util.stream.Collectors.toList());
            
            // 查询用户在这些题目上的所有提交记录
            LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Record::getUserId, userId)
                   .in(Record::getProblemId, problemIds)
                   .orderByAsc(Record::getSubmitTime);
            List<Record> records = recordMapper.selectList(wrapper);
            
            // 对每个题目，如果有AC则返回AC，否则返回最新状态
            Map<Long, String> progress = new HashMap<>();
            
            for (Record record : records) {
                Long problemId = record.getProblemId();
                String status = record.getStatus();
                
                // 如果已经有AC记录，保持AC
                if ("AC".equals(progress.get(problemId))) {
                    continue;
                }
                
                // 如果当前是AC，直接设置为AC
                if ("AC".equals(status)) {
                    progress.put(problemId, "AC");
                } else {
                    // 否则更新为最新状态
                    progress.put(problemId, status);
                }
            }
            
            return progress;
        } catch (Exception e) {
            log.error("获取用户题集过题情况异常, userId: {}, problemsetId: {}", userId, problemsetId, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Result rejudge(Long recordId) {
        try {
            log.info("开始重新判题, recordId: {}", recordId);
            
            if (recordId == null) {
                return Result.error(400, "记录ID不能为空");
            }
            
            // 1. 获取原提交记录
            Record record = recordMapper.selectById(recordId);
            if (record == null) {
                log.warn("重测失败: 记录不存在, recordId: {}", recordId);
                return Result.error(404, "提交记录不存在");
            }
            
            // 2. 重置记录状态为waiting
            record.setStatus("waiting");
            record.setCompileTime(null);
            record.setCompileMemory(null);
            record.setCompileStdout(null);
            record.setCompileStderr(null);
            recordMapper.updateById(record);
            log.info("记录状态已重置为waiting, recordId: {}", recordId);
            
            // 3. 构建判题请求（复用addRecord中的逻辑）
            SandboxExecuteRequest sandboxExecuteRequest = new SandboxExecuteRequest();
            sandboxExecuteRequest.setRecordId(record.getId());
            sandboxExecuteRequest.setCode(record.getCode());
            sandboxExecuteRequest.setLanguage(record.getLanguage());

            // 获取运行配置
            String language = record.getLanguage();
            sandboxExecuteRequest.setCompileCommand(
                    env.getProperty(String.format("language.%s.compile", language))
            );
            sandboxExecuteRequest.setRunCommand(
                    env.getProperty(String.format("language.%s.run", language))
            );
            sandboxExecuteRequest.setImage(
                    env.getProperty(String.format("language.%s.image", language))
            );
            sandboxExecuteRequest.setFileName(
                    env.getProperty(String.format("language.%s.fileName", language))
            );

            // 获取测试用例
            Result testCaseResult = problemFeignClient.getTestCase(record.getProblemId());
            List<Testcase> testcaseList = objectMapper.convertValue(
                testCaseResult.getData(), 
                objectMapper.getTypeFactory().constructParametricType(List.class, Testcase.class)
            );
            
            if (testcaseList == null || testcaseList.isEmpty()) {
                log.warn("重测失败: 题目没有测试用例, problemId: {}", record.getProblemId());
                return Result.error(400, "题目没有测试用例");
            }
            
            List<String> inputList = testcaseList.stream().map(Testcase::getInputFileId).toList();
            List<String> outputList = testcaseList.stream().map(Testcase::getOutputFileId).toList();

            sandboxExecuteRequest.setInputList(inputList);
            sandboxExecuteRequest.setOutputList(outputList);

            // 获取题目信息
            Result problem = problemFeignClient.getProblemInfo(record.getProblemId());
            Probleminfo probleminfo = objectMapper.convertValue(problem.getData(), Probleminfo.class);
            sandboxExecuteRequest.setTimeLimit(probleminfo.getTimeLimit());
            sandboxExecuteRequest.setMemoryLimit(probleminfo.getMemoryLimit());

            // 4. 调用judger服务
            log.info("发送重测请求到judger服务, recordId: {}", record.getId());
            Result judge = judgerFeignClient.judge(sandboxExecuteRequest);
            log.info("重测请求发送成功, recordId: {}", record.getId());

            return Result.success("重测任务已提交");
        } catch (Exception e) {
            log.error("重测失败, recordId: {}", recordId, e);
            return Result.error(500, "重测失败: " + e.getMessage());
        }
    }
}
