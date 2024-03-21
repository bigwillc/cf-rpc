package com.bigwillc.cfrpccore.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author bigwillc on 2024/3/13
 */
@Data
@Builder
public class ProviderMeta {

    Method method;

    String methodSign;

    Object serviceImpl;

}
