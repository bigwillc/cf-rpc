package com.bigwillc.cfrpccore.registry;

import com.bigwillc.cfrpccore.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author bigwillc on 2024/3/19
 */
@Data
@AllArgsConstructor
public class Event {
    List<InstanceMeta> data;
}
