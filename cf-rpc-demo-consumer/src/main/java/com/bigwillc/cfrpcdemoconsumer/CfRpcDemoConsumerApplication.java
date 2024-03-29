package com.bigwillc.cfrpcdemoconsumer;

import com.bigwillc.cfrpccore.annotation.CFConsumer;
import com.bigwillc.cfrpccore.consumer.ConsumerConfig;
import com.bigwillc.cfrpcdemoapi.OrderService;
import com.bigwillc.cfrpcdemoapi.User;
import com.bigwillc.cfrpcdemoapi.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SpringBootApplication
@Import({ConsumerConfig.class})
//@ComponentScan(basePackages = "")
@RestController
@Slf4j
public class CfRpcDemoConsumerApplication {

	@Autowired
	ApplicationContext context;

	@CFConsumer
	UserService userService;	// 这个field 是一个空的

	@CFConsumer
	OrderService orderService;


	@GetMapping("/")
	@ResponseBody
	public User findById(@RequestParam(value = "id") int id) {
		log.info("测试负载均衡...");
		return userService.findById(id);
	}

	@GetMapping("/find")
	@ResponseBody
	public User findTimeout(@RequestParam(value = "timeout") int timeout) {
		log.info("测试负载均衡...");
		return userService.find(timeout);
	}


	public static void main(String[] args) {
		SpringApplication.run(CfRpcDemoConsumerApplication.class, args);
	}

	@Bean
	public ApplicationRunner consumer_runner() {


		return x -> {

			// rpcContext.set 需要考虑使用ThreadLocal
			long stat = System.currentTimeMillis();
			userService.find(800);
			System.out.println("Time: " + (System.currentTimeMillis() - stat));
		};



//		return allTest();
	}


	@NotNull
	private ApplicationRunner allTest() {

			// 常规int类型，返回User对象
			System.out.println("Case 1. >>===[常规int类型，返回User对象]===");
			User user = userService.findById(1);
			System.out.println("RPC result userService.findById(1) = " + user);

			// 测试方法重载，同名方法，参数不同
			System.out.println("Case 2. >>===[测试方法重载，同名方法，参数不同===");
			User user1 = userService.findById(1, "hubao");
			System.out.println("RPC result userService.findById(1, \"hubao\") = " + user1);

			// 测试返回字符串
			System.out.println("Case 3. >>===[测试返回字符串]===");
			System.out.println("userService.getName() = " + userService.getName());

			// 测试重载方法返回字符串
			System.out.println("Case 4. >>===[测试重载方法返回字符串]===");
			System.out.println("userService.getName(123) = " + userService.getName(123));

			// 测试local toString方法
//			System.out.println("Case 5. >>===[测试local toString方法]===");
//			System.out.println("userService.toString() = " + userService.toString());

			// 测试long类型
			System.out.println("Case 6. >>===[常规int类型，返回User对象]===");
			System.out.println("userService.getId(10) = " + userService.getId(10));

			// 测试long+float类型
			System.out.println("Case 7. >>===[测试long+float类型]===");
			System.out.println("userService.getId(10f) = " + userService.getId(10f));

			// 测试参数是User类型
			System.out.println("Case 8. >>===[测试参数是User类型]===");
			System.out.println("userService.getId(new User(100,\"KK\")) = " +
					userService.getId(new User(100, "KK")));


//			System.out.println("Case 9. >>===[测试返回long[]]===");
//			System.out.println(" ===> userService.getLongIds(): ");
//			for (long id : userService.getLongIds()) {
//				System.out.println(id);
//			}
//
//			System.out.println("Case 10. >>===[测试参数和返回值都是long[]]===");
//			System.out.println(" ===> userService.getLongIds(): ");
//			for (long id : userService.getIds(new int[]{4,5,6})) {
//				System.out.println(id);
//			}
//
//			// 测试参数和返回值都是List类型
//			System.out.println("Case 11. >>===[测试参数和返回值都是List类型]===");
//			List<User> list = userService.getList(List.of(
//					new User(100, "KK100"),
//					new User(101, "KK101")));
//			list.forEach(System.out::println);
//
//			// 测试参数和返回值都是Map类型
//			System.out.println("Case 12. >>===[测试参数和返回值都是Map类型]===");
//			Map<String, User> map = new HashMap<>();
//			map.put("A200", new User(200, "KK200"));
//			map.put("A201", new User(201, "KK201"));
//			userService.getMap(map).forEach(
//					(k,v) -> System.out.println(k + " -> " + v)
//			);
//
//			System.out.println("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
//			System.out.println("userService.getFlag(false) = " + userService.getFlag(false));
//
//			System.out.println("Case 14. >>===[测试参数和返回值都是User[]类型]===");
//			User[] users = new User[]{
//					new User(100, "KK100"),
//					new User(101, "KK101")};
//			Arrays.stream(userService.findUsers(users)).forEach(System.out::println);
//
//			System.out.println("Case 15. >>===[测试参数为long，返回值是User类型]===");
//			User userLong = userService.findById(10000L);
//			System.out.println(userLong);

//			System.out.println("Case 16. >>===[测试参数为boolean，返回值都是User类型]===");
//			User user100 = userService.ex(false);
//			System.out.println(user100);
//
//			System.out.println("Case 17. >>===[测试服务端抛出一个RuntimeException异常]===");
//			try {
//				User userEx = userService.ex(true);
//				System.out.println(userEx);
//			} catch (RuntimeException e) {
//				System.out.println(" ===> exception: " + e.getMessage());
//			}
		return null;
	}


}
