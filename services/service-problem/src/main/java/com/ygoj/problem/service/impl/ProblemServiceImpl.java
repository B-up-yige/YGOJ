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
        try {
            log.debug("获取题目信息, problemId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            
            Probleminfo probleminfo = probleminfoMapper.selectById(id);

            if(probleminfo != null){
                Result result = userFeignClient.userinfo(probleminfo.getAuthorId());
                if (result != null && result.getData() != null) {
                    Userinfo author = JSON.parseObject(JSON.toJSONString(result.getData()), Userinfo.class);
                    probleminfo.setAuthor(author);
                    log.debug("获取题目信息成功, problemId: {}, author: {}", id, author.getUsername());
                }
            }

            return probleminfo;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取题目信息异常, problemId: {}", id, e);
            throw new RuntimeException("获取题目信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除题目
     *
     * @param id 题目id
     */
    @Override
    @GlobalTransactional
    public void delProbleminfo(Long id) {
        try {
            log.info("删除题目, problemId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            
            probleminfoMapper.deleteById(id);
            log.info("删除题目成功, problemId: {}", id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除题目异常, problemId: {}", id, e);
            throw new RuntimeException("删除题目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 新增题目
     *
     * @param probleminfo 题目信息
     */
    @Override
    @GlobalTransactional
    public void addProbleminfo(Probleminfo probleminfo) {
        try {
            log.info("添加题目, title: {}", probleminfo.getTitle());
            
            if (probleminfo == null) {
                throw new IllegalArgumentException("题目信息不能为空");
            }
            
            probleminfoMapper.insert(probleminfo);
            log.info("添加题目成功, problemId: {}", probleminfo.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加题目异常, title: {}", probleminfo.getTitle(), e);
            throw new RuntimeException("添加题目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 修改题目信息
     *
     * @param probleminfo 题目信息
     */
    @Override
    @GlobalTransactional
    public void editProbleminfo(Probleminfo probleminfo) {
        try {
            log.info("编辑题目, problemId: {}", probleminfo.getId());
            
            if (probleminfo == null || probleminfo.getId() == null) {
                throw new IllegalArgumentException("题目信息或ID不能为空");
            }
            
            probleminfoMapper.updateById(probleminfo);
            log.info("编辑题目成功, problemId: {}", probleminfo.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑题目异常, problemId: {}", probleminfo.getId(), e);
            throw new RuntimeException("编辑题目失败: " + e.getMessage(), e);
        }
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
        try {
            log.debug("分页查询题目, page: {}, pageSize: {}", page, pageSize);
            
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10L;
            }
            
            Page<Probleminfo> probleminfoPage = new Page<>(page, pageSize);
            List<Probleminfo> records = probleminfoMapper.selectPage(probleminfoPage, null).getRecords();
            
            // 填充作者信息
            for (Probleminfo problem : records) {
                if (problem.getAuthorId() != null) {
                    try {
                        Result result = userFeignClient.userinfo(problem.getAuthorId());
                        if (result != null && result.getData() != null) {
                            Userinfo author = JSON.parseObject(JSON.toJSONString(result.getData()), Userinfo.class);
                            problem.setAuthor(author);
                        }
                    } catch (Exception e) {
                        log.warn("获取题目作者信息失败, problemId: {}, authorId: {}", problem.getId(), problem.getAuthorId(), e);
                    }
                }
            }
            
            return records;
        } catch (Exception e) {
            log.error("分页查询题目异常", e);
            throw new RuntimeException("分页查询题目失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为题目添加测试用例
     *
     * @param testcase testcase
     */
    @Override
    @GlobalTransactional
    public void addTestCase(Testcase testcase) {
        try {
            log.info("添加测试用例, problemId: {}", testcase.getProblemId());
            
            if (testcase == null) {
                throw new IllegalArgumentException("测试用例不能为空");
            }
            if (testcase.getProblemId() == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            
            testcaseMapper.insert(testcase);
            log.info("添加测试用例成功, testcaseId: {}", testcase.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加测试用例异常, problemId: {}", testcase.getProblemId(), e);
            throw new RuntimeException("添加测试用例失败: " + e.getMessage(), e);
        }
    }

    @Override
    @GlobalTransactional
    public void delTestCase(Testcase testcase) {
        try {
            log.info("删除测试用例, testcaseId: {}", testcase.getId());
            
            if (testcase == null || testcase.getId() == null) {
                throw new IllegalArgumentException("测试用例或ID不能为空");
            }
            
            testcaseMapper.deleteById(testcase);
            log.info("删除测试用例成功, testcaseId: {}", testcase.getId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除测试用例异常, testcaseId: {}", testcase.getId(), e);
            throw new RuntimeException("删除测试用例失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Testcase> getTestCase(Long problemId) {
        try {
            log.debug("获取测试用例列表, problemId: {}", problemId);
            
            if (problemId == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            
            LambdaQueryWrapper<Testcase> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Testcase::getProblemId, problemId);

            return testcaseMapper.selectList(queryWrapper);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取测试用例列表异常, problemId: {}", problemId, e);
            throw new RuntimeException("获取测试用例列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Tag> getTag(Long id) {
        try {
            log.debug("获取题目标签, problemId: {}", id);
            
            if (id == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            
            LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Tag::getProblemId, id);

            return tagMapper.selectList(queryWrapper);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取题目标签异常, problemId: {}", id, e);
            throw new RuntimeException("获取题目标签失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void addTag(Long id, String tagName) {
        try {
            log.info("添加标签, problemId: {}, tag: {}", id, tagName);
            
            if (id == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            if (tagName == null || tagName.trim().isEmpty()) {
                throw new IllegalArgumentException("标签名称不能为空");
            }
            
            Tag tag = new Tag();
            tag.setProblemId(id);
            tag.setTag(tagName);
            tagMapper.insert(tag);
            log.info("添加标签成功, problemId: {}, tag: {}", id, tagName);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加标签异常, problemId: {}, tag: {}", id, tagName, e);
            throw new RuntimeException("添加标签失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delTag(Long id, String tagName) {
        try {
            log.info("删除标签, problemId: {}, tag: {}", id, tagName);
            
            if (id == null) {
                throw new IllegalArgumentException("题目ID不能为空");
            }
            if (tagName == null || tagName.trim().isEmpty()) {
                throw new IllegalArgumentException("标签名称不能为空");
            }
            
            LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Tag::getProblemId, id);
            queryWrapper.eq(Tag::getTag, tagName);
            tagMapper.delete(queryWrapper);
            log.info("删除标签成功, problemId: {}, tag: {}", id, tagName);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除标签异常, problemId: {}, tag: {}", id, tagName, e);
            throw new RuntimeException("删除标签失败: " + e.getMessage(), e);
        }
    }
}
