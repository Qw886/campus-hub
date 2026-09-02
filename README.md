# CampusHub

CampusHub 是一个面向高校学生、活动组织者和管理员的校园活动管理平台后端项目。

项目以 Java 后端能力为主线，从基础业务版本开始，逐步实现活动发布、审核、报名、取消报名和签到，并在后续版本中引入权限、缓存与并发优化。

## 技术栈

- Java 17
- Spring Boot 3.5.16
- Maven
- MySQL
- MyBatis-Plus 3.5.17
- Jakarta Validation
- Lombok

## 当前进度

- [x] Spring Boot 项目初始化
- [x] MySQL 数据源配置
- [x] MyBatis-Plus 集成
- [x] `sys_user` 实体与基础查询链路
- [x] 统一接口响应结构
- [x] 注册参数 DTO 与校验规则
- [ ] 用户注册与登录
- [ ] 活动发布、查询与审核
- [ ] 活动报名与取消报名
- [ ] 活动签到
- [ ] 权限、缓存与并发优化

## 本地运行

项目通过环境变量读取数据库密码，不在仓库中保存敏感信息。

在运行配置中设置：

```text
CAMPUS_HUB_DB_PASSWORD=你的本地 MySQL 密码
```

然后启动 `CampusHubApplication`。

当前开发阶段可通过以下接口验证数据库查询链路：

```http
GET /test/users
```

> 数据库初始化脚本和完整接口文档会随着业务模块开发逐步补充。
