package com.bigwillc.cfrpccore.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述实例的原数据
 *
 * @author bigwillc on 2024/3/20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {

    private String schema;
    private String host;
    private Integer port;
    private String context;

    private boolean status; // online or offline
    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "cfrpc");
    }

    public String toMetas(){

        return JSON.toJSONString(this.getParameters());
    }
}
