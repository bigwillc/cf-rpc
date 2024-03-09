package com.bigwillc.cfrpccore.api;

import lombok.Data;

@Data
public class RpcRequest {

    private String service; // 接口
    private String method; // 方法
    private Object[] orgs; // 参数
}
