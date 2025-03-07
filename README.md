
# CF-RPC
**CF-RPC** 是一个基于Java开发的远程过程调用（RPC）组件，旨在为开发者提供一个简单、高效且功能丰富的RPC解决方案。该框架支持HTTP和Netty两种远程调用方式，并集成了负载均衡、注册中心、灰度发布、故障隔离、流量控制等高级功能，适用于构建高性能、可扩展的分布式系统。

## 特性

- **自定义注解**：通过自定义注解轻松定义和注册RPC服务。
- **动态代理**：消费端使用动态代理实现透明的远程调用，隐藏网络通信细节。
- **支持方法重载**：支持处理具有相同方法名但参数不同的方法。
- **JSON序列化**：使用JSON作为数据交换格式，轻量且易于调试。
- **负载均衡与注册中心**：集成负载均衡策略和服务注册中心，支持高可用性部署。
- **灰度发布**：支持灰度发布功能，实现服务的平滑升级。
- **故障隔离与恢复**：具备故障隔离和自动恢复能力，提升系统可靠性。
- **接口级流量控制**：支持接口级别的流控，防止服务过载。
- **多种传输协议**：支持HTTP和Netty两种传输方式，可通过配置文件灵活切换。

## 快速开始
```bash
cf-rpc  # 根目录（Maven 多模块项目）
├── .idea  # IntelliJ IDEA 项目配置文件夹
├── cf-rpc-core  # 核心模块，可能包含核心业务逻辑或通用组件
├── cf-rpc-demo-api  # API 模块，通常用于定义接口
├── cf-rpc-demo-consumer  # 消费者模块，可能是客户端调用方
├── cf-rpc-demo-provider  # 服务提供者模块，可能是服务器端
├── doc  # 文档文件夹
├── .gitignore  # Git 忽略文件配置
├── pom.xml  # 父类 Maven 配置文件（父 POM）
└── README.md  
```
cf-rpc-demo-consumer 模块提供测试用例，可以用于演示CF-RPC的使用方法。

项目启动依赖zookeeper; 注册中心功能下次实现。
### 定义服务

定义一个RPC服务接口：


```java
public interface UserService {
    String hello(String name);
}
```

### 实现服务

实现服务接口并使用 @RpcServiceImpl 注解标记服务实现类：

```java
@Component
@CFProvider
public class UserServiceImpl implements UserService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }
    
    // 其他方法
}
```

### 消费服务

在消费端通过动态代理获取服务实例并调用：

```java
@SpringBootApplication
@Import({ConsumerConfig.class})
@RestController
@Slf4j
public class CfRpcDemoConsumerApplication {
    
    @CFConsumer
    UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(CfRpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumer_runner() {
        return x -> {
            allTest();
        };
    }

    private void allTest() {
        String result = userService.hello("world");
        log.info("result: {}", result);
        
        // 其他测试示例
    }
}
```
### 配置传输协议

在配置文件（如 application.yaml）中指定使用HTTP或Netty作为传输协议：

```yaml
cfrpc:
  zkServer: 127.0.0.1:2181
  zkRoot: cfrpc
  protocol: netty  #protocol: http，netty模式使用
  netty:
    port: 8090 #netty模式使用，http 方式无需配置netty.port
```
启动服务端和客户端即可完成远程调用。

## 性能测试

以下是在本地Mac环境下（单机运行1个客户端和1个服务提供者）使用 wrk 工具进行的压力测试结果，测试时长为30秒。测试展示了HTTP和Netty两种传输协议在不同并发连接数下的性能表现。

| 传输协议      | 并发连接数 | 吞吐量 (请求/秒) | 平均延迟 (ms) | 最大延迟 (ms) | 50%延迟 (ms) | 90%延迟 (ms) | 99%延迟 (ms) |
| --------- | ----- | ---------- | -------- | --------- | ---------- | ---------- | ---------- |
| **HTTP**  | 10    | 13,876.11  | 15.32    | 1,032.13  | 0.38       | 0.59       | 665.66     |
|           | 25    | 5,158.37   | 37.05    | 1,012.16  | 0.78       | 2.01       | 866.49     |
|           | 50    | 2,740.97   | 29.51    | 1,527.00  | 1.91       | 4.04       | 820.05     |
| **Netty** | 10    | 26,486.93  | **0.35**     | **62.58**     | **0.29**       | **0.38**       | **0.77**       |
|           | 25    | 34,987.19  | 0.80     | 47.60     | 0.64       | 0.94       | 4.44       |
|           | 50    | **36,161.66**  | 1.37     | 29.49     | 1.25       | 1.93       | 3.72       |


**分析**：Netty传输协议在高并发场景下表现出色，相比HTTP具有更低的延迟和更高的吞吐量，适合对性能要求较高的应用场景。

**测试方法**：

命令：wrk -t8 -c50 -d30s -s wrk_benchmark.lua http://localhost:8080

脚本：
[wrk_benchmark.lua](cf-rpc-demo-consumer%2Fsrc%2Fmain%2Fresources%2Fwrk_benchmark.lua)

## 详细文档

（待补充更详细的配置说明、API文档或完整示例代码。例如负载均衡策略配置、注册中心集成方式等。）

## 贡献指南

欢迎对本项目提出建议或贡献代码！

## 许可证

（建议指定项目的许可证，例如：）

本项目采用 [MIT许可证](https://opensource.org/licenses/MIT) 发布。

## 联系方式

- **作者**: bigwillc
- **邮箱**: [cfh870993074@outlook.com](mailto:cfh870993074@outlook.com)
- **项目地址**: https://github.com/bigwillc/cf-rpc