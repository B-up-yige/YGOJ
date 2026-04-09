package com.ygoj.record.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ygoj.common.Result;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;
import com.ygoj.record.feign.JudgerFeignClient;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.feign.UserFeignClient;
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
        Record record = recordMapper.selectById(id);
        return record;
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
        //1 校验数据

        //TODO 语言是否存在
        //用户是否存在
        Result userinfo = userFeignClient.userinfo(record.getUserId());
        if(userinfo.getData() == null){
            return Result.error(400, "用户不存在");
        }
        //题目是否存在
        Result problem = problemFeignClient.getProblemInfo(record.getProblemId());
        if(problem.getData() == null){
            return Result.error(400, "题目不存在");
        }

        //2 添加信息到数据库
        record.setStatus("waiting");
        recordMapper.insert(record);

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
        List<String> inputList = testcaseList.stream().map(Testcase::getInputFileId).toList();
        List<String> outputList = testcaseList.stream().map(Testcase::getOutputFileId).toList();

        sandboxExecuteRequest.setInputList(inputList);
        sandboxExecuteRequest.setOutputList(outputList);

        Probleminfo probleminfo = objectMapper.convertValue(problem.getData(), Probleminfo.class);
        sandboxExecuteRequest.setTimeLimit(probleminfo.getTimeLimit());
        sandboxExecuteRequest.setMemoryLimit(probleminfo.getMemoryLimit());

        //调用judger
        Result judge = judgerFeignClient.judge(sandboxExecuteRequest);

        return Result.success();
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
        Page<Record> recordPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Record::getSubmitTime);
        return recordMapper.selectPage(recordPage, wrapper).getRecords();
    }
}
