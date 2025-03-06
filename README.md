
# CF-RPC
**CF-RPC** 是一个基于Java开发的远程过程调用（RPC）组件，旨在为开发者提供一个简单、高效且功能丰富的RPC解决方案。该框架支持HTTP和Netty两种远程调用方式，并集成了负载均衡、注册中心、灰度发布、故障隔离、流量控制等高级功能，适用于构建高性能、可扩展的分布式系统。

## 特性

- **自定义注解**：通过自定义注解轻松定义和注册RPC服务。
- **动态代理**：消费端使用动态代理实现透明的远程调用，隐藏网络通信细节。
- **支持方法重载**：支持处理具有相同方法名但参数不同的方法。
- **JSON序列化**：使用JSON作为数据交换格式，轻量且易于调试。
- **支持多种数据类型**：支持Java基础类型（如int、String）以及复杂类型（如数组、List、Map）作为参数。
- **负载均衡与注册中心**：集成负载均衡策略和服务注册中心，支持高可用性部署。
- **灰度发布**：支持灰度发布功能，实现服务的平滑升级。
- **故障隔离与恢复**：具备故障隔离和自动恢复能力，提升系统可靠性。
- **接口级流量控制**：支持接口级别的流控，防止服务过载。
- **多种传输协议**：支持HTTP和Netty两种传输方式，可通过配置文件灵活切换。
- **测试支持**：提供丰富的测试案例，确保组件的稳定性和功能完整性。

## 快速开始

### 安装

（此处建议补充具体的安装方法，例如从Maven仓库引入依赖或从GitHub克隆源码编译。例如：）


```xml
<!-- 示例：在Maven项目中添加依赖 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>java-rpc-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 定义服务

使用自定义注解 @RpcService 定义一个RPC服务接口：


```java
@RpcService
public interface HelloService {
    String sayHello(String name);
}
```

### 实现服务

实现服务接口并使用 @RpcServiceImpl 注解标记服务实现类：

java

CollapseWrapCopy

```java
@RpcServiceImpl
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
```

### 消费服务

在消费端通过动态代理获取服务实例并调用：

```java
public class Client {
    public static void main(String[] args) {
        // 创建代理对象
        HelloService helloService = RpcProxy.create(HelloService.class);
        // 调用远程服务
        String result = helloService.sayHello("World");
        System.out.println(result); // 输出: Hello, World
    }
}
```
### 配置传输协议

在配置文件（如 rpc.properties）中指定使用HTTP或Netty作为传输协议：

```yaml
rpc.transport=netty  # 可选值: http 或 netty
rpc.server.host=localhost
rpc.server.port=8080
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

## 详细文档

（建议补充更详细的配置说明、API文档或完整示例代码。例如负载均衡策略配置、注册中心集成方式等。）

## 贡献指南

欢迎对本项目提出建议或贡献代码！请按照以下步骤参与：

1. Fork 本仓库。
2. 创建你的功能分支（git checkout -b feature/AmazingFeature）。
3. 提交你的更改（git commit -m 'Add some AmazingFeature'）。
4. 推送到远程分支（git push origin feature/AmazingFeature）。
5. 创建一个Pull Request。

## 许可证

（建议指定项目的许可证，例如：）

本项目采用 [MIT许可证](https://opensource.org/licenses/MIT) 发布。

## 联系方式

- **作者**: bigwillc
- **邮箱**: [cfh870993074@outlook.com](mailto:cfh870993074@outlook.com)
- **项目地址**: https://github.com/bigwillc/cf-rpc