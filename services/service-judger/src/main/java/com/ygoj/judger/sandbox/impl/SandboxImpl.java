package com.ygoj.judger.sandbox.impl;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.ygoj.judger.CompileInfo;
import com.ygoj.judger.ExecuteDetail;
import com.ygoj.judger.feign.FileSystemFeignClient;
import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.sandbox.SandboxExecuteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Component
public class SandboxImpl implements Sandbox {
    private static final String GLOBAL_TEMP_DIR_NAME = "temp";
    private static final Map<String, Boolean> IMAGE = new HashMap<>();

    @Autowired
    private FileSystemFeignClient fileSystemFeignClient;

    static {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null || dockerHost.isEmpty()) {
            dockerHost = "unix:///var/run/docker.sock";
        }

        //创建默认dockerClient
        DockerHttpClient dockerHttpClient =  new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create(dockerHost))
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance()
                .withDockerHttpClient(dockerHttpClient)
                .build();

        for (Image image : dockerClient.listImagesCmd().exec()) {
            for(String tag : image.getRepoTags()){
                IMAGE.put(tag, true);
            }
        }

    }

    public String checkAnswer(String userOutputPath, String answerPath) {
        List<String> userOutput = FileUtil.readLines(userOutputPath, StandardCharsets.UTF_8);
        List<String> answer = FileUtil.readLines(answerPath, StandardCharsets.UTF_8);

        for(int i = 0; i < Math.max(userOutput.size(), answer.size()); i++) {
            String userOutputLine = "", answerLine = "";
            if(userOutput.size() >= i+1){
                userOutputLine = userOutput.get(i).stripTrailing();
            }
            if(answer.size() >= i+1){
                answerLine = answer.get(i).stripTrailing();
            }

            if(!userOutputLine.equals(answerLine)){
                return "WA";
            }
        }

        return "AC";
    }

    @Override
    public SandboxExecuteResponse sandboxExecute(SandboxExecuteRequest sandboxExecuteRequest) {
        try {
            return doJudge(sandboxExecuteRequest);
        } catch (Exception e) {
            log.error("判题过程发生异常: {}", e.getMessage(), e);
            // 返回系统错误响应
            SandboxExecuteResponse errorResponse = new SandboxExecuteResponse();
            errorResponse.setStatus("SE");
            errorResponse.setRecordId(sandboxExecuteRequest.getRecordId());
            CompileInfo compileInfo = new CompileInfo();
            compileInfo.setStderr("System Error: " + e.getMessage());
            errorResponse.setCompileInfo(compileInfo);
            return errorResponse;
        }
    }
    
    private SandboxExecuteResponse doJudge(SandboxExecuteRequest sandboxExecuteRequest) {
        List<String> inputList = sandboxExecuteRequest.getInputList();
        List<String> outputList = sandboxExecuteRequest.getOutputList();
        String code = sandboxExecuteRequest.getCode();
        String language = sandboxExecuteRequest.getLanguage();
        String compileCommand = sandboxExecuteRequest.getCompileCommand();
        String runCommand = sandboxExecuteRequest.getRunCommand();
        String fileName = sandboxExecuteRequest.getFileName();
        Long timeLimit = sandboxExecuteRequest.getTimeLimit();
        Long memoryLimit = sandboxExecuteRequest.getMemoryLimit();
        String image = sandboxExecuteRequest.getImage();

        SandboxExecuteResponse result = new SandboxExecuteResponse();
        result.setDetail(new ArrayList<>());
        result.setRecordId(sandboxExecuteRequest.getRecordId());
        result.setStatus("waiting");

        //拼接临时目录
        String globalTempDir = "/tmp/judgerTemp";
        String tempName = UUID.randomUUID().toString()+ DateUtil.currentSeconds();
        String userTempDir = globalTempDir + File.separator + tempName;
        String codeFilePath = userTempDir + File.separator + fileName;

        //创建判题临时目录
        FileUtil.mkdir(userTempDir);
        //存入代码
        FileUtil.writeString(code, codeFilePath, StandardCharsets.UTF_8);

        //创建默认dockerClient
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null || dockerHost.isEmpty()) {
            dockerHost = "unix:///var/run/docker.sock";
        }
        
        DockerHttpClient dockerHttpClient =  new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create(dockerHost))
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance()
                .withDockerHttpClient(dockerHttpClient)
                .build();

        String containerId = null;
        //拉取镜像
        if(!IMAGE.getOrDefault(image, false)) {
            try {
                log.info("开始拉取镜像: {}", image);
                PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
                PullImageResultCallback pullImageResultCallback = new PullImageResultCallback();
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
                log.info("镜像下载完成: {}", image);
                IMAGE.put(image, true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cleanupResources(null, userTempDir, dockerClient);
                throw new RuntimeException("镜像拉取被中断: " + image, e);
            } catch (Exception e) {
                cleanupResources(null, userTempDir, dockerClient);
                throw new RuntimeException("镜像拉取失败: " + image + ", 错误: " + e.getMessage(), e);
            }
        }

        //创建容器
        try {
            CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image);
            HostConfig hostConfig = new HostConfig();
            hostConfig.setBinds(new Bind(userTempDir, new Volume("/workspace")));
            hostConfig.withMemory((memoryLimit + Math.max(memoryLimit/2, 100)) * 1024 * 1024);
            hostConfig.withCpuCount(1L);
            CreateContainerResponse containerResponse = createContainerCmd
                    .withHostConfig(hostConfig)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .withTty(true)
                    .exec();

            containerId = containerResponse.getId();
            log.info("容器创建成功: {}", containerId);
            
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动成功: {}", containerId);
        } catch (Exception e) {
            cleanupResources(containerId, userTempDir, dockerClient);
            throw new RuntimeException("容器创建或启动失败: " + e.getMessage(), e);
        }

        //编译代码
        try {
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", compileCommand)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .withWorkingDir("/workspace")
                    .exec();
            String execId = execCreateCmdResponse.getId();
            
            CompileInfo compileInfo = new CompileInfo();
            final String[] stdout = {""};
            final String[] stderr = {""};
            final Long[] maxMemory = {0L};

            StopWatch stopWatch = new StopWatch();
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }
                @Override
                public void onStart(Closeable closeable) {}
                @Override
                public void onError(Throwable throwable) {
                    log.error("统计内存时发生错误: {}", throwable.getMessage());
                }
                @Override
                public void onComplete() {}
                @Override
                public void close() throws IOException {}
            });

            stopWatch.start();
            dockerClient.execStartCmd(execId)
                    .exec(new ExecStartResultCallback(){
                        @Override
                        public void onNext(Frame frame) {
                            if(StreamType.STDOUT.equals(frame.getStreamType())) {
                                stdout[0] = stdout[0] + new String(frame.getPayload());
                            }else if(StreamType.STDERR.equals(frame.getStreamType())) {
                                stderr[0] = stderr[0] + new String(frame.getPayload());
                            }
                            super.onNext(frame);
                        }
                    })
                    .awaitCompletion();
            stopWatch.stop();
            statsCmd.close();

            compileInfo.setTime(stopWatch.getLastTaskTimeMillis());
            compileInfo.setMemory(maxMemory[0]);
            compileInfo.setCode(dockerClient.inspectExecCmd(execId).exec().getExitCodeLong());
            if(!stdout[0].isEmpty()) {
                compileInfo.setStdout(stdout[0]);
            }else if(!stderr[0].isEmpty()) {
                compileInfo.setStderr(stderr[0]);
            }

            result.setCompileInfo(compileInfo);
            log.info("编译完成，退出码: {}", compileInfo.getCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cleanupResources(containerId, userTempDir, dockerClient);
            throw new RuntimeException("编译过程被中断", e);
        } catch (Exception e) {
            cleanupResources(containerId, userTempDir, dockerClient);
            throw new RuntimeException("编译过程失败: " + e.getMessage(), e);
        }

        //编译错误直接返回结果
        if(result.getCompileInfo().getCode() == 0) {
            String inputFilePath = userTempDir + File.separator + "input.in";
            String outputFilePath = userTempDir + File.separator + "ans.out";
            String userOutputFilepath = userTempDir + File.separator + "output.out";
            runCommand = runCommand + " < input.in > output.out";

            FileUtil.writeString("", userOutputFilepath, StandardCharsets.UTF_8);

            for (int i = 0; i < inputList.size(); i++) {
                try {
                    ResponseEntity<byte[]> inputFile = fileSystemFeignClient.downloadFile(inputList.get(i));
                    ResponseEntity<byte[]> outputFile = fileSystemFeignClient.downloadFile(outputList.get(i));
                    FileUtil.writeBytes(inputFile.getBody(), inputFilePath);
                    FileUtil.writeBytes(outputFile.getBody(), outputFilePath);

                    ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                            .withCmd("sh", "-c", runCommand)
                            .withAttachStderr(true)
                            .withAttachStdout(true)
                            .withTty(true)
                            .withWorkingDir("/workspace")
                            .exec();
                    String execId = execCreateCmdResponse.getId();

                    final String[] stderr = {""};
                    final Long[] maxMemory = {0L};

                    StopWatch stopWatch = new StopWatch();
                    StatsCmd statsCmd = dockerClient.statsCmd(containerId);
                    statsCmd.exec(new ResultCallback<Statistics>() {
                        @Override
                        public void onNext(Statistics statistics) {
                            maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                        }
                        @Override
                        public void onStart(Closeable closeable) {}
                        @Override
                        public void onError(Throwable throwable) {
                            log.error("统计运行内存时发生错误: {}", throwable.getMessage());
                        }
                        @Override
                        public void onComplete() {}
                        @Override
                        public void close() throws IOException {}
                    });

                    stopWatch.start();
                    dockerClient.execStartCmd(execId)
                            .exec(new ExecStartResultCallback() {
                                @Override
                                public void onNext(Frame frame) {
                                    if (StreamType.STDERR.equals(frame.getStreamType())) {
                                        stderr[0] = stderr[0] + new String(frame.getPayload());
                                    }
                                    super.onNext(frame);
                                }
                            })
                            .awaitCompletion(timeLimit * 2, TimeUnit.MILLISECONDS);
                    stopWatch.stop();
                    Thread.sleep(1000L);
                    statsCmd.close();

                    ExecuteDetail detail = new ExecuteDetail();
                    if (!stderr[0].isEmpty()) {
                        detail.setStderr(stderr[0]);
                    }
                    detail.setTime(stopWatch.getLastTaskTimeMillis());
                    detail.setMemory(maxMemory[0]);
                    detail.setCode(dockerClient.inspectExecCmd(execId).exec().getExitCodeLong());

                    if (detail.getTime() > timeLimit) {
                        detail.setStatus("TLE");
                    } else if (maxMemory[0] > memoryLimit * 1024 * 1024) {
                        detail.setStatus("MLE");
                    }else if(detail.getCode() != 0){
                        detail.setStatus("RE");
                    } else {
                        detail.setStatus(checkAnswer(outputFilePath, userOutputFilepath));
                    }
                    result.setStatus(updateStatus(result.getStatus(), detail.getStatus()));
                    result.getDetail().add(detail);
                    log.info("测试用例 {}/{} 执行完成，结果: {}", i+1, inputList.size(), detail.getStatus());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cleanupResources(containerId, userTempDir, dockerClient);
                    throw new RuntimeException("测试用例执行被中断", e);
                } catch (Exception e) {
                    cleanupResources(containerId, userTempDir, dockerClient);
                    throw new RuntimeException("测试用例 " + (i+1) + " 执行失败: " + e.getMessage(), e);
                }
            }
        }else{
            result.setStatus("CE");
            log.info("编译错误，返回CE状态");
        }
        
        // 清理资源
        cleanupResources(containerId, userTempDir, dockerClient);
        
        return result;
    }

    static List<String> statusList = Arrays.asList("CE", "RE", "MLE", "TLE", "WA", "AC");
    private String updateStatus(String status, String detailStatus) {
        for(String s : statusList){
            if(s.equals(detailStatus) || s.equals(status)){
                return s;
            }
        }
        return "AC";
    }
    
    /**
     * 清理资源：停止并删除容器，删除临时文件
     */
    private void cleanupResources(String containerId, String userTempDir, DockerClient dockerClient) {
        if (containerId != null && dockerClient != null) {
            try {
                log.info("开始清理容器: {}", containerId);
                dockerClient.stopContainerCmd(containerId).withTimeout(5).exec();
                log.info("容器已停止: {}", containerId);
            } catch (Exception e) {
                log.warn("停止容器失败: {}, 错误: {}", containerId, e.getMessage());
            }
            
            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                log.info("容器已删除: {}", containerId);
            } catch (Exception e) {
                log.warn("删除容器失败: {}, 错误: {}", containerId, e.getMessage());
            }
        }
        
        if (userTempDir != null) {
            try {
                log.info("开始清理临时目录: {}", userTempDir);
                FileUtil.del(userTempDir);
                log.info("临时目录已删除: {}", userTempDir);
            } catch (Exception e) {
                log.warn("删除临时目录失败: {}, 错误: {}", userTempDir, e.getMessage());
            }
        }
        
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (Exception e) {
                log.warn("关闭DockerClient失败: {}", e.getMessage());
            }
        }
    }
}



