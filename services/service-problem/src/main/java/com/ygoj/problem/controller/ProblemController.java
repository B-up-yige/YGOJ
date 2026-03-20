package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Testcase;
import com.ygoj.problem.feign.FileSystemFeignClient;
import com.ygoj.problem.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ProblemController {
    @Autowired
    private ProblemService problemService;
    @Autowired
    FileSystemFeignClient fileSystemFeignClient;

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
    public Result addTestCase(@RequestParam Long problemId,
                              @RequestParam MultipartFile input, @RequestParam MultipartFile output) {

        System.out.println(input.getContentType());
        System.out.println(output.getContentType());

        //上传input和output文件
        String inputId = (String) fileSystemFeignClient.uploadFile(input).getData();
        String outputId = (String) fileSystemFeignClient.uploadFile(output).getData();

        Testcase testcase = new Testcase();
        testcase.setProblemId(problemId);
        testcase.setInputFileId(inputId);
        testcase.setOutputFileId(outputId);
        testcase.setInputFileName(input.getOriginalFilename());
        testcase.setOutputFileName(output.getOriginalFilename());

        problemService.addTestCase(testcase);
        return Result.success();
    }

    @DeleteMapping("/delTestCase")
    public Result delTestCase(@RequestBody Testcase testcase) {
        problemService.delTestCase(testcase);
        fileSystemFeignClient.deleteFile(testcase.getInputFileId());
        fileSystemFeignClient.deleteFile(testcase.getOutputFileId());
        return Result.success();
    }

    @GetMapping("/getTestCase")
    public Result getTestCase(@RequestParam Long problemId) {
        return Result.success(problemService.getTestCase(problemId));
    }
}
