package com.ygoj.judger.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.stereotype.Component;

@Component
public class SandboxJudgerClient{
    private final DockerClientConfig config = DefaultDockerClientConfig
            .createDefaultConfigBuilder()
            .withDockerHost("tcp://localhost:2375")
            .build();
    private final DockerClient dockerClient = DockerClientBuilder
            .getInstance(config)
            .build();

    void execute(String containerId, String[] command){

    }

    public void run(String imageName){
        //初始化文件

        //创建容器
        CreateContainerResponse container = dockerClient
                .createContainerCmd(imageName)
                .exec();
        //启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        //停止容器
        dockerClient.stopContainerCmd(container.getId()).exec();
        //删除容器
        dockerClient.removeContainerCmd(container.getId()).exec();

        //编译代码
        //运行代码
        //比较结果
        //返回结果
    }
}
