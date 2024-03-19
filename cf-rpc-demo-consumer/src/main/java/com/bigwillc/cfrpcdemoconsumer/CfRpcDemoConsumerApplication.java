package com.bigwillc.cfrpcdemoconsumer;

import com.bigwillc.cfrpccore.annotation.CFConsumer;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.consumer.ConsumerConfig;
import com.bigwillc.cfrpccore.provider.ProviderBootstrap;
import com.bigwillc.cfrpcdemoapi.Order;
import com.bigwillc.cfrpcdemoapi.OrderService;
import com.bigwillc.cfrpcdemoapi.User;
import com.bigwillc.cfrpcdemoapi.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
@Import({ConsumerConfig.class})
//@ComponentScan(basePackages = "")
@RestController
public class CfRpcDemoConsumerApplication {

	@Autowired
	ApplicationContext context;

	@CFConsumer
	UserService userService;	// 这个field 是一个空的

	@CFConsumer
	OrderService orderService;


	@GetMapping("/")
	@ResponseBody
	public User findById(int id) {
		System.out.println("测试负载均衡...");
		return userService.findById(id);
	}


	public static void main(String[] args) {
		SpringApplication.run(CfRpcDemoConsumerApplication.class, args);
	}

	@Bean
	public ApplicationRunner consumer_runner() {
		return x -> {
//			List userArray = new ArrayList();
//			userArray.add(new User(1, "array1"));
//			userArray.add(new User(2, "array2"));
//			System.out.println(userService.getUserList(userArray));
//
//			HashMap<String, User> userMap = new HashMap<>();
//			userMap.put("1", new User(1, "map1"));
//			userMap.put("2", new User(2, "map2"));
//			System.out.println(userService.getUserMap(userMap));
//			System.out.println(userService.getUserMap(new java.util.HashMap<>()
//			{{
//				put("1", "map1");
//				put("2", "map2");
//			}}));

			System.out.println(userService.getId(10));
			System.out.println(userService.getId(new User(10, "bigwillc")));
			System.out.println(userService.getId(1.1f));
//
//			System.out.println(Arrays.toString(userService.getIds()));
//			System.out.println(Arrays.toString(userService.getLongIds()));
//			System.out.println(Arrays.toString(userService.getIds(new int[]{1, 2})));
//
//			User user = userService.findById(1);
//			System.out.println("===> user :" + user);
//
//			Order order = orderService.findById(100);
//			System.out.println("===> order :" + order);
//
//			System.out.println(userService.toString());
//			System.out.println(userService.getId(20));
//			System.out.println(userService.findById(20));
//			System.out.println(userService.findById(20, "bigwillc"));
//			System.out.println(userService.getName());

//			Order order404 = orderService.findById(404);
//			System.out.println("===> order 404 :" + order404);

//			Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);



		};
	}



}
