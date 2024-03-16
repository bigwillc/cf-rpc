# CF-RPC
## 1. Introduction
设计一个远程RPC调用框架，支持多种序列化方式

## 设计前
- 需求
- 拓展性
- 高可用
- 性能
- 安全
- 可维护性
- 通用性
- 灵活性
- 用户体验
- 数据管理
- 成本
- 技术栈
- 团队协作

### CF-RPC-CORE
- 定义了request和response的数据结构
- 定义了rpc的接口

### 开发日志
- [x] 定义request和response 数据结构
- [x] 加载自定义注解的服务
- [x] 启动时自定义注解加载代理对象
- [x] 动态代理实现消费端的http请求
- [x] 支持方法重载 
- [x] json 序列化
- [x] 处理基础类型参数
- [ ] 处理数组、List、Map的类型
- [ ] 支持接口多个实现类调用
- [ ] 压力测试
- [ ] 支持netty实现远程调用


为什么要做类型转换？
因为RpcRequest中的args是Object类型，需要根据方法的参数类型进行转换；

为什么要

通过扫描@CFConsumer 和 @CFProvider 注解，自动注册服务

## 数据结构
### RpcRequest
service：请求服务
method：请求方法
args：参数
### RpcResponse
status: 请求是否成功
data：请求响应数据
exception：异常

