package com.ygoj.problem.service.impl;

import com.ygoj.problem.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {

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
}
