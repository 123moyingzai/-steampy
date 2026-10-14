package com.steampy.dto;

import lombok.Data;

/**
 * 统一 API 返回包装
 * 核心职责：将后端各接口返回的业务数据统一封装为标准的 { code, message, data } 结构
 * 泛型 T 表示 data 字段承载的业务实体类型，便于前端统一解构响应
 *
 * @param <T> data 字段承载的业务实体泛型
 * @author steampy
 * @date 2026-10-14
 */
@Data
public class Result<T> {
    /** 响应状态码，200 表示成功，4xx/5xx 表示业务/系统错误 */
    private int code;
    /** 响应提示信息，成功时通常为 "success"，失败时为错误描述 */
    private String message;
    /** 业务数据载体，类型由泛型 T 决定 */
    private T data;

    /**
     * 构造成功响应并携带泛型数据
     *
     * @param <T>  data 字段的业务实体类型
     * @param data 要返回的业务数据，可为 null
     * @return 统一包装的成功 Result 实例（code=200, message="success"）
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    /**
     * 构造无数据的成功响应
     *
     * @param <T> data 字段的业务实体类型（此处固定为 null）
     * @return 统一包装的成功 Result 实例（data=null）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 构造业务失败响应，默认使用 400 作为错误码
     *
     * @param <T>     data 字段的业务实体类型（此处固定为 null）
     * @param message 错误描述信息
     * @return 统一包装的失败 Result 实例（code=400）
     */
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.code = 400;
        r.message = message;
        return r;
    }

    /**
     * 构造指定状态码的失败响应
     *
     * @param <T>     data 字段的业务实体类型（此处固定为 null）
     * @param code    自定义错误码
     * @param message 错误描述信息
     * @return 统一包装的失败 Result 实例
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
