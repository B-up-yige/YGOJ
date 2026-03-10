package com.ygoj.record.service.impl;

import com.ygoj.record.mapper.RecordMapper;
import com.ygoj.record.Record;
import com.ygoj.record.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecordServiceImpl implements RecordService {
//    @Autowired
//    private LoadBalancerClient loadBalancer;
//    @Autowired
//    private RestTemplate restTemplate;
//    @Autowired
//    private ProblemFeignClient ProblemfeignClient;
//    @Autowired
//    private RecordMapper recordMapper;
//
////    @SentinelResource(value = "queryRecord", blockHandler = "getRecordByIdFallback")
////    @SentinelResource(value = "queryRecord")
//    public Record getRecordById(Long id) {
//        Record record = recordMapper.selectById(id);
//        System.out.println(record.toString());
//        User user = getUserFromRemoteById(record.getUserId());
//        record.setUser(user);
//        return record;
//    }
//
//    @Override
//    @GlobalTransactional
//    public void submit(String status, Long userId) {
//        Record record = new Record();
//        record.setStatus(status);
//        record.setUserId(userId);
//        recordMapper.insert(record);
//
//        String url = "http://service-user/addcnt?userId="+userId;
//        restTemplate.getForObject(url, String.class);
//    }
//
//    //兜底回调
//    public Record getRecordByIdFallback(Long id, BlockException e) {
//        Record record = new Record();
//        record.setId(id);
//        record.setStatus("AC");
//        return record;
//    }
//
//    public User getUserFromRemoteById(Long id){
//        String url = "http://service-user/user/"+id;
//
//        log.info("远程调用：{}", url);
//        User user = restTemplate.getForObject(url, User.class);
//
//        return user;
//    }
//
//    public Problem getProblemFromRemoteById(Long id){
//        Problem problem = ProblemfeignClient.getProblemById(id);
//
//        return problem;
//    }

    @Autowired
    private RecordMapper recordMapper;

    @Override
    public Record getRecordinfoById(Long id) {
        Record record = recordMapper.selectById(id);
        return record;
    }

    @Override
    @GlobalTransactional
    public void addRecord(Record record) {
        //TODO:数据检验
        //检验userId
        //检验problemId
        recordMapper.insert(record);
    }

    @Override
    @GlobalTransactional
    public void editRecordStatus(Long id, String status) {
        Record record = recordMapper.selectById(id);
        record.setStatus(status);
        recordMapper.updateById(record);
    }
}
