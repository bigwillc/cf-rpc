package com.bigwillc.cfrpccore.filter;

import com.bigwillc.cfrpccore.api.Filter;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.util.MethodUtils;
import com.bigwillc.cfrpccore.util.MockUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author bigwillc on 2024/3/26
 */
@Slf4j
public class MockFilter implements Filter {

    @SneakyThrows
    @Override
    public Object preFilter(RpcRequest request) {
        log.info("mock filter: {}", request);
        Class service = Class.forName(request.getService());
        Method method = getMethod(service, request.getMethodSign());
        Class clazz = method.getReturnType();
        Object mockResult = MockUtils.mock(clazz);
        log.info("mock result: {}", mockResult);
        return mockResult;
    }

    private Method getMethod(Class service, String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst().orElse(null);
    }


    @Override
    public Object postFilter(RpcRequest request, RpcResponse rpcResponse, Object result) {
        return null;
    }
}
