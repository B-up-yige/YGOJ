package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.problem.Problemset;
import com.ygoj.problem.ProblemsetProblem;
import com.ygoj.problem.service.ProblemsetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/problemset")
public class ProblemsetController {
    
    @Autowired
    private ProblemsetService problemsetService;

    /**
     * 获取题集列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Long page,
                       @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            log.info("获取题集列表请求, page: {}, pageSize: {}", page, pageSize);
            List<Problemset> problemsets = problemsetService.list(page, pageSize);
            return Result.success(problemsets);
        } catch (Exception e) {
            log.error("获取题集列表失败", e);
            return Result.error(500, "获取题集列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取题集详情
     */
    @GetMapping("/{id}")
    public Result getProblemsetById(@PathVariable Long id) {
        try {
            log.info("获取题集详情请求, problemsetId: {}", id);
            Problemset problemset = problemsetService.getProblemsetById(id);
            if (problemset == null) {
                return Result.error(404, "题集不存在");
            }
            return Result.success(problemset);
        } catch (Exception e) {
            log.error("获取题集详情失败, problemsetId: {}", id, e);
            return Result.error(500, "获取题集详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建题集
     */
    @PostMapping("/add")
    public Result addProblemset(@RequestBody Problemset problemset) {
        try {
            log.info("创建题集请求, title: {}", problemset.getTitle());
            
            if (problemset.getTitle() == null || problemset.getTitle().trim().isEmpty()) {
                return Result.error(400, "题集标题不能为空");
            }
            if (problemset.getAuthorId() == null) {
                return Result.error(400, "作者ID不能为空");
            }
            
            problemsetService.addProblemset(problemset);
            log.info("创建题集成功, problemsetId: {}", problemset.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("创建题集失败", e);
            return Result.error(500, "创建题集失败: " + e.getMessage());
        }
    }

    /**
     * 编辑题集
     */
    @PutMapping("/edit")
    public Result editProblemset(@RequestBody Problemset problemset) {
        try {
            log.info("编辑题集请求, problemsetId: {}", problemset.getId());
            
            if (problemset.getId() == null) {
                return Result.error(400, "题集ID不能为空");
            }
            
            problemsetService.editProblemset(problemset);
            log.info("编辑题集成功, problemsetId: {}", problemset.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("编辑题集失败, problemsetId: {}", problemset.getId(), e);
            return Result.error(500, "编辑题集失败: " + e.getMessage());
        }
    }

    /**
     * 删除题集
     */
    @DeleteMapping("/del/{id}")
    public Result delProblemset(@PathVariable Long id) {
        try {
            log.info("删除题集请求, problemsetId: {}", id);
            problemsetService.delProblemset(id);
            log.info("删除题集成功, problemsetId: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除题集失败, problemsetId: {}", id, e);
            return Result.error(500, "删除题集失败: " + e.getMessage());
        }
    }

    /**
     * 添加题集题目
     */
    @PostMapping("/problem/add")
    public Result addProblemsetProblem(@RequestBody ProblemsetProblem problemsetProblem) {
        try {
            log.info("添加题集题目请求, problemsetId: {}, problemId: {}", 
                    problemsetProblem.getProblemsetId(), problemsetProblem.getProblemId());
            
            if (problemsetProblem.getProblemsetId() == null || problemsetProblem.getProblemId() == null) {
                return Result.error(400, "题集ID和题目ID不能为空");
            }
            
            problemsetService.addProblemsetProblem(problemsetProblem);
            log.info("添加题集题目成功");
            return Result.success();
        } catch (Exception e) {
            log.error("添加题集题目失败", e);
            return Result.error(500, "添加题集题目失败: " + e.getMessage());
        }
    }

    /**
     * 删除题集题目
     */
    @DeleteMapping("/problem/del")
    public Result delProblemsetProblem(@RequestParam Long problemsetId,
                                        @RequestParam Long problemId) {
        try {
            log.info("删除题集题目请求, problemsetId: {}, problemId: {}", problemsetId, problemId);
            problemsetService.delProblemsetProblem(problemsetId, problemId);
            log.info("删除题集题目成功");
            return Result.success();
        } catch (Exception e) {
            log.error("删除题集题目失败", e);
            return Result.error(500, "删除题集题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取题集的题目列表
     */
    @GetMapping("/{id}/problems")
    public Result getProblemsetProblems(@PathVariable Long id) {
        try {
            log.info("获取题集题目列表请求, problemsetId: {}", id);
            List<ProblemsetProblem> problems = problemsetService.getProblemsetProblems(id);
            return Result.success(problems);
        } catch (Exception e) {
            log.error("获取题集题目列表失败, problemsetId: {}", id, e);
            return Result.error(500, "获取题集题目列表失败: " + e.getMessage());
        }
    }
}
