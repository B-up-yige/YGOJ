package com.ygoj.common;

import lombok.Data;

@Data
public class Result {
    Integer code;
    String msg;
    Object data;

    public static Result success() {
        Result result = new Result();
        result.code = 200;
        result.msg = "success";
        result.data = null;
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.code = 200;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static Result error() {
        Result result = new Result();
        result.code = 500;
        result.msg = "error";
        result.data = null;
        return result;
    }

    public static Result error(Integer code, String msg) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        result.data = null;
        return result;
    }
}
