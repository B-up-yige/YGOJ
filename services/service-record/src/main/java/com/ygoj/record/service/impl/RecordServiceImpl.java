package com.ygoj.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;
import com.ygoj.record.RecordDetail;
import com.ygoj.record.feign.JudgerFeignClient;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.feign.UserFeignClient;
import com.ygoj.record.mapper.RecordDetailMapper;
import com.ygoj.record.mapper.RecordMapper;
import com.ygoj.record.Record;
import com.ygoj.record.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RecordServiceImpl implements RecordService {
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private RecordDetailMapper recordDetailMapper;
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
}
