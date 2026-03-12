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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.sandbox.SandboxExecuteRequest;
import com.ygoj.sandbox.SandboxExecuteResponse;
import org.springframework.util.StopWatch;

public class SandboxImplByDocker implements Sandbox {
    private static final String GLOBAL_TEMP_DIR_NAME = "temp";
    private static final Map<String, String> IMAGE = new HashMap<>();

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
        result.setCompileInfo(new HashMap<>());


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
                .dockerHost(URI.create("tcp://localhost:2376"))
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance()
                .withDockerHttpClient(dockerHttpClient)
                .build();
        if(!IMAGE.containsKey(image)) {
            //拉取镜像
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("镜像下载完成");
            IMAGE.put(image, image);
        }

        //创建容器
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(userTempDir, new Volume("/workspace")));
        hostConfig.withMemory(memoryLimit * 1024 * 1024);
        hostConfig.withCpuCount(1L);
        CreateContainerResponse containerResponse = createContainerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withTty(true)
                .exec();

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

            result.getCompileInfo().put("time", stopWatch.getLastTaskTimeMillis());
            result.getCompileInfo().put("memory", maxMemory[0]);
            if(!stdout[0].isEmpty()) {
                result.getCompileInfo().put("stdout", stdout[0]);
            }else if(!stderr[0].isEmpty()) {
                result.getCompileInfo().put("stderr", stderr[0]);

                //TODO 编译错误直接返回结果
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        String inputFilePath = userTempDir + File.separator + "input.in";
        String outputFilePath = userTempDir + File.separator + "ans.out";
        String userOutputFilepath = userTempDir + File.separator + "output.out";
        runCommand = runCommand + " < input.in > output.out";

        FileUtil.writeString("", userOutputFilepath, StandardCharsets.UTF_8);

        for(int i = 0; i < inputList.size(); i++) {
            //下载输入输出
            FileUtil.writeString(inputList.get(i), inputFilePath, StandardCharsets.UTF_8);
            FileUtil.writeString(outputList.get(i), outputFilePath, StandardCharsets.UTF_8);

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
                        .exec(new ExecStartResultCallback(){
                            @Override
                            public void onNext(Frame frame) {
                                if(StreamType.STDERR.equals(frame.getStreamType())) {
                                    stderr[0] = stderr[0] + new String(frame.getPayload());
                                }
                                super.onNext(frame);
                            }
                        })
                        .awaitCompletion();
                stopWatch.stop();
                Thread.sleep(1000L);
                statsCmd.close();

                //记录运行数据
                //TODO 优化
                String message = "";
                if(!stderr[0].isEmpty()){
                    message = message + stderr[0] + "\n";
                }
                message = message + "time:" + stopWatch.getLastTaskTimeMillis() + "\n";
                message = message + "memory:" + maxMemory[0] + "\n";

                result.getDetail().add(message);

                //TODO 对比答案

                //TODO 记录结果

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

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
