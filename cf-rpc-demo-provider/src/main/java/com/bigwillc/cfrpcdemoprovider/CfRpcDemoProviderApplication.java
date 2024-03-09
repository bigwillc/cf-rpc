package com.bigwillc.cfrpcdemoprovider;

import com.bigwillc.cfrpccore.annotation.CFProvider;
import com.bigwillc.cfrpccore.api.RpcRequest;
import com.bigwillc.cfrpccore.api.RpcResponse;
import com.bigwillc.cfrpccore.provider.ProviderConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Import({ProviderConfig.class})
public class CfRpcDemoProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfRpcDemoProviderApplication.class, args);
	}

	@RequestMapping("/")
	public RpcResponse invoke(@RequestBody RpcRequest request) {

		return invokeRequest(request);
	}

	private RpcResponse invokeRequest(RpcRequest request) {

		Object bean = skeleton.get(request.getService());
		try {
//			Method method = bean.getClass().getDeclaredMethod(request.getMethod());
			Method method = findMethod(bean.getClass(), request.getMethod());
			Object result = method.invoke(bean, request.getOrgs());
			return new RpcResponse(true, result);
		} catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
	}

	private Method findMethod(Class<?> aClass, String methodName) {
		for (Method method : aClass.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}

	@Autowired
	ApplicationContext context;

	private Map<String, Object> skeleton = new HashMap<>();

	@PostConstruct
	public void buildProviders() {
		Map<String, Object> providers = context.getBeansWithAnnotation(CFProvider.class);
		providers.forEach((x, y) -> System.out.println(x));
//		skeleton.putAll(providers);

		providers.values().forEach(
				x -> genInterface(x)
		);
	}

	private void genInterface(Object x) {
		Class<?> itfer = x.getClass().getInterfaces()[0];
		skeleton.put(itfer.getCanonicalName(), x);
	}

	@Bean
	ApplicationRunner providerRun() {
		return x -> {
			RpcRequest request = new RpcRequest();
			request.setService("");
			request.setMethod("");
			request.setOrgs(new Object[]{100});

			RpcResponse rpcResponse = invokeRequest(request);
			System.out.println("return: "+ rpcResponse);
		};
	}

}
