package com.ygoj.problem.service;

import com.ygoj.problem.Probleminfo;

import java.util.List;

public interface ProblemService {
    Probleminfo getProbleminfoById(Long id);

    void delProbleminfo(Long id);

    void addProbleminfo(Probleminfo probleminfo);

    void editProbleminfo(Probleminfo probleminfo);

    List<Probleminfo> list(Long page, Long pageSize);
}
