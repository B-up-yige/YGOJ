package com.ygoj.record.service.impl;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ygoj.problem.pojo.Problem;
import com.ygoj.record.feign.ProblemFeignClient;
import com.ygoj.record.mapper.RecordMapper;
import com.ygoj.record.pojo.Record;
import com.ygoj.record.service.RecordService;
import com.ygoj.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RecordServiceImpl implements RecordService {
    @Autowired
    private LoadBalancerClient loadBalancer;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ProblemFeignClient ProblemfeignClient;
    @Autowired
    private RecordMapper recordMapper;

//    @SentinelResource(value = "queryRecord", blockHandler = "getRecordByIdFallback")
//    @SentinelResource(value = "queryRecord")
    public Record getRecordById(Long id) {
        Record record = recordMapper.selectById(id);
        System.out.println(record.toString());
        User user = getUserFromRemoteById(record.getUserId());
        record.setUser(user);
        return record;
    }

    @Override
    @GlobalTransactional
    public void submit(String status, Long userId) {
        Record record = new Record();
        record.setStatus(status);
        record.setUserId(userId);
        recordMapper.insert(record);

        String url = "http://service-user/addcnt?userId="+userId;
        restTemplate.getForObject(url, String.class);
    }

    //兜底回调
    public Record getRecordByIdFallback(Long id, BlockException e) {
        Record record = new Record();
        record.setId(id);
        record.setStatus("AC");
        return record;
    }

    public User getUserFromRemoteById(Long id){
        String url = "http://service-user/user/"+id;

        log.info("远程调用：{}", url);
        User user = restTemplate.getForObject(url, User.class);

        return user;
    }

    public Problem getProblemFromRemoteById(Long id){
        Problem problem = ProblemfeignClient.getProblemById(id);

        return problem;
    }
}
