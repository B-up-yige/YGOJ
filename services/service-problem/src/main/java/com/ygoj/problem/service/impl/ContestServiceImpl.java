package com.ygoj.problem.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.common.Result;
import com.ygoj.problem.Contest;
import com.ygoj.problem.ContestProblem;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.feign.UserFeignClient;
import com.ygoj.problem.mapper.ContestMapper;
import com.ygoj.problem.mapper.ContestProblemMapper;
import com.ygoj.problem.mapper.ProbleminfoMapper;
import com.ygoj.problem.service.ContestService;
import com.ygoj.user.Userinfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ContestServiceImpl implements ContestService {
    
    @Autowired
    private ContestMapper contestMapper;
    
    @Autowired
    private ContestProblemMapper contestProblemMapper;
    
    @Autowired
    private ProbleminfoMapper probleminfoMapper;
    
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public List<Contest> list(Long page, Long pageSize, String title) {
        try {
            log.info("获取比赛列表, page: {}, pageSize: {}, title: {}", page, pageSize, title);
            Page<Contest> contestPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
            
            // 标题模糊搜索
            if (title != null && !title.trim().isEmpty()) {
                wrapper.like(Contest::getTitle, title.trim());
            }
            
            wrapper.orderByDesc(Contest::getCreatedAt);
            Page<Contest> result = contestMapper.selectPage(contestPage, wrapper);
            
            // 更新比赛状态并填充作者信息
            for (Contest contest : result.getRecords()) {
                updateContestStatus(contest);
                
                // 填充作者信息
                if (contest.getAuthorId() != null) {
                    try {
                        Result userInfoResult = userFeignClient.userinfo(contest.getAuthorId());
                        if (userInfoResult != null && userInfoResult.getData() != null) {
                            Userinfo author = JSON.parseObject(JSON.toJSONString(userInfoResult.getData()), Userinfo.class);
                            contest.setAuthor(author);
                        }
                    } catch (Exception e) {
                        log.warn("获取比赛作者信息失败, contestId: {}, authorId: {}", contest.getId(), contest.getAuthorId(), e);
                    }
                }
            }
            
            return result.getRecords();
        } catch (Exception e) {
            log.error("获取比赛列表异常", e);
            throw new RuntimeException("获取比赛列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Contest getContestById(Long id) {
        try {
            log.info("获取比赛详情, contestId: {}", id);
            Contest contest = contestMapper.selectById(id);
            if (contest != null) {
                updateContestStatus(contest);
                contestMapper.updateById(contest);
                
                // 填充作者信息
                if (contest.getAuthorId() != null) {
                    try {
                        Result userInfoResult = userFeignClient.userinfo(contest.getAuthorId());
                        if (userInfoResult != null && userInfoResult.getData() != null) {
                            Userinfo author = JSON.parseObject(JSON.toJSONString(userInfoResult.getData()), Userinfo.class);
                            contest.setAuthor(author);
                            log.debug("获取比赛作者信息成功, contestId: {}, author: {}", id, author.getUsername());
                        }
                    } catch (Exception e) {
                        log.warn("获取比赛作者信息失败, contestId: {}, authorId: {}", contest.getId(), contest.getAuthorId(), e);
                    }
                }
            }
            return contest;
        } catch (Exception e) {
            log.error("获取比赛详情异常, contestId: {}", id, e);
            throw new RuntimeException("获取比赛详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void addContest(Contest contest) {
        try {
            log.info("创建比赛, title: {}", contest.getTitle());
            contest.setCreatedAt(LocalDateTime.now());
            contest.setUpdatedAt(LocalDateTime.now());
            contest.setStatus("UPCOMING");
            contestMapper.insert(contest);
            log.info("创建比赛成功, contestId: {}", contest.getId());
        } catch (Exception e) {
            log.error("创建比赛异常, title: {}", contest.getTitle(), e);
            throw new RuntimeException("创建比赛失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void editContest(Contest contest) {
        try {
            log.info("编辑比赛, contestId: {}", contest.getId());
            contest.setUpdatedAt(LocalDateTime.now());
            contestMapper.updateById(contest);
            log.info("编辑比赛成功, contestId: {}", contest.getId());
        } catch (Exception e) {
            log.error("编辑比赛异常, contestId: {}", contest.getId(), e);
            throw new RuntimeException("编辑比赛失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delContest(Long id) {
        try {
            log.info("删除比赛, contestId: {}", id);
            // 先删除关联的题目
            LambdaQueryWrapper<ContestProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContestProblem::getContestId, id);
            contestProblemMapper.delete(wrapper);
            // 再删除比赛
            contestMapper.deleteById(id);
            log.info("删除比赛成功, contestId: {}", id);
        } catch (Exception e) {
            log.error("删除比赛异常, contestId: {}", id, e);
            throw new RuntimeException("删除比赛失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void addContestProblem(ContestProblem contestProblem) {
        try {
            log.info("添加比赛题目, contestId: {}, problemId: {}", 
                    contestProblem.getContestId(), contestProblem.getProblemId());
            
            // 验证题目是否存在
            if (contestProblem.getProblemId() != null) {
                Probleminfo problem = probleminfoMapper.selectById(contestProblem.getProblemId());
                if (problem == null) {
                    log.warn("添加比赛题目失败: 题目不存在, problemId: {}", contestProblem.getProblemId());
                    throw new IllegalArgumentException("题目不存在");
                }
            }
            
            // 自动设置 problemOrder 为当前最大值 + 1
            LambdaQueryWrapper<ContestProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContestProblem::getContestId, contestProblem.getContestId())
                   .orderByDesc(ContestProblem::getProblemOrder)
                   .last("LIMIT 1");
            ContestProblem lastProblem = contestProblemMapper.selectOne(wrapper);
            
            int nextOrder = 1;
            if (lastProblem != null && lastProblem.getProblemOrder() != null) {
                nextOrder = lastProblem.getProblemOrder() + 1;
            }
            contestProblem.setProblemOrder(nextOrder);
            
            contestProblemMapper.insert(contestProblem);
            log.info("添加比赛题目成功, problemOrder: {}", nextOrder);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加比赛题目异常", e);
            throw new RuntimeException("添加比赛题目失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delContestProblem(Long contestId, Long problemId) {
        try {
            log.info("删除比赛题目, contestId: {}, problemId: {}", contestId, problemId);
            LambdaQueryWrapper<ContestProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContestProblem::getContestId, contestId)
                   .eq(ContestProblem::getProblemId, problemId);
            contestProblemMapper.delete(wrapper);
            log.info("删除比赛题目成功");
        } catch (Exception e) {
            log.error("删除比赛题目异常", e);
            throw new RuntimeException("删除比赛题目失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ContestProblem> getContestProblems(Long contestId) {
        try {
            log.info("获取比赛题目列表, contestId: {}", contestId);
            LambdaQueryWrapper<ContestProblem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContestProblem::getContestId, contestId)
                   .orderByAsc(ContestProblem::getProblemOrder);
            return contestProblemMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("获取比赛题目列表异常, contestId: {}", contestId, e);
            throw new RuntimeException("获取比赛题目列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新比赛状态
     */
    private void updateContestStatus(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime())) {
            contest.setStatus("UPCOMING");
        } else if (now.isAfter(contest.getEndTime())) {
            contest.setStatus("ENDED");
        } else {
            contest.setStatus("RUNNING");
        }
    }
}
