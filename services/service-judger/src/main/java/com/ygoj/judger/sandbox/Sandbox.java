package com.ygoj.judger.sandbox;

import com.ygoj.sandbox.SandboxExecuteRequest;
import com.ygoj.sandbox.SandboxExecuteResponse;

public interface Sandbox {
    SandboxExecuteResponse sandboxExecute(SandboxExecuteRequest sandboxExecuteRequest);
}
