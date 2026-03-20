package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;
import com.ygoj.problem.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProblemController {
    @Autowired
    private ProblemService problemService;

    /**
     * 获取题目信息
     *
     * @param id 题目id
     * @return {@link Result}
     */
    @GetMapping("/probleminfo/{id}")
    public Result getProblemInfo(@PathVariable("id") Long id) {
        Probleminfo probleminfo = problemService.getProbleminfoById(id);
        return Result.success(probleminfo);
    }

    /**
     * 添加题目
     *
     * @param probleminfo 题目信息
     * @return {@link Result}
     */
    @PostMapping("/add")
    public Result addProblem(@RequestBody Probleminfo probleminfo) {
        problemService.addProbleminfo(probleminfo);
        return Result.success();
    }

    /**
     * 编辑题目信息
     *
     * @param probleminfo 题目信息
     * @return {@link Result}
     */
    @PutMapping("/edit")
    public Result editProblem(@RequestBody Probleminfo probleminfo) {
        problemService.editProbleminfo(probleminfo);
        return Result.success();
    }

    /**
     * 删除题目
     *
     * @param id 题目id
     * @return {@link Result}
     */
    @DeleteMapping("/del/{id}")
    public Result delProblemInfo(@PathVariable("id") Long id) {
        problemService.delProbleminfo(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(Long page, Long pageSize){
        return Result.success(problemService.list(page, pageSize));
    }

    @PostMapping("/addTestCase")
    public Result addTestCase(@RequestBody Testcase testcase) {
        problemService.addTestCase(testcase);
        return Result.success();
    }

    @DeleteMapping("/delTestCase")
    public Result delTestCase(@RequestBody Testcase testcase) {
        problemService.delTestCase(testcase);
        return Result.success();
    }

    @GetMapping("/getTestCase")
    public Result getTestCase(Long problemId) {
        return Result.success(problemService.getTestCase(problemId));
    }
}
