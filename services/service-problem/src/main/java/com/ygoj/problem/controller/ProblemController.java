package com.ygoj.problem.controller;

import com.ygoj.common.Result;
import com.ygoj.problem.Probleminfo;
import com.ygoj.problem.Tag;
import com.ygoj.problem.Testcase;
import com.ygoj.problem.feign.FileSystemFeignClient;
import com.ygoj.problem.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
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
        try {
            log.info("获取题目信息请求, problemId: {}", id);
            Probleminfo probleminfo = problemService.getProbleminfoById(id);
            if (probleminfo == null) {
                log.warn("题目不存在, problemId: {}", id);
                return Result.error(404, "题目不存在");
            }
            log.info("获取题目信息成功, problemId: {}", id);
            return Result.success(probleminfo);
        } catch (Exception e) {
            log.error("获取题目信息失败, problemId: {}", id, e);
            return Result.error(500, "获取题目信息失败: " + e.getMessage());
        }
    }

    /**
     * 添加题目
     *
     * @param probleminfo 题目信息
     * @return {@link Result}
     */
    @PostMapping("/add")
    public Result addProblem(@RequestBody Probleminfo probleminfo) {
        try {
            log.info("添加题目请求, title: {}, authorId: {}", probleminfo.getTitle(), probleminfo.getAuthorId());
            
            // 参数校验
            if (probleminfo.getTitle() == null || probleminfo.getTitle().trim().isEmpty()) {
                return Result.error(400, "题目标题不能为空");
            }
            if (probleminfo.getAuthorId() == null) {
                return Result.error(400, "作者ID不能为空");
            }
            
            problemService.addProbleminfo(probleminfo);
            log.info("添加题目成功, problemId: {}", probleminfo.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("添加题目失败", e);
            return Result.error(500, "添加题目失败: " + e.getMessage());
        }
    }

    /**
     * 编辑题目信息
     *
     * @param probleminfo 题目信息
     * @return {@link Result}
     */
    @PutMapping("/edit")
    public Result editProblem(@RequestBody Probleminfo probleminfo) {
        try {
            log.info("编辑题目请求, problemId: {}", probleminfo.getId());
            
            if (probleminfo.getId() == null) {
                return Result.error(400, "题目ID不能为空");
            }
            
            problemService.editProbleminfo(probleminfo);
            log.info("编辑题目成功, problemId: {}", probleminfo.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("编辑题目失败, problemId: {}", probleminfo.getId(), e);
            return Result.error(500, "编辑题目失败: " + e.getMessage());
        }
    }

    /**
     * 删除题目
     *
     * @param id 题目id
     * @return {@link Result}
     */
    @DeleteMapping("/del/{id}")
    public Result delProblemInfo(@PathVariable("id") Long id) {
        try {
            log.info("删除题目请求, problemId: {}", id);
            problemService.delProbleminfo(id);
            log.info("删除题目成功, problemId: {}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除题目失败, problemId: {}", id, e);
            return Result.error(500, "删除题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result list(Long page, Long pageSize){
        try {
            log.debug("获取题目列表请求, page: {}, pageSize: {}", page, pageSize);
            
            // 参数校验和默认值设置
            if (page == null || page < 1) {
                page = 1L;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10L;
            }
            
            return Result.success(problemService.list(page, pageSize));
        } catch (Exception e) {
            log.error("获取题目列表失败", e);
            return Result.error(500, "获取题目列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/addTestCase")
    public Result addTestCase(@RequestParam Long problemId,
                              @RequestParam MultipartFile input, @RequestParam MultipartFile output) {
        try {
            log.info("添加测试用例请求, problemId: {}, input: {}, output: {}", 
                    problemId, input.getOriginalFilename(), output.getOriginalFilename());

            // 参数校验
            if (problemId == null) {
                return Result.error(400, "题目ID不能为空");
            }
            if (input == null || input.isEmpty()) {
                return Result.error(400, "输入文件不能为空");
            }
            if (output == null || output.isEmpty()) {
                return Result.error(400, "输出文件不能为空");
            }

            System.out.println(input.getContentType());
            System.out.println(output.getContentType());

            //上传input和output文件
            Result inputResult = fileSystemFeignClient.uploadFile(input);
            if (inputResult.getCode() != 200) {
                return Result.error(500, "上传输入文件失败: " + inputResult.getMsg());
            }
            String inputId = (String) inputResult.getData();
            
            Result outputResult = fileSystemFeignClient.uploadFile(output);
            if (outputResult.getCode() != 200) {
                // 清理已上传的输入文件
                fileSystemFeignClient.deleteFile(inputId);
                return Result.error(500, "上传输出文件失败: " + outputResult.getMsg());
            }
            String outputId = (String) outputResult.getData();

            Testcase testcase = new Testcase();
            testcase.setProblemId(problemId);
            testcase.setInputFileId(inputId);
            testcase.setOutputFileId(outputId);
            testcase.setInputFileName(input.getOriginalFilename());
            testcase.setOutputFileName(output.getOriginalFilename());

            problemService.addTestCase(testcase);
            log.info("添加测试用例成功, problemId: {}", problemId);
            return Result.success();
        } catch (Exception e) {
            log.error("添加测试用例失败, problemId: {}", problemId, e);
            return Result.error(500, "添加测试用例失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delTestCase")
    public Result delTestCase(@RequestBody Testcase testcase) {
        try {
            log.info("删除测试用例请求, testcaseId: {}", testcase.getId());
            
            if (testcase.getId() == null) {
                return Result.error(400, "测试用例ID不能为空");
            }
            
            problemService.delTestCase(testcase);
            
            // 删除文件系统上的文件
            if (testcase.getInputFileId() != null) {
                fileSystemFeignClient.deleteFile(testcase.getInputFileId());
            }
            if (testcase.getOutputFileId() != null) {
                fileSystemFeignClient.deleteFile(testcase.getOutputFileId());
            }
            
            log.info("删除测试用例成功, testcaseId: {}", testcase.getId());
            return Result.success();
        } catch (Exception e) {
            log.error("删除测试用例失败, testcaseId: {}", testcase.getId(), e);
            return Result.error(500, "删除测试用例失败: " + e.getMessage());
        }
    }

    @GetMapping("/getTestCase")
    public Result getTestCase(@RequestParam Long problemId) {
        try {
            log.debug("获取测试用例列表, problemId: {}", problemId);
            
            if (problemId == null) {
                return Result.error(400, "题目ID不能为空");
            }
            
            return Result.success(problemService.getTestCase(problemId));
        } catch (Exception e) {
            log.error("获取测试用例列表失败, problemId: {}", problemId, e);
            return Result.error(500, "获取测试用例列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/probleminfo/{id}/tag")
    public Result getTag(@PathVariable("id") Long id) {
        try {
            log.debug("获取题目标签, problemId: {}", id);
            
            if (id == null) {
                return Result.error(400, "题目ID不能为空");
            }
            
            return Result.success(problemService.getTag(id));
        } catch (Exception e) {
            log.error("获取题目标签失败, problemId: {}", id, e);
            return Result.error(500, "获取题目标签失败: " + e.getMessage());
        }
    }

    @PostMapping("/probleminfo/{id}/addTag")
    public Result addTag(@PathVariable("id") Long id, @RequestBody String tagName) {
        log.info("添加题目标签请求, problemId: {}, tag: {}", id, tagName);
        //检验数据
        if(id == null){
            log.warn("添加标签失败: id为空");
            return Result.error(400, "id不能为空");
        }
        if(problemService.getProbleminfoById(id) == null){
            log.warn("添加标签失败: 题目不存在, problemId: {}", id);
            return Result.error(400, "题目不存在");
        }
        if(tagName == null || tagName.isEmpty()){
            log.warn("添加标签失败: tag为空");
            return Result.error(400, "tag不能为空");
        }
        if(tagName.length() > 20){
            log.warn("添加标签失败: tag长度超过限制, length: {}", tagName.length());
            return Result.error(400, "tag长度不能超过20");
        }

        //检查标签是否重复
        List<Tag> tags = problemService.getTag(id);
        for(Tag tag : tags){
            if(tag.getTag().equals(tagName)){
                log.warn("添加标签失败: 标签重复, tag: {}", tagName);
                return Result.error(400, "标签重复");
            }
        }

        problemService.addTag(id, tagName);
        log.info("添加题目标签成功, problemId: {}, tag: {}", id, tagName);
        return Result.success();
    }

    @DeleteMapping("/probleminfo/{id}/delTag")
    public Result delTag(@PathVariable("id") Long id, @RequestBody String tagName) {
        try {
            log.info("删除题目标签请求, problemId: {}, tag: {}", id, tagName);
            
            if (id == null) {
                return Result.error(400, "题目ID不能为空");
            }
            if (tagName == null || tagName.isEmpty()) {
                return Result.error(400, "标签名称不能为空");
            }
            
            problemService.delTag(id, tagName);
            log.info("删除题目标签成功, problemId: {}, tag: {}", id, tagName);
            return Result.success();
        } catch (Exception e) {
            log.error("删除题目标签失败, problemId: {}, tag: {}", id, tagName, e);
            return Result.error(500, "删除题目标签失败: " + e.getMessage());
        }
    }
}
