package com.bigwillc.cfrpccore.api;

import java.util.List;

/**
 * 从大集合里面选取小集合
 *
 * @author bigwillc on 2024/3/16
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    Router Default = p -> p;
}
