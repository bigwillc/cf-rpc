package com.bigwillc.cfrpccore.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    boolean status;
    T data;

    Exception ex;
}
