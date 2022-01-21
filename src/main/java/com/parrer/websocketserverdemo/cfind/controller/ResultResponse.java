package com.parrer.websocketserverdemo.cfind.controller;

import com.parrer.constant.ApiResponseCodeEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ResultResponse<T> {

    private T data;

    private String code;

    private String message;

    private Boolean success = true;

    public ResultResponse(T data) {
        this.data = data;
        this.code = ApiResponseCodeEnum.SUCCESS.getCode();
        this.message = "success";
    }

    public ResultResponse() {
    }

    public ResultResponse(T data, String code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public ResultResponse(String code, String message, Boolean success) {
        this.code = code;
        this.message = message;
        this.success = success;
    }

    /**
     * 成功时候的调用
     *
     * @return
     */
    public static <T> ResultResponse<T> success(T data) {
        return new ResultResponse<>(data);
    }

    /**
     * 失败时候的调用
     *
     * @return
     */
    public static <T> ResultResponse<T> error(String message) {
        return new ResultResponse<>(ApiResponseCodeEnum.SYSTEM_ERROR.getCode(), message, false);
    }

    /**
     * 通用时候的调用
     *
     * @return
     */
    public static <T> ResultResponse<T> build(T data, String code, String message) {
        return new ResultResponse<>(data, code, message);
    }

    public static ResultResponse<Object> ok(Object data) {
        ResultResponse<Object> resultResponse = new ResultResponse<>();
        resultResponse.setData(data);
        resultResponse.setCode(ApiResponseCodeEnum.SUCCESS.getCode());
        resultResponse.setSuccess(true);
        resultResponse.setMessage("success");
        return resultResponse;
    }
    public static ResultResponse<Object> ok() {
        return ok(StringUtils.EMPTY);
    }

    @Override
    public String toString() {
        return "ResultResponse(data=" + this.getData() + ", success=" + success + ", code=" + this.getCode() + ", message=" + this.getMessage() + ")";
    }

}
