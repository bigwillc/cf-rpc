package com.bigwillc.cfrpcdemoprovider;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.provider.ProviderBootstrap;
import com.bigwillc.cfrpccore.provider.ProviderConfig;
import com.bigwillc.cfrpccore.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class CfRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CfRpcDemoProviderApplication.class, args);
    }

}
