package com.ygoj.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;
import com.ygoj.record.RecordDetail;
import com.ygoj.record.UserDailyStats;
import com.ygoj.record.feign.JudgerFeignClient;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.feign.UserFeignClient;
import com.ygoj.record.mapper.RecordDetailMapper;
import com.ygoj.record.mapper.RecordMapper;
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
    private com.ygoj.record.mapper.UserDailyStatsMapper userDailyStatsMapper;
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
     * @return {@link List<Record>}
     */
    @Override
    public List<Record> list(Long page, Long pageSize) {
        try {
            log.debug("分页查询提交记录, page: {}, pageSize: {}", page, pageSize);
            
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10L;
            }
            
            Page<Record> recordPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Record::getSubmitTime);
            return recordMapper.selectPage(recordPage, wrapper).getRecords();
        } catch (Exception e) {
            log.error("分页查询提交记录异常", e);
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
    public void updateUserStatistics(Long userId) {
        try {
            log.debug("更新用户统计数据, userId: {}", userId);
            
            if (userId == null) {
                return;
            }
            
            // 1. 获取该用户最新的提交记录（刚判题完成的）
            LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Record::getUserId, userId)
                   .orderByDesc(Record::getSubmitTime)
                   .last("LIMIT 1");
            Record latestRecord = recordMapper.selectOne(wrapper);
            
            if (latestRecord == null) {
                return;
            }
            
            // 2. 更新 user_daily_stats 表
            updateDailyStats(userId, latestRecord);
            
            log.debug("用户统计数据更新完成, userId: {}", userId);
        } catch (Exception e) {
            log.error("更新用户统计数据异常, userId: {}", userId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 更新每日统计数据
     */
    private void updateDailyStats(Long userId, Record record) {
        try {
            // 查询今日是否已有统计记录
            Long statsId = userDailyStatsMapper.findTodayStatsId(userId);
            
            UserDailyStats dailyStats;
            if (statsId != null) {
                // 更新现有记录
                dailyStats = userDailyStatsMapper.selectById(statsId);
                dailyStats.setSubmissions(dailyStats.getSubmissions() + 1);
                if ("Accepted".equals(record.getStatus())) {
                    dailyStats.setAccepted(dailyStats.getAccepted() + 1);
                }
                userDailyStatsMapper.updateById(dailyStats);
            } else {
                // 创建新记录
                dailyStats = new UserDailyStats();
                dailyStats.setUserId(userId);
                dailyStats.setStatDate(LocalDate.now());
                dailyStats.setSubmissions(1);
                dailyStats.setAccepted("Accepted".equals(record.getStatus()) ? 1 : 0);
                dailyStats.setProblemsSolved("Accepted".equals(record.getStatus()) ? 1 : 0);
                userDailyStatsMapper.insert(dailyStats);
            }
            
            log.debug("每日统计数据更新成功, userId: {}, date: {}", userId, LocalDate.now());
        } catch (Exception e) {
            log.error("更新每日统计数据失败, userId: {}", userId, e);
        }
    }
}
