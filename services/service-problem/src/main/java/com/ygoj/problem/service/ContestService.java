package com.ygoj.problem.service;

import com.ygoj.problem.Contest;
import com.ygoj.problem.ContestProblem;

import java.util.List;

public interface ContestService {
    /**
     * 获取比赛列表
     */
    List<Contest> list(Long page, Long pageSize);

    /**
     * 根据ID获取比赛详情
     */
    Contest getContestById(Long id);

    /**
     * 创建比赛
     */
    void addContest(Contest contest);

    /**
     * 编辑比赛
     */
    void editContest(Contest contest);

    /**
     * 删除比赛
     */
    void delContest(Long id);

    /**
     * 添加比赛题目
     */
    void addContestProblem(ContestProblem contestProblem);

    /**
     * 删除比赛题目
     */
    void delContestProblem(Long contestId, Long problemId);

    /**
     * 获取比赛的题目列表
     */
    List<ContestProblem> getContestProblems(Long contestId);
}
