package com.bigwillc.cfrpccore.api;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private String service; // 接口
    private String methodSign; // 方法 methodSign
    private Object[] args; // 参数
}
