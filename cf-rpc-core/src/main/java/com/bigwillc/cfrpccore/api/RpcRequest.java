package com.bigwillc.cfrpccore.api;

import lombok.Data;

@Data
public class RpcRequest {

    private String service; // 接口
    private String methodSign; // 方法 methodSign
    private Object[] args; // 参数
}
