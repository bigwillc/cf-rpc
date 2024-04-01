package com.bigwillc.cfrpccore.meta;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务元的数据
 *
 * @author bigwillc on 2024/3/20
 */
@Data
@Builder
public class ServiceMeta {

    private String app;

    private String namespace;

    private String env;

    private String name;

    private Map<String, String> parameters = new HashMap<>();

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }

}
