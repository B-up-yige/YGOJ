package com.ygoj.problem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.problem.Problemset;
import com.ygoj.problem.ProblemsetProblem;
import com.ygoj.problem.mapper.ProblemsetMapper;
import com.ygoj.problem.mapper.ProblemsetProblemMapper;
import com.ygoj.problem.service.ProblemsetService;
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

    @Override
    public List<Problemset> list(Long page, Long pageSize) {
        try {
            log.info("获取题集列表, page: {}, pageSize: {}", page, pageSize);
            Page<Problemset> problemsetPage = new Page<>(page, pageSize);
            LambdaQueryWrapper<Problemset> wrapper = new LambdaQueryWrapper<>();
            // 只返回公开的题集
            wrapper.eq(Problemset::getIsPublic, true);
            wrapper.orderByDesc(Problemset::getCreatedAt);
            Page<Problemset> result = problemsetMapper.selectPage(problemsetPage, wrapper);
            return result.getRecords();
        } catch (Exception e) {
            log.error("获取题集列表异常", e);
            throw new RuntimeException("获取题集列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Problemset getProblemsetById(Long id) {
        try {
            log.info("获取题集详情, problemsetId: {}", id);
            return problemsetMapper.selectById(id);
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
            problemsetProblem.setAddedAt(LocalDateTime.now());
            problemsetProblemMapper.insert(problemsetProblem);
            log.info("添加题集题目成功");
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
