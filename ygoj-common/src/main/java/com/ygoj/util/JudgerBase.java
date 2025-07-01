package com.ygoj.util;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.File;
import java.util.List;

/// 针对 go-judger 的判题工具基础类
///
/// 网络请求利用 hutool 工具类
@Data
public class JudgerBase {
    public static final Long S = 1000000000L;
    public static final Long MB = 1048576L;

    //judger地址
    private String judgerUrl;

    //judger 各个接口地址
    private String judgerRunUrl;
    private String judgerFileUrl;

    /**
     * @param judgerUrl 判题机地址url
     */
    JudgerBase(String judgerUrl){
        this.judgerUrl = judgerUrl;
        this.judgerRunUrl = URLUtil.completeUrl(judgerUrl, "run");
        this.judgerFileUrl = URLUtil.completeUrl(judgerUrl, "file");
    }

    /**
     * 生成用于请求 /run POST 的json
     * 但是需要自行嵌套cmd，这只会生成cmd列表中的一个json
     * 返回值作用位置
     * {
     *     cmd: [@return]
     * }
     *
     * @return 用于请求的json
     */
    public JSONObject makeOneCMD(String args, JSONArray files, Long cpuLimit, Long memoryLimit,
                                 JSONObject copyIn,
                                 String[] copyOut,
                                 String[] copyOutCached){

        JSONObject json = new JSONObject();

        json.put("args", args.split(" "));
        json.put("env", JSONArray.parse("[\"PATH=/usr/bin:/bin\"]"));
        json.put("files", files);
        json.put("cpuLimit", cpuLimit == null ? 1*S : cpuLimit);
        json.put("memoryLimit", memoryLimit == null ? 100*MB : memoryLimit);
        json.put("procLimit", 50);
        json.put("copyIn", copyIn);
        json.put("copyOut", copyOut);
        json.put("copyOutCached", copyOutCached);

        return json;
    }

    /**
     * 把cmds打包成标准请求格式
     *
     * @param cmd cmd 的 json 列表
     * @return 可以用于请求的json
     */
    public JSONObject packageCMD(List<JSONObject> cmd){
        JSONObject json = new JSONObject();
        json.put("cmd", cmd);

        return json;
    }
    public JSONObject packageCMD(JSONObject cmd){
        JSONObject json = new JSONObject();
        json.put("cmd", new JSONArray(cmd));

        return json;
    }

    /**
     * 用json 请求 /run POST 并返回结果
     *
     * @param json 需要执行的命令对应的json对象
     * @return 执行结果的json对象
     */
    public JSONArray request(JSONObject json){
        String result = HttpRequest.post(judgerRunUrl)
                .body(json.toJSONString())
                .execute()
                .body();

        return JSONArray.parseArray(result);
    }

    /**
     * @return go-judger 中的文件缓存json
     */
    public JSONObject getAllFile(){
        String result = HttpRequest.get(judgerFileUrl)
                .execute()
                .body();

        return JSONObject.parseObject(result);
    }

    /**
     * 清除 go-judger 中的文件缓存
     */
    public void clearCache(){
        JSONObject cache = getAllFile();

        cache.keySet().forEach(key -> {
            HttpRequest.delete(judgerFileUrl + "/" + key)
                    .execute();
        });
    }

    /**
     * 上传文件到 go-judger
     *
     * @param file 需要上传的文件
     * @return go-judger 中的缓存文件 id
     */
    public String uploadFile(File file){
        String result = HttpRequest.post(judgerFileUrl)
                .form("file", file)
                .execute()
                .body();

        return result.replaceAll("\"", "");
    }

    public String getFileById(String id){
        String res = HttpRequest.get(judgerFileUrl + "/" + id)
                .execute()
                .body();

        return res;
    }
}