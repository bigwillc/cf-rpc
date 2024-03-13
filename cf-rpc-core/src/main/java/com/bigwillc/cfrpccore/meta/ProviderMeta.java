package com.bigwillc.cfrpccore.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author bigwillc on 2024/3/13
 */
@Data
public class ProviderMeta {

    Method method;

    String methodSign;

    Object serviceImpl;

}
