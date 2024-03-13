package com.bigwillc.cfrpcdemoprovider;

import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.provider.ProviderBootstrap;
import com.bigwillc.cfrpccore.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
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

    // 使用http + json 来实现序列化和通信

    @Autowired
    ProviderBootstrap providerBootstrap;

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

        return providerBootstrap.invoke(request);
    }


    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("com.bigwillc.cfrpcdemoapi.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});

            RpcResponse rpcResponse = invoke(request);
            System.out.println("return: " + rpcResponse);

            RpcRequest request2 = new RpcRequest();
            request2.setService("com.bigwillc.cfrpcdemoapi.UserService");
            request2.setMethodSign("findById@2_int_java.lang.String");
            request2.setArgs(new Object[]{100, "bigwillc"});

            RpcResponse rpcResponse2 = invoke(request2);
            System.out.println("return: " + rpcResponse2);
        };
    }

}
