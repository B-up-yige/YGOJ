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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class SandboxImpl implements Sandbox {
    private static final String GLOBAL_TEMP_DIR_NAME = "temp";
    private static final Map<String, Boolean> IMAGE = new HashMap<>();

    @Autowired
    private FileSystemFeignClient fileSystemFeignClient;

    static {
        //创建默认dockerClient
        DockerHttpClient dockerHttpClient =  new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create("tcp://127.0.0.1:2376"))
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

    //TODO 加日志
    //TODO 错误处理
    @Override
    public SandboxExecuteResponse sandboxExecute(SandboxExecuteRequest sandboxExecuteRequest) {
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


        //拼接临时目录
        String userDir = System.getProperty("user.dir");
        String globalTempDir = userDir + File.separator + GLOBAL_TEMP_DIR_NAME;
        String tempName = UUID.randomUUID().toString()+ DateUtil.currentSeconds();
        String userTempDir = globalTempDir + File.separator + tempName;
        String codeFilePath = userTempDir + File.separator + fileName;


        //创建判题临时目录
        FileUtil.mkdir(userTempDir);
        //存入代码
        FileUtil.writeString(code, codeFilePath, StandardCharsets.UTF_8);


        //创建默认dockerClient
        DockerHttpClient dockerHttpClient =  new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create("tcp://192.168.61.135:2376"))
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance()
                .withDockerHttpClient(dockerHttpClient)
                .build();


        //拉取镜像
        //TODO 镜像本地化
        if(!IMAGE.getOrDefault(image, false)) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback();
            try {
                pullImageCmd.exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("镜像下载完成");
            IMAGE.put(image, true);
        }

        //创建容器
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

        //获取容器ID
        String containerId = containerResponse.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();


        //编译代码
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", compileCommand)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .withWorkingDir("/workspace")
                .exec();
        String execId = execCreateCmdResponse.getId();
        try {
            CompileInfo compileInfo = new CompileInfo();
            final String[] stdout = {""};
            final String[] stderr = {""};
            final Long[] maxMemory = {0L};

            StopWatch stopWatch = new StopWatch();

            //统计内存
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }
                @Override
                public void onStart(Closeable closeable) {
                }
                @Override
                public void onError(Throwable throwable) {
                }
                @Override
                public void onComplete() {
                }
                @Override
                public void close() throws IOException {
                }
            });

            stopWatch.start();
            //开始执行命令
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

            //整合结果
            compileInfo.setTime(stopWatch.getLastTaskTimeMillis());
            compileInfo.setMemory(maxMemory[0]);
            compileInfo.setCode(dockerClient.inspectExecCmd(execId).exec().getExitCodeLong());
            if(!stdout[0].isEmpty()) {
                compileInfo.setStdout(stdout[0]);
            }else if(!stderr[0].isEmpty()) {
                compileInfo.setStderr(stderr[0]);
            }

            result.setCompileInfo(compileInfo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        //编译错误直接返回结果
        if(result.getCompileInfo().getCode() == 0) {
            String inputFilePath = userTempDir + File.separator + "input.in";
            String outputFilePath = userTempDir + File.separator + "ans.out";
            String userOutputFilepath = userTempDir + File.separator + "output.out";
            runCommand = runCommand + " < input.in > output.out";

            FileUtil.writeString("", userOutputFilepath, StandardCharsets.UTF_8);

            for (int i = 0; i < inputList.size(); i++) {
                //下载输入输出
                ResponseEntity<byte[]> inputFile = fileSystemFeignClient.downloadFile(inputList.get(i));
                ResponseEntity<byte[]> outputFile = fileSystemFeignClient.downloadFile(outputList.get(i));
                FileUtil.writeBytes(inputFile.getBody(), inputFilePath);
                FileUtil.writeBytes(outputFile.getBody(), outputFilePath);

                //执行代码
                execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                        .withCmd("sh", "-c", runCommand)
                        .withAttachStderr(true)
                        .withAttachStdout(true)
                        .withTty(true)
                        .withWorkingDir("/workspace")
                        .exec();
                execId = execCreateCmdResponse.getId();

                try {
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
                        public void onStart(Closeable closeable) {
                        }

                        @Override
                        public void onError(Throwable throwable) {
                        }

                        @Override
                        public void onComplete() {
                        }

                        @Override
                        public void close() throws IOException {
                        }
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

                    //记录运行数据
                    ExecuteDetail detail = new ExecuteDetail();
                    if (!stderr[0].isEmpty()) {
                        detail.setStderr(stderr[0]);
                    }
                    detail.setTime(stopWatch.getLastTaskTimeMillis());
                    detail.setMemory(maxMemory[0]);
                    detail.setCode(dockerClient.inspectExecCmd(execId).exec().getExitCodeLong());

                    //是否超出限制
                    if (detail.getTime() > timeLimit) {
                        detail.setStatus("TLE");
                    } else if (maxMemory[0] > memoryLimit * 1024 * 1024) {
                        detail.setStatus("MLE");
                    }else if(detail.getCode() != 0){
                        detail.setStatus("RE");
                    } else {
                        //对比答案
                        detail.setStatus(checkAnswer(outputFilePath, userOutputFilepath));
                    }

                    result.getDetail().add(detail);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }else{
            result.setStatus("CE");
        }

        //停止容器
        dockerClient.stopContainerCmd(containerId).exec();

        //删除容器
        dockerClient.removeContainerCmd(containerId).exec();

        //删除tempDir
        FileUtil.del(userTempDir);

        return result;
    }
}
