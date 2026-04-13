package com.ygoj.problem.service;

import com.ygoj.problem.Problemset;
import com.ygoj.problem.ProblemsetProblem;

import java.util.List;

public interface ProblemsetService {
    /**
     * 获取题集列表
     */
    List<Problemset> list(Long page, Long pageSize);

    /**
     * 根据ID获取题集详情
     */
    Problemset getProblemsetById(Long id);

    /**
     * 创建题集
     */
    void addProblemset(Problemset problemset);

    /**
     * 编辑题集
     */
    void editProblemset(Problemset problemset);

    /**
     * 删除题集
     */
    void delProblemset(Long id);

    /**
     * 添加题集题目
     */
    void addProblemsetProblem(ProblemsetProblem problemsetProblem);

    /**
     * 删除题集题目
     */
    void delProblemsetProblem(Long problemsetId, Long problemId);

    /**
     * 获取题集的题目列表
     */
    List<ProblemsetProblem> getProblemsetProblems(Long problemsetId);
}
