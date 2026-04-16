package com.ygoj.problem.service;

import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Tag;
import com.ygoj.problem.Testcase;

import java.util.List;

public interface ProblemService {
    Probleminfo getProbleminfoById(Long id);

    void delProbleminfo(Long id);

    void addProbleminfo(Probleminfo probleminfo);

    void editProbleminfo(Probleminfo probleminfo);

    List<Probleminfo> list(Long page, Long pageSize, String title, String tag);

    void addTestCase(Testcase testcase);

    void delTestCase(Testcase testcase);

    List<Testcase> getTestCase(Long problemId);

    List<Tag> getTag(Long id);

    void addTag(Long id, String tagName);

    void delTag(Long id, String tagName);
}
