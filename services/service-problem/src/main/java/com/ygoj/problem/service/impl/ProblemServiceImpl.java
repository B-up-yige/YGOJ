package com.ygoj.problem.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.List;

@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {
    @Autowired
    ProbleminfoMapper probleminfoMapper;
    @Autowired
    UserFeignClient userFeignClient;

    /**
     * 根据题目id获取题目信息
     *
     * @param id 题目id
     * @return {@link Probleminfo}
     */
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

    /**
     * 删除题目
     *
     * @param id 题目id
     */
    @Override
    @GlobalTransactional
    public void delProbleminfo(Long id) {
        probleminfoMapper.deleteById(id);
    }

    /**
     * 新增题目
     *
     * @param probleminfo 题目信息
     */
    @Override
    @GlobalTransactional
    public void addProbleminfo(Probleminfo probleminfo) {
        probleminfoMapper.insert(probleminfo);
    }

    /**
     * 修改题目信息
     *
     * @param probleminfo 题目信息
     */
    @Override
    @GlobalTransactional
    public void editProbleminfo(Probleminfo probleminfo) {
        probleminfoMapper.updateById(probleminfo);
    }

    @Override
    public List<Probleminfo> list(Long page, Long pageSize) {
        Page<Probleminfo> probleminfoPage = new Page<>(page, pageSize);
        return probleminfoMapper.selectPage(probleminfoPage, null).getRecords();
    }
}
