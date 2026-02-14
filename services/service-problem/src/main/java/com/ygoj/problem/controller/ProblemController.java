package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.problem.pojo.Probleminfo;
import com.ygoj.problem.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProblemController {
    @Autowired
    private ProblemService problemService;
//
//    @GetMapping("/problem/{id}")
//    public Problem getProblemById(@PathVariable("id") Long id,
//                                  HttpServletRequest request) {
//        Problem problem = problemService.getProblemById(id);
//        System.out.println(request.getHeader("X-Token"));
//
////        try {
////            TimeUnit.SECONDS.sleep(2);
////        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
////        }
//
//        return problem;
//    }

    @GetMapping("/probleminfo/{id}")
    public Result getProblemInfo(@PathVariable("id") Long id) {
        Probleminfo probleminfo = problemService.getProbleminfoById(id);
        return Result.success(probleminfo);
    }

    @PostMapping("/add")
    public Result addProblem(Probleminfo probleminfo) {
        problemService.addProbleminfo(probleminfo);
        return Result.success();
    }

    @PutMapping("/edit")
    public Result editProblem(Probleminfo probleminfo) {
        problemService.editProbleminfo(probleminfo);
        return Result.success();
    }

    @DeleteMapping("/del/{id}")
    public Result delProblemInfo(@PathVariable("id") Long id) {
        problemService.delProbleminfo(id);
        return Result.success();
    }
}
