package com.bigwillc.cfrpccore.api;

import lombok.Data;

/**
 * rpc 同一异常类
 *
 * @author bigwillc on 2024/3/27
 */
@Data
public class RpcException extends RuntimeException{

    private String Errcode;

    public RpcException() {
    }

    public RpcException(String errcode) {
        Errcode = errcode;
    }

    public RpcException(String message, String errcode) {
        super(message);
        Errcode = errcode;
    }

    public RpcException(String message, Throwable cause, String errcode) {
        super(message, cause);
        Errcode = errcode;
    }

    public RpcException(Throwable cause, String errcode) {
        super(cause);
        Errcode = errcode;
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String errcode) {
        super(message, cause, enableSuppression, writableStackTrace);
        Errcode = errcode;
    }

    // X => 技术类异常
    // Y => 业务类异常
    // Z => unknown, 搞不清楚的异常
    public static final String SoecketTimeoutEx = "X001" + "-" + "http_invoke_time";
    public static final String NoSuchMethodEx = "X002" + "-" + "method_not_exists";
    public static final String UnknowEx = "Z001" + "-" + "unknown_exception";
    public static final String NoProviderEx = "P001" + "-" + "no_avaliable_provider";

//    UnknownEx("Y", "001", "unknown_exception"),
}
