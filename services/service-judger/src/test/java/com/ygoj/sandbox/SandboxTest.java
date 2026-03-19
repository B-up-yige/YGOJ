package com.ygoj.sandbox;
import java.util.Arrays;

import com.ygoj.judger.sandbox.Sandbox;
import com.ygoj.judger.sandbox.SandboxExecuteRequest;
import com.ygoj.judger.sandbox.SandboxExecuteResponse;
import com.ygoj.judger.sandbox.impl.SandboxImpl;
import org.junit.jupiter.api.Test;

public class SandboxTest {
    @Test
    public void sandboxTest() {
        Sandbox sandbox = new SandboxImpl();

        String code = """
import java.util.ArrayList;
import java.util.List;

public class Main{
    public static void main(String[] args) {
//         for(int i = 1; i <= 10000000; i++)System.out.println("Hello World");
//        try {
//            Thread.sleep(100000L);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        
//        List<byte[]> buckets = new ArrayList<>();
//        for(int i = 1; i <= 1000000; i++){
//            byte[] chunk = new byte[1024 * 1024];
//            buckets.add(chunk);
//        }

//        String s = "";
//        for(int i = 1; i <= 100000; i++){
//            s += "a";
//        }

        List<String> a = new ArrayList<>();
        a.get(3);
    }
}
                """;

//        code = """
//                import java.util.ArrayList;
//                import java.util.List;
//
//                public class Main {
//                    public static void main(String[] args) {
//                        // 使用原始类型的 List（未指定泛型参数）→ 编译警告：rawtypes
//                        List list = new ArrayList();
//
//                        // 添加不同类型的元素（运行时均能正常工作）
//                        list.add("Hello, World!");
//                        list.add(123);       // 自动装箱为 Integer
//                        list.add(3.14);      // 自动装箱为 Double
//
//                        // 遍历并打印所有元素（运行时类型信息保留，强制转换由编译器插入）
//                        for (Object obj : list) {
//                            System.out.println(obj);
//                        }
//                    }
//                }""";

        SandboxExecuteRequest request = new SandboxExecuteRequest();
        request.setInputList(Arrays.asList("a", "b", "c"));
        request.setOutputList(Arrays.asList("a", "b", "c"));
        request.setCode(code);
        request.setLanguage("123");
        request.setCompileCommand("javac -encoding utf-8 -Xlint ./Main.java");
        request.setRunCommand("java -Dfile.encoding=utf-8 -Xmx2g Main");
        request.setFileName("Main.java");
        request.setTimeLimit(2000L);
        request.setMemoryLimit(256L);
        request.setImage("amazoncorretto:18");

        SandboxExecuteResponse response = sandbox.sandboxExecute(request);

        System.out.println();
        System.out.println(response);
    }
}
