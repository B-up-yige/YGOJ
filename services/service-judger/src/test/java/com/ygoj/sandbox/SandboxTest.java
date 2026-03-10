package com.ygoj.sandbox;
import java.util.Arrays;

import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.judger.sandbox.impl.SandboxImpl;
import org.junit.jupiter.api.Test;

public class SandboxTest {
    @Test
    public void sandboxTest() {
        Sandbox sandbox = new SandboxImpl();

        SandboxExecuteRequest request = new SandboxExecuteRequest();
        request.setInputList(Arrays.asList("a", "b", "c"));
        request.setOutputList(Arrays.asList("a", "b", "c"));
        request.setCode("""
public class Main{
    public static void main(String[] args) {
//        try {
//            Thread.sleep(100000L);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}
                """);
        request.setLanguage("123");
        request.setCompileCommand("javac -encoding utf-8 ./Main.java");
        request.setRunCommand("java -Dfile.encoding=utf-8 Main");
        request.setFileName("Main.java");
        request.setTimeLimit(1000L);
        request.setMemoryLimit(512*1024L);

        SandboxExecuteResponse response = sandbox.sandboxExecute(request);

        System.out.println(response);
    }
}
