package com.ygoj.judger;

import com.ygoj.judger.client.SandboxJudgerClient;
import org.junit.jupiter.api.Test;

public class SandboxJudgerClientTest {
    private final SandboxJudgerClient judgerClient = new SandboxJudgerClient();

    @Test
    public void testSandboxJudgerClient() {
        judgerClient.run("ubuntu");
    }
}
