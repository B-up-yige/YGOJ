package com.ygoj.problem.controller;

import com.ygoj.problem.pojo.Problem;
import com.ygoj.problem.service.ProblemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ProblemController {
//    @Autowired
//    private ProblemService problemService;
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
}
