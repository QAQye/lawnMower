### JAVA项目整理

## 第一版结构：

├───ai
│   ├───controller
│   ├───dto
│   └───service
├───auth
│   ├───controller
│   ├───entity
│   ├───mapper
│   ├───security
│   └───service
│       └───impl
├───common
│   ├───exception
│   ├───response
│   └───util
├───infrastructure
│   └───config
└───robot
    ├───service
    ├───stream
    │   ├───controller
    │   └───service
    └───websocket

- `auth`：登录注册与 JWT
- `ai`：AI 问答
- `robot`：小车控制、推流、设备通信
- `infrastructure`：配置
- `common`：真正公共的类

## 大致分类方案

+ 接口入口 → `controller`

+ 数据传输 → `dto`

+ 数据库实体 → `entity`

+ 数据访问 → `mapper`

+ 业务逻辑 → `service`

+ 安全校验 → `security`

+ WebSocket 通信 → `websocket`

+ 系统配置 → `infrastructure/config`

### ai功能包



├───ai
│   ├───controller
│   ├───dto
│   └───service

这个模块就是专门处理“和 AI 聊天、调用大模型、返回回答”这一类事情的

#### 基本工作流程：

![image-20260314215231038](C:\Users\24861\AppData\Roaming\Typora\typora-user-images\image-20260314215231038.png)

#### ai/controller

这里放 **对外暴露的接口层**。

前端发一个问题给后端

后端提供 `/ai/chat`、`/ai/ask` 这样的接口

这个接口先接住请求，再把事情交给 service

#### ai/dto

这里放 **AI 模块自己的请求体和响应体对象**。

DTO 可以理解成：
 **前后端传数据时用的小盒子。**

前端传过来一句话：`"帮我规划路线"`

你就可以用一个 `UserQuery` 类接收

你要调用 DeepSeek 或别的模型时，也可能需要一个 `DeepSeekRequest``

#### ai/service

这里放 **AI 模块自己的请求体和响应体对象**。

DTO 可以理解成：
 **前后端传数据时用的小盒子。**

前端传过来一句话：`"帮我规划路线"`

你就可以用一个 `UserQuery` 类接收

你要调用 DeepSeek 或别的模型时，也可能需要一个 `DeepSeekRequest`

这里放 **AI 模块真正的业务逻辑**。

- 拼接 prompt
- 调第三方大模型接口
- 处理流式返回
- 对结果做简单加工
- 捕获异常并返回统一结果

你可以把 service 理解成：

> **service 是“干活的人”。**

controller 接到请求后，不应该自己去调用第三方接口，而是交给 service。

### auth 包：认证授权模块

#### 基本工作流程：

![image-20260314213821252](C:\Users\24861\AppData\Roaming\Typora\typora-user-images\image-20260314213821252.png)

auth
├── controller
├── entity
├── mapper
├── security
└── service
    └── impl

这个模块就是负责：

- 注册
- 登录
- 查用户
- JWT 生成和校验
- Spring Security 身份认证

#### auth/controller

这里放认证相关接口。

+ 接收用户名密码

+ 调用 service

+ 返回 token 或提示信息

#### auth/entity

这里放 **和数据库表对应的实体类**。

比如数据库里有 `user` 表，那么这里一般会有：

- `User`

entity 可以理解成：

> **数据库里一条记录，在 Java 里的样子。**

#### auth/mapper

这里放数据库访问层。`

它的职责是：

- 查用户
- 插入用户
- 更新用户
- 根据用户名查找

可以把 mapper 理解成：

> **专门和数据库说话的那一层。**

#### auth/security

这里放认证安全相关的类。

比如：

- `JwtUtil`：生成和解析 JWT
- `JwtAuthFilter`：请求进来时拦截并校验 token

#### auth/service

这里放认证业务接口。

它负责定义“我要做什么”，例如：

- 登录
- 注册
- 根据用户名查询用户

#### auth/service/impl

这里放 service 的实现类。

### common包：公共通用模块

common
├── exception
├── response
└── util

#### common/exception

这里放公共异常相关类。

比如：

- 自定义异常 `BusinessException`
- 全局异常处理器 `GlobalExceptionHandler`

#### common/util

这里放通用工具类。

比如：

- 字符串工具
- 时间工具
- JWT 之外的通用工具
- 文件名生成工具
- IP/路径处理工具

### infrastructure 包：基础设施层

```
infrastructure
└── config
```

它放的是：

> **让项目能正常跑起来的底层支撑代码。**

包括底层的配置

### robot 包：小车/设备通信模块

```
robot
├── service
├── stream
│   ├── controller
│   └── service
└── websocket
```

主要管：

+ 小车控制

+ 视频流

+ WebSocket 实时通信

+ 设备状态交互

#### robot/service

这里放 **robot 模块的公共业务逻辑**。

不专门属于 stream，也不单纯属于 websocket 的那部分逻辑，可以放这里

这些 service 处理的是：

- 发送控制命令
- 维护机器人状态
- 管理连接
- 做业务编排

#### robot/stream

这个子包专门负责“视频流/图像流”相关功能。

这是个很好的拆法，因为 stream 和 control 虽然都属于 robot，但关注点不一样。

##### robot/stream/controller

这里放视频流相关接口。

控制器负责接收前端请求，比如：

- 请求开始推流
- 查看流状态
- 获取当前流信息

##### robot/stream/service

这里放视频流业务逻辑。

比如：

- 建立视频流连接
- 推送图像帧
- 广播给前端
- 重连
- 管理流状态

#### robot/websocket

这里放 WebSocket 相关处理器。

它的职责通常是：

- 建立 WebSocket 连接
- 接收前端发来的消息
- 把消息交给 service
- 把结果推送回去
- 处理连接关闭

websocket 包负责“通信入口”，service 包负责“业务处理”

