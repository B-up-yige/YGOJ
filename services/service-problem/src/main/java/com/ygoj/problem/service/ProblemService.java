package com.ygoj.problem.service;

import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;

import java.util.List;

public interface ProblemService {
    Probleminfo getProbleminfoById(Long id);

    void delProbleminfo(Long id);

    void addProbleminfo(Probleminfo probleminfo);

    void editProbleminfo(Probleminfo probleminfo);

    List<Probleminfo> list(Long page, Long pageSize);

    void addTestCase(Testcase testcase);

    void delTestCase(Testcase testcase);

    List<Testcase> getTestCase(Long problemId);
}
