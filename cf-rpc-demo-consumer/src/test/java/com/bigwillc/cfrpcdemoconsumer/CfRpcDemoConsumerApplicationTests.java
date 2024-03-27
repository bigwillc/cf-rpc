package com.bigwillc.cfrpcdemoconsumer;

import com.bigwillc.cfrpcdemoprovider.CfRpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class CfRpcDemoConsumerApplicationTests {

	static ApplicationContext context;

	@BeforeAll
	static void init() {
		context = SpringApplication.run(CfRpcDemoProviderApplication.class, "--server.port=8081");
	}

	@Test
	void contextLoads() {
		System.out.println(" ===> aaa ...");
	}

	@AfterAll
	static void destroy() {
		SpringApplication.exit(context, () -> 1);
	}



}
