package com.ygoj.problem.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.common.Result;
import com.ygoj.problem.Tag;
import com.ygoj.problem.Testcase;
import com.ygoj.problem.feign.UserFeignClient;
import com.ygoj.problem.mapper.ProbleminfoMapper;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.mapper.TagMapper;
import com.ygoj.problem.mapper.TestcaseMapper;
import com.ygoj.problem.service.ProblemService;
import com.ygoj.user.Userinfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ProblemServiceImpl implements ProblemService {
    @Autowired
    private ProbleminfoMapper probleminfoMapper;
    @Autowired
    private TestcaseMapper testcaseMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private UserFeignClient userFeignClient;

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

    /**
     * 分页查询题目信息
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @return {@link List<Probleminfo>}
     */
    @Override
    public List<Probleminfo> list(Long page, Long pageSize) {
        Page<Probleminfo> probleminfoPage = new Page<>(page, pageSize);
        return probleminfoMapper.selectPage(probleminfoPage, null).getRecords();
    }

    /**
     * 为题目添加测试用例
     *
     * @param testcase testcase
     */
    @Override
    @GlobalTransactional
    public void addTestCase(Testcase testcase) {
        testcaseMapper.insert(testcase);
    }

    @Override
    @GlobalTransactional
    public void delTestCase(Testcase testcase) {
        testcaseMapper.deleteById(testcase);
    }

    @Override
    public List<Testcase> getTestCase(Long problemId) {
        LambdaQueryWrapper<Testcase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Testcase::getProblemId, problemId);

        return testcaseMapper.selectList(queryWrapper);
    }

    @Override
    public List<Tag> getTag(Long id) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getProblemId, id);

        return tagMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void addTag(Long id, String tagName) {
        Tag tag = new Tag();
        tag.setProblemId(id);
        tag.setTag(tagName);
        tagMapper.insert(tag);
    }

    @Override
    @Transactional
    public void delTag(Long id, String tagName) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getProblemId, id);
        queryWrapper.eq(Tag::getTag, tagName);
        tagMapper.delete(queryWrapper);
    }
}
