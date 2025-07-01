package com.ygoj.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 对 JudgerBase 工具类的二次封装
 */
@Data
public class Judger {
//    常用常量
    private static final JSONArray files = JSONArray.parseArray("""
        [{"content": ""},\s
        {"name": "stdout",
        "max": 10240},
        {"name": "stderr",
        "max": 10240}]""");

    private JudgerBase judgerBase;

    Judger(String JudgerUrl){
        this.judgerBase = new JudgerBase(JudgerUrl);
    }

    /**
     * 编译代码
     *
     * @param cmd 编译命令
     * @param code 需要编译的代码
     * @param codeName 代码对应的文件名
     * @param exeName 编译结果的文件名
     * @return 结果map
     */
    public Map<String, String> compile(String cmd, String code, String codeName, String exeName){
        JSONObject copyIn = new JSONObject();
        copyIn.putObject(codeName);
        copyIn.getJSONObject(codeName).fluentPut("content", code);

        JSONObject json = judgerBase.makeOneCMD(cmd, files, null, null, copyIn,
                new String[]{"stdout", "stderr"}, new String[]{"a"});

        JSONObject res = judgerBase.request(judgerBase.packageCMD(json)).getJSONObject(0);

        Map<String, String> result = new HashMap<>();
        result.put("status", res.getString("status"));
        result.put("stdout", res.getJSONObject("files").getString("stdout"));
        result.put("stderr", res.getJSONObject("files").getString("stderr"));
        try{
            result.put("fileId", res.getJSONObject("fileIds").getString(exeName));
        }catch (Exception e){
            result.put("fileId", null);
        }

        return result;
    }

    public Map<String, String> run(String cmd, String exeName, String exeId, String stdinId,
                                   Long cpuLimit, Long memoryLimit){
        JSONArray files = Judger.files;
        files.set(0, new JSONObject());
        files.getJSONObject(0).fluentPut("fileId", stdinId);

        JSONObject copyIn = new JSONObject();
        copyIn.putObject(exeName);
        copyIn.getJSONObject(exeName).fluentPut("fileId", exeId);

        JSONObject json = judgerBase.makeOneCMD(cmd, files, cpuLimit, memoryLimit, copyIn,
                new String[]{"stderr"}, new String[]{"stdout"});

        JSONObject res = judgerBase.request(judgerBase.packageCMD(json)).getJSONObject(0);

        Map<String, String> result = new HashMap<>();
        result.put("status", res.getString("status"));
        result.put("time", res.getString("time"));
        result.put("memory", res.getString("memory"));
        result.put("stderr", res.getJSONObject("files").getString("stderr"));
        result.put("stdoutId", res.getJSONObject("fileIds").getString("stdout"));

        return result;
    }

    public void clearCache(){
        this.judgerBase.clearCache();
    }

    public JSONObject getAllFile(){
        return this.judgerBase.getAllFile();
    }

    public String uploadFile(File file){
        return this.judgerBase.uploadFile(file);
    }

    public String getFileById(String id){
        return judgerBase.getFileById(id);
    }
}
