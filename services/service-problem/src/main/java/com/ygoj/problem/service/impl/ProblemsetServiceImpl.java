package com.ygoj.problem.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.common.Result;
import com.ygoj.problem.Problemset;
import com.ygoj.problem.ProblemsetProblem;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.feign.UserFeignClient;
import com.ygoj.problem.mapper.ProblemsetMapper;
import com.ygoj.problem.mapper.ProblemsetProblemMapper;
import com.ygoj.problem.mapper.ProbleminfoMapper;
import com.ygoj.problem.service.ProblemsetService;
import com.ygoj.user.Userinfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ProblemsetServiceImpl implements ProblemsetService {
    
    @Autowired
    private ProblemsetMapper problemsetMapper;
    
    @Autowired
    private ProblemsetProblemMapper problemsetProblemMapper;
    
    @Autowired
    private ProbleminfoMapper probleminfoMapper;
    
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public List<Problemset> list(Long page, Long pageSize, String title, Long userId) {
        try {
            log.info("获取题集列表, page: {}, pageSize: {}, title: {}, userId: {}", page, pageSize, title, userId);
            Page<Problemset> problemsetPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Problemset> wrapper = new LambdaQueryWrapper<>();
            
            // 查询条件：公开的题集 OR (如果提供了userId) 用户自己创建的私有题集
            if (userId != null) {
                wrapper.or(w -> w.eq(Problemset::getIsPublic, true)
                        .or(w1 -> w1.eq(Problemset::getAuthorId, userId)));
            } else {
                // 没有提供userId，只返回公开题集
                wrapper.eq(Problemset::getIsPublic, true);
            }
            
            // 标题模糊搜索
            if (title != null && !title.trim().isEmpty()) {
                wrapper.like(Problemset::getTitle, title.trim());
            }
            
            wrapper.orderByDesc(Problemset::getCreatedAt);
            Page<Problemset> result = problemsetMapper.selectPage(problemsetPage, wrapper);
            
            // 填充作者信息
            for (Problemset problemset : result.getRecords()) {
                if (problemset.getAuthorId() != null) {
                    try {
                        Result userInfoResult = userFeignClient.userinfo(problemset.getAuthorId());
                        if (userInfoResult != null && userInfoResult.getData() != null) {
                            Userinfo author = JSON.parseObject(JSON.toJSONString(userInfoResult.getData()), Userinfo.class);
                            problemset.setAuthor(author);
                        }
                    } catch (Exception e) {
                        log.warn("获取题集作者信息失败, problemsetId: {}, authorId: {}", problemset.getId(), problemset.getAuthorId(), e);
                    }
                }
            }
            
            return result.getRecords();
        } catch (Exception e) {
            log.error("获取题集列表异常", e);
            throw new RuntimeException("获取题集列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Problemset getProblemsetById(Long id, Long userId) {
        try {
            log.info("获取题集详情, problemsetId: {}, userId: {}", id, userId);
            Problemset problemset = problemsetMapper.selectById(id);
            
            if (problemset == null) {
                return null;
            }
            
            // 权限验证：公开题集或创建者本人可以访问
            if (!problemset.getIsPublic()) {
                // 私有题集，需要验证是否是创建者
                if (userId == null || !userId.equals(problemset.getAuthorId())) {
                    log.warn("无权访问题集, problemsetId: {}, userId: {}, authorId: {}", id, userId, problemset.getAuthorId());
                    throw new SecurityException("无权访问该私有题集");
                }
            }
            
            // 填充作者信息
            if (problemset.getAuthorId() != null) {
                try {
                    Result userInfoResult = userFeignClient.userinfo(problemset.getAuthorId());
                    if (userInfoResult != null && userInfoResult.getData() != null) {
                        Userinfo author = JSON.parseObject(JSON.toJSONString(userInfoResult.getData()), Userinfo.class);
                        problemset.setAuthor(author);
                        log.debug("获取题集作者信息成功, problemsetId: {}, author: {}", id, author.getUsername());
                    }
                } catch (Exception e) {
                    log.warn("获取题集作者信息失败, problemsetId: {}, authorId: {}", problemset.getId(), problemset.getAuthorId(), e);
                }
            }
            
            return problemset;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取题集详情异常, problemsetId: {}", id, e);
            throw new RuntimeException("获取题集详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void addProblemset(Problemset problemset) {
        try {
            log.info("创建题集, title: {}", problemset.getTitle());
            problemset.setCreatedAt(LocalDateTime.now());
            problemset.setUpdatedAt(LocalDateTime.now());
            if (problemset.getIsPublic() == null) {
                problemset.setIsPublic(true);
            }
            problemsetMapper.insert(problemset);
            log.info("创建题集成功, problemsetId: {}", problemset.getId());
        } catch (Exception e) {
            log.error("创建题集异常, title: {}", problemset.getTitle(), e);
            throw new RuntimeException("创建题集失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void editProblemset(Problemset problemset) {
        try {
            log.info("编辑题集, problemsetId: {}", problemset.getId());
            problemset.setUpdatedAt(LocalDateTime.now());
            problemsetMapper.updateById(problemset);
            log.info("编辑题集成功, problemsetId: {}", problemset.getId());
        } catch (Exception e) {
            log.error("编辑题集异常, problemsetId: {}", problemset.getId(), e);
            throw new RuntimeException("编辑题集失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delProblemset(Long id) {
        try {
            log.info("删除题集, problemsetId: {}", id);
            // 先删除关联的题目
            LambdaQueryWrapper<ProblemsetProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProblemsetProblem::getProblemsetId, id);
            problemsetProblemMapper.delete(wrapper);
            // 再删除题集
            problemsetMapper.deleteById(id);
            log.info("删除题集成功, problemsetId: {}", id);
        } catch (Exception e) {
            log.error("删除题集异常, problemsetId: {}", id, e);
            throw new RuntimeException("删除题集失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void addProblemsetProblem(ProblemsetProblem problemsetProblem) {
        try {
            log.info("添加题集题目, problemsetId: {}, problemId: {}", 
                    problemsetProblem.getProblemsetId(), problemsetProblem.getProblemId());
            
            // 验证题目是否存在
            if (problemsetProblem.getProblemId() != null) {
                Probleminfo problem = probleminfoMapper.selectById(problemsetProblem.getProblemId());
                if (problem == null) {
                    log.warn("添加题集题目失败: 题目不存在, problemId: {}", problemsetProblem.getProblemId());
                    throw new IllegalArgumentException("题目不存在");
                }
            }
            
            problemsetProblem.setAddedAt(LocalDateTime.now());
            problemsetProblemMapper.insert(problemsetProblem);
            log.info("添加题集题目成功");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加题集题目异常", e);
            throw new RuntimeException("添加题集题目失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delProblemsetProblem(Long problemsetId, Long problemId) {
        try {
            log.info("删除题集题目, problemsetId: {}, problemId: {}", problemsetId, problemId);
            LambdaQueryWrapper<ProblemsetProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProblemsetProblem::getProblemsetId, problemsetId)
                   .eq(ProblemsetProblem::getProblemId, problemId);
            problemsetProblemMapper.delete(wrapper);
            log.info("删除题集题目成功");
        } catch (Exception e) {
            log.error("删除题集题目异常", e);
            throw new RuntimeException("删除题集题目失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProblemsetProblem> getProblemsetProblems(Long problemsetId) {
        try {
            log.info("获取题集题目列表, problemsetId: {}", problemsetId);
            LambdaQueryWrapper<ProblemsetProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProblemsetProblem::getProblemsetId, problemsetId)
                   .orderByDesc(ProblemsetProblem::getAddedAt);
            return problemsetProblemMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("获取题集题目列表异常, problemsetId: {}", problemsetId, e);
            throw new RuntimeException("获取题集题目列表失败: " + e.getMessage(), e);
        }
    }
}
