package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.problem.Contest;
import com.ygoj.problem.ContestProblem;
import com.ygoj.problem.service.ContestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/contest")
public class ContestController {
    
    @Autowired
    private ContestService contestService;

    /**
     * 获取比赛列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Long page,
                       @RequestParam(defaultValue = "10") Long pageSize) {
        try {
            log.info("获取比赛列表请求, page: {}, pageSize: {}", page, pageSize);
            List<Contest> contests = contestService.list(page, pageSize);
            return Result.success(contests);
        } catch (Exception e) {
            log.error("获取比赛列表失败", e);
            return Result.error(500, "获取比赛列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取比赛详情
     */
    @GetMapping("/{id}")
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
     * 创建比赛
     */
    @PostMapping("/add")
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
     * 编辑比赛
     */
    @PutMapping("/edit")
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
     * 删除比赛
     */
    @DeleteMapping("/del/{id}")
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
     * 添加比赛题目
     */
    @PostMapping("/problem/add")
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
     * 删除比赛题目
     */
    @DeleteMapping("/problem/del")
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
     * 获取比赛的题目列表
     */
    @GetMapping("/{id}/problems")
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
}
