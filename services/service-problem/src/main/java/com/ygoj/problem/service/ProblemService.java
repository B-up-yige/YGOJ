package com.ygoj.problem.service;

import com.ygoj.problem.pojo.Probleminfo;

public interface ProblemService {
    Probleminfo getProbleminfoById(Long id);

    void delProbleminfo(Long id);

    void addProbleminfo(Probleminfo probleminfo);

    void editProbleminfo(Probleminfo probleminfo);
}
