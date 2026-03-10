package com.ygoj.problem.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ygoj.common.Result;
import com.ygoj.problem.feign.UserFeignClient;
import com.ygoj.problem.mapper.ProbleminfoMapper;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.service.ProblemService;
import com.ygoj.user.Userinfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {
    @Autowired
    ProbleminfoMapper probleminfoMapper;
    @Autowired
    UserFeignClient userFeignClient;
//    @Autowired
//    DiscoveryClient discoveryClient;
//    @Autowired
//    RestTemplate restTemplate;
//
//    @Override
//    public Problem getProblemById(Long id) {
//        Problem problem = new Problem();
//        problem.setId(id);
//        problem.setTitle("loop");
//        problem.setDescription("loop");
//
//        User user = getUserFromRemoteById(id);
//        problem.setCreator(user);
//        return problem;
//    }
//
//    public User getUserFromRemoteById(Long id){
//        List<ServiceInstance> instances = discoveryClient.getInstances("service-user");
//        ServiceInstance serviceInstance = instances.get(0);
//        String url = serviceInstance.getUri().toString()+"/user/"+id;
//
//        log.info("远程调用：{}", url);
//        User user = restTemplate.getForObject(url, User.class);
//
//        return user;
//    }

    @Override
    public Probleminfo getProbleminfoById(Long id) {
        Probleminfo probleminfo = probleminfoMapper.selectById(id);

        if(probleminfo != null){
            Result result = userFeignClient.userinfo(probleminfo.getAuthorId());
            Userinfo author = JSON.parseObject(JSON.toJSONString(result.getData()), Userinfo.class);
            probleminfo.setAuthor(author);
        }

        return probleminfo;
    }

    @Override
    @GlobalTransactional
    public void delProbleminfo(Long id) {
        probleminfoMapper.deleteById(id);
    }

    @Override
    @GlobalTransactional
    public void addProbleminfo(Probleminfo probleminfo) {
        probleminfoMapper.insert(probleminfo);
    }

    @Override
    @GlobalTransactional
    public void editProbleminfo(Probleminfo probleminfo) {
        probleminfoMapper.updateById(probleminfo);
    }
}
