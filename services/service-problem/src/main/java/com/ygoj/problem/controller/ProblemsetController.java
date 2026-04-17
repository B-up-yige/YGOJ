package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.common.security.CustomUserDetails;
import com.ygoj.problem.Problemset;
import com.ygoj.problem.ProblemsetProblem;
import com.ygoj.problem.service.ProblemsetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/problemset")
public class ProblemsetController {
    
    @Autowired
    private ProblemsetService problemsetService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        return null;
    }

    /**
     * 获取题集列表(公开访问，但会返回当前用户创建的私有题集)
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Long page,
                       @RequestParam(defaultValue = "10") Long pageSize,
                       @RequestParam(required = false) String title) {
        try {
            Long userId = getCurrentUserId();
            log.info("获取题集列表请求, page: {}, pageSize: {}, title: {}, userId: {}", page, pageSize, title, userId);
            List<Problemset> problemsets = problemsetService.list(page, pageSize, title, userId);
            return Result.success(problemsets);
        } catch (Exception e) {
            log.error("获取题集列表失败", e);
            return Result.error(500, "获取题集列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取题集详情(需要权限验证：公开题集或创建者本人)
     */
    @GetMapping("/{id}")
    public Result getProblemsetById(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            log.info("获取题集详情请求, problemsetId: {}, userId: {}", id, userId);
            Problemset problemset = problemsetService.getProblemsetById(id, userId);
            if (problemset == null) {
                return Result.error(404, "题集不存在或无权访问");
            }
            return Result.success(problemset);
        } catch (SecurityException e) {
            log.warn("无权访问题集, problemsetId: {}, userId: {}", id, getCurrentUserId());
            return Result.error(403, "无权访问该题集");
        } catch (Exception e) {
            log.error("获取题集详情失败, problemsetId: {}", id, e);
            return Result.error(500, "获取题集详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建题集(需要登录)
     */
    @PreAuthorize("isAuthenticated()")
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
     * 编辑题集(需要管理题集权限或是创建者)
     */
    @PutMapping("/edit")
    public Result editProblemset(@RequestBody Problemset problemset) {
        try {
            Long userId = getCurrentUserId();
            log.info("编辑题集请求, problemsetId: {}, userId: {}", problemset.getId(), userId);
            
            if (problemset.getId() == null) {
                return Result.error(400, "题集ID不能为空");
            }
            
            // 验证权限：有管理权限或者是创建者
            Problemset existingProblemset = problemsetService.getProblemsetById(problemset.getId(), userId);
            if (existingProblemset == null) {
                return Result.error(404, "题集不存在或无权访问");
            }
            
            problemsetService.editProblemset(problemset);
            log.info("编辑题集成功, problemsetId: {}", problemset.getId());
            return Result.success();
        } catch (SecurityException e) {
            log.warn("无权编辑题集, problemsetId: {}, userId: {}", problemset.getId(), getCurrentUserId());
            return Result.error(403, "无权编辑该题集");
        } catch (Exception e) {
            log.error("编辑题集失败, problemsetId: {}", problemset.getId(), e);
            return Result.error(500, "编辑题集失败: " + e.getMessage());
        }
    }

    /**
     * 删除题集(需要管理题集权限或是创建者)
     */
    @DeleteMapping("/del/{id}")
    public Result delProblemset(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            log.info("删除题集请求, problemsetId: {}, userId: {}", id, userId);
            
            // 验证权限：有管理权限或者是创建者
            Problemset problemset = problemsetService.getProblemsetById(id, userId);
            if (problemset == null) {
                return Result.error(404, "题集不存在或无权访问");
            }
            
            problemsetService.delProblemset(id);
            log.info("删除题集成功, problemsetId: {}", id);
            return Result.success();
        } catch (SecurityException e) {
            log.warn("无权删除题集, problemsetId: {}, userId: {}", id, getCurrentUserId());
            return Result.error(403, "无权删除该题集");
        } catch (Exception e) {
            log.error("删除题集失败, problemsetId: {}", id, e);
            return Result.error(500, "删除题集失败: " + e.getMessage());
        }
    }

    /**
     * 添加题集题目(需要管理题集权限或是创建者)
     */
    @PostMapping("/problem/add")
    public Result addProblemsetProblem(@RequestBody ProblemsetProblem problemsetProblem) {
        try {
            Long userId = getCurrentUserId();
            log.info("添加题集题目请求, problemsetId: {}, problemId: {}, userId: {}", 
                    problemsetProblem.getProblemsetId(), problemsetProblem.getProblemId(), userId);
            
            if (problemsetProblem.getProblemsetId() == null || problemsetProblem.getProblemId() == null) {
                return Result.error(400, "题集ID和题目ID不能为空");
            }
            
            // 验证权限：有管理权限或者是创建者
            Problemset problemset = problemsetService.getProblemsetById(problemsetProblem.getProblemsetId(), userId);
            if (problemset == null) {
                return Result.error(404, "题集不存在或无权访问");
            }
            
            problemsetService.addProblemsetProblem(problemsetProblem);
            log.info("添加题集题目成功");
            return Result.success();
        } catch (SecurityException e) {
            log.warn("无权添加题目到题集, problemsetId: {}, userId: {}", problemsetProblem.getProblemsetId(), getCurrentUserId());
            return Result.error(403, "无权向该题集添加题目");
        } catch (Exception e) {
            log.error("添加题集题目失败", e);
            return Result.error(500, "添加题集题目失败: " + e.getMessage());
        }
    }

    /**
     * 删除题集题目(需要管理题集权限或是创建者)
     */
    @DeleteMapping("/problem/del")
    public Result delProblemsetProblem(@RequestParam Long problemsetId,
                                        @RequestParam Long problemId) {
        try {
            Long userId = getCurrentUserId();
            log.info("删除题集题目请求, problemsetId: {}, problemId: {}, userId: {}", problemsetId, problemId, userId);
            
            // 验证权限：有管理权限或者是创建者
            Problemset problemset = problemsetService.getProblemsetById(problemsetId, userId);
            if (problemset == null) {
                return Result.error(404, "题集不存在或无权访问");
            }
            
            problemsetService.delProblemsetProblem(problemsetId, problemId);
            log.info("删除题集题目成功");
            return Result.success();
        } catch (SecurityException e) {
            log.warn("无权删除题集题目, problemsetId: {}, userId: {}", problemsetId, getCurrentUserId());
            return Result.error(403, "无权从该题集删除题目");
        } catch (Exception e) {
            log.error("删除题集题目失败", e);
            return Result.error(500, "删除题集题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取题集的题目列表(需要登录)
     */
    @PreAuthorize("isAuthenticated()")
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
