package com.ygoj.judger.sandbox.impl;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.sandbox.SandboxExecuteRequest;
import com.ygoj.sandbox.SandboxExecuteResponse;
import org.springframework.util.StopWatch;

public class SandboxImpl implements Sandbox {
    private static final String GLOBAL_TEMP_DIR_NAME = "temp";

    private Map<String, String> process(String command, String workDir,
                                        Long timeLimit) throws IOException, InterruptedException {
            Map<String, String> result = new HashMap<>();

            StopWatch stopWatch = new StopWatch();

            stopWatch.start();
            Process process = Runtime.getRuntime().exec(command, null, new File(workDir));
            //超时控制
            //TODO 有bug,到时候直接换docker
            new Thread(() -> {
                try {
                    Thread.sleep(timeLimit*2);
                    process.destroyForcibly();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int exitValue = process.waitFor();
            stopWatch.stop();

            result.put("time", ""+stopWatch.getLastTaskTimeMillis());
            result.put("exitValue", String.valueOf(exitValue));

            //获取进程输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String runInfo;
            StringBuilder errorInfo = new StringBuilder();
            while ((runInfo = reader.readLine()) != null) {
                errorInfo.append(runInfo).append("\n");
            }

            result.put("errorInfo", errorInfo.toString());

            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();

            return result;
    }

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

        //编译代码
        Map<String, String> compileResult;
        try {
            compileResult = process(compileCommand, userTempDir, timeLimit*2);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        //执行编译后的代码
        String inputFilePath = userTempDir + File.separator + "input.in";
        String outputFilePath = userTempDir + File.separator + "output.out";
        List<String> detail = new ArrayList<>();
        for(String input : inputList) {
            FileUtil.writeString(input, inputFilePath, StandardCharsets.UTF_8);
            FileUtil.writeString("", outputFilePath, StandardCharsets.UTF_8);
            try {
                //windows要在命令前加 "cmd /c"
                String osName = System.getProperty("os.name");
                osName = osName.toLowerCase();

                Map<String, String> result = process(
                        (osName.matches(".*?windows.*?") ? "cmd /c " : "")
                                + runCommand + " < input.in > output.out",
                        userTempDir,
                        timeLimit);

                //TODO 对比答案
                detail.add("accept: " + result.get("time") + "ms");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //整理输出结果
        SandboxExecuteResponse sandboxExecuteResponse = new SandboxExecuteResponse();
        sandboxExecuteResponse.setDetail(detail);
        sandboxExecuteResponse.setStatus("AC");
        sandboxExecuteResponse.setError(null);
        sandboxExecuteResponse.setWarning(null);
        sandboxExecuteResponse.setCompileInfo(compileResult);

        //删除临时目录
        FileUtil.del(userTempDir);

        return sandboxExecuteResponse;
    }
}
