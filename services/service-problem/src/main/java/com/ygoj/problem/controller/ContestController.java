package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.common.filter.Permission;
import com.ygoj.problem.Contest;
import com.ygoj.problem.ContestProblem;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.service.ContestService;
import com.ygoj.problem.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/contest")
public class ContestController {
    
    @Autowired
    private ContestService contestService;
    
    @Autowired
    private ProblemService problemService;

    /**
     * 获取比赛列表（公开访问）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Long page,
                       @RequestParam(defaultValue = "10") Long pageSize,
                       @RequestParam(required = false) String title) {
        try {
            log.info("获取比赛列表请求, page: {}, pageSize: {}, title: {}", page, pageSize, title);
            List<Contest> contests = contestService.list(page, pageSize, title);
            return Result.success(contests);
        } catch (Exception e) {
            log.error("获取比赛列表失败", e);
            return Result.error(500, "获取比赛列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取比赛详情（公开访问）
     */
    @GetMapping("/{id}")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "14",
        message = "您没有查看比赛的权限"
    )
    public Result getContestById(@PathVariable Long id) {
        try {
            log.info("获取比赛详情请求, contestId: {}", id);
            Contest contest = contestService.getContestById(id);
            if (contest == null) {
                return Result.error(404, "比赛不存在");
            }
            return Result.success(contest);
        } catch (Exception e) {
            log.error("获取比赛详情失败, contestId: {}", id, e);
            return Result.error(500, "获取比赛详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建比赛（需要创建比赛权限）
     */
    @PostMapping("/add")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "9",
        message = "您没有创建比赛的权限"
    )
    public Result addContest(@RequestBody Contest contest) {
        try {
            log.info("创建比赛请求, title: {}", contest.getTitle());
            
            if (contest.getTitle() == null || contest.getTitle().trim().isEmpty()) {
                return Result.error(400, "比赛标题不能为空");
            }
            if (contest.getStartTime() == null || contest.getEndTime() == null) {
                return Result.error(400, "比赛开始时间和结束时间不能为空");
            }
            if (contest.getAuthorId() == null) {
                return Result.error(400, "作者ID不能为空");
            }
            
            contestService.addContest(contest);
            log.info("创建比赛成功, contestId: {}", contest.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("创建比赛失败", e);
            return Result.error(500, "创建比赛失败: " + e.getMessage());
        }
    }

    /**
     * 编辑比赛（需要管理比赛权限）
     */
    @PutMapping("/edit")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "10",
        message = "您没有管理比赛的权限"
    )
    public Result editContest(@RequestBody Contest contest) {
        try {
            log.info("编辑比赛请求, contestId: {}", contest.getId());
            
            if (contest.getId() == null) {
                return Result.error(400, "比赛ID不能为空");
            }
            
            contestService.editContest(contest);
            log.info("编辑比赛成功, contestId: {}", contest.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("编辑比赛失败, contestId: {}", contest.getId(), e);
            return Result.error(500, "编辑比赛失败: " + e.getMessage());
        }
    }

    /**
     * 删除比赛（需要管理比赛权限）
     */
    @DeleteMapping("/del/{id}")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "10",
        message = "您没有管理比赛的权限"
    )
    public Result delContest(@PathVariable Long id) {
        try {
            log.info("删除比赛请求, contestId: {}", id);
            contestService.delContest(id);
            log.info("删除比赛成功, contestId: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除比赛失败, contestId: {}", id, e);
            return Result.error(500, "删除比赛失败: " + e.getMessage());
        }
    }

    /**
     * 添加比赛题目（需要管理比赛权限）
     */
    @PostMapping("/problem/add")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "10",
        message = "您没有管理比赛的权限"
    )
    public Result addContestProblem(@RequestBody ContestProblem contestProblem) {
        try {
            log.info("添加比赛题目请求, contestId: {}, problemId: {}", 
                    contestProblem.getContestId(), contestProblem.getProblemId());
            
            if (contestProblem.getContestId() == null || contestProblem.getProblemId() == null) {
                return Result.error(400, "比赛ID和题目ID不能为空");
            }
            
            contestService.addContestProblem(contestProblem);
            log.info("添加比赛题目成功");
            return Result.success();
        } catch (Exception e) {
            log.error("添加比赛题目失败", e);
            return Result.error(500, "添加比赛题目失败: " + e.getMessage());
        }
    }

    /**
     * 删除比赛题目（需要管理比赛权限）
     */
    @DeleteMapping("/problem/del")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "10",
        message = "您没有管理比赛的权限"
    )
    public Result delContestProblem(@RequestParam Long contestId,
                                     @RequestParam Long problemId) {
        try {
            log.info("删除比赛题目请求, contestId: {}, problemId: {}", contestId, problemId);
            contestService.delContestProblem(contestId, problemId);
            log.info("删除比赛题目成功");
            return Result.success();
        } catch (Exception e) {
            log.error("删除比赛题目失败", e);
            return Result.error(500, "删除比赛题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取比赛的题目列表（需要参加比赛权限 + 时间验证）
     */
    @GetMapping("/{id}/problems")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "11",
        message = "您没有参加该比赛的权限"
    )
    public Result getContestProblems(@PathVariable Long id) {
        try {
            log.info("获取比赛题目列表请求, contestId: {}", id);
            List<ContestProblem> problems = contestService.getContestProblems(id);
            return Result.success(problems);
        } catch (Exception e) {
            log.error("获取比赛题目列表失败, contestId: {}", id, e);
            return Result.error(500, "获取比赛题目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取比赛中的题目详情（带时间验证，需要参加比赛权限）
     */
    @GetMapping("/{contestId}/problem/{problemId}")
    @Permission(
        type = Permission.PermissionType.BIT,
        value = "11",
        message = "您没有参加该比赛的权限"
    )
    public Result getContestProblemDetail(@PathVariable Long contestId, @PathVariable Long problemId) {
        try {
            log.info("获取比赛题目详情请求, contestId: {}, problemId: {}", contestId, problemId);
            
            // 1. 验证比赛是否存在
            Contest contest = contestService.getContestById(contestId);
            if (contest == null) {
                return Result.error(404, "比赛不存在");
            }
            
            // 2. 验证比赛时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = contest.getStartTime();
            LocalDateTime endTime = contest.getEndTime();
            
            if (now.isBefore(startTime)) {
                log.warn("比赛尚未开始, contestId: {}, startTime: {}", contestId, startTime);
                return Result.error(403, "比赛尚未开始，开始时间: " + startTime);
            }
            
            if (now.isAfter(endTime)) {
                log.warn("比赛已结束, contestId: {}, endTime: {}", contestId, endTime);
                return Result.error(403, "比赛已结束，结束时间: " + endTime);
            }
            
            // 3. 验证题目是否属于该比赛
            List<ContestProblem> contestProblems = contestService.getContestProblems(contestId);
            boolean problemInContest = contestProblems.stream()
                .anyMatch(cp -> cp.getProblemId().equals(problemId));
            
            if (!problemInContest) {
                return Result.error(404, "该题目不属于此比赛");
            }
            
            // 4. 获取题目详情
            Probleminfo problem = problemService.getProbleminfoById(problemId);
            if (problem == null) {
                return Result.error(404, "题目不存在");
            }
            
            log.info("获取比赛题目详情成功, contestId: {}, problemId: {}", contestId, problemId);
            return Result.success(problem);
        } catch (Exception e) {
            log.error("获取比赛题目详情失败, contestId: {}, problemId: {}", contestId, problemId, e);
            return Result.error(500, "获取题目详情失败: " + e.getMessage());
        }
    }
}
