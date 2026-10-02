# CampusHub

CampusHub 是一个面向高校学生、活动组织者和管理员的校园活动管理平台，包含 Spring Boot 后端和 Vue 3 网页前端。

## 项目功能

| 使用者 | 可以做什么 |
| --- | --- |
| 未登录访客 | 查看、分页和筛选公开活动，查看活动详情 |
| 学生 | 注册登录、报名/取消报名、查看“我的报名”、在规定时间签到 |
| 组织者 | 创建活动、管理自己发布的活动、取消活动、查看报名名单 |
| 管理员 | 查看全部活动，审核通过或拒绝待审核活动 |

业务流程：**组织者创建活动 → 管理员审核 → 学生查看并报名 → 临近开始时签到 → 系统自动结束过期活动**。

## 项目特色

- `USER`、`ORGANIZER`、`ADMIN` 三种角色各有独立工作台和接口权限。
- JWT 登录认证，密码使用 BCrypt 加密保存。
- 活动支持分页、分类和关键词筛选。
- 报名使用事务和数据库行锁，避免并发超卖。
- 定时维护活动状态，统一处理参数错误、未登录、无权限和业务错误。
- Vue 页面适配电脑和手机，主要功能都能直接在网页操作。
- 支持活动封面和用户头像本地上传、个人资料维护。
- 学生报名、组织者活动、报名名单和管理员审核列表均支持分页。

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
- [x] 注册、登录、BCrypt 密码加密、JWT 当前用户接口
- [x] 分类列表和统一错误响应
- [x] 公开活动分页、筛选、详情
- [x] 组织者发布、管理、取消活动和查看报名名单
- [x] 管理员审核活动
- [x] 学生报名、取消报名、我的报名和签到
- [x] 活动结束状态定时更新
- [x] 学生申请成为组织者、管理员审核和拒绝原因反馈
- [x] 角色权限、事务和活动行锁防超卖

## 第一次运行

项目通过环境变量读取数据库密码，不在仓库中保存敏感信息。

1. 在 MySQL Workbench 中依次执行 `sql/schema.sql`、`sql/seed.sql`。`seed.sql` 会创建基础分类和 3 条可直接浏览的示例活动；示例活动归属于一个已禁用登录的系统账号，不会开放管理员权限。
2. 在 IDEA 的运行配置中设置：

```text
CAMPUS_HUB_DB_PASSWORD=你的本地 MySQL 密码
CAMPUS_HUB_JWT_SECRET=至少32字节、标准Base64编码的密钥
```

3. 启动 `CampusHubApplication`。
4. 按下方命令启动前端，在浏览器打开 `http://localhost:5173`。

如果数据库已经初始化过，重新执行 `seed.sql` 即可补充示例活动，不会删除真实用户或已有活动。在 Docker 部署的服务器项目目录中可执行：

```bash
docker compose exec -T mysql sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" campus_hub' < sql/seed.sql
```

执行后刷新网站首页；新访客注册并登录后即可浏览这些活动。

接口清单见 [docs/API.md](docs/API.md)。公开接口可以直接访问；报名、签到、组织者和管理员接口需要登录并使用对应角色。

## 本地演示账号和数据

以下是公开的本地演示账号，不是系统自动创建的初始账号。第一次运行时，先启动后端和前端，再在网页注册这三个账号；已有账号不要重复注册。普通注册得到的账号一律是学生（`USER`）。

| 注册用户名 | 注册密码 | 执行演示脚本后的角色 |
| --- | --- | --- |
| `student001` | `123456` | 学生（`USER`） |
| `test_org` | `123456` | 组织者（`ORGANIZER`） |
| `admin_test` | `Test123456` | 管理员（`ADMIN`） |

注册完成后，在 MySQL Workbench 执行 `sql/demo.sql`。脚本不会创建账号或密码；它会把已注册账号设置为表中对应角色，并创建：

- 3 个可以查看和报名的公开活动；
- 1 个学生已报名、可以立即测试签到的活动；
- 1 个供管理员测试审核的待审核活动。

该脚本可以重复执行。如果演示活动以后过期，再执行一次即可刷新关键活动时间。修改角色后，请退出并重新登录对应账号，让新登录凭证带上最新角色。

如果三个账号已经存在并且角色已经设置好，也可以在后端运行时直接执行下面的命令。它通过真实接口创建、审核并报名活动，不需要打开数据库：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\setup-demo.ps1
```

登录窗口不内置测试账号和密码。演示账号和密码写在仓库里，任何能访问仓库的人都能看到，因此只能用于本地学习和演示；不要把这些账号用于真实部署或保存真实用户数据。正式部署时应由部署者通过受控方式单独创建管理员并设置新密码；普通用户注册仍是学生，组织者通过页面申请并由管理员审核。

## 三种账号怎么使用

### 学生

公开注册得到的都是学生账号（`USER`）。登录 `student001` → 在“发现活动”查看详情并报名 → 打开“我的报名”查看或取消报名。`[演示] 即时签到测试活动` 会显示“立即签到”。

学生想发布活动时，进入“个人中心”，填写理由并提交“申请成为组织者”。管理员通过后刷新页面，账号会变成组织者；拒绝时，个人中心会显示管理员填写的原因，修改说明后可以再次申请。

### 组织者

登录 `test_org` → 打开“组织者工作台” → 发布活动。新活动先显示“待审核”；还可以查看自己的活动、报名名单和取消活动。

### 管理员

登录 `admin_test` → 打开“审核管理” → 可以审核组织者申请和待审核活动。组织者申请通过后，该学生获得发布活动权限；活动审核通过后，学生才能在公开列表中看到该活动。

## 前端运行

前端代码位于 `frontend`，使用 Vue 3 + Vite。先保持后端运行，再打开新的终端执行：

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问终端显示的 `http://localhost:5173`。Vite 会把前端的 `/api` 请求代理到后端 `http://localhost:8080`。

## 阿里云 ECS 部署（Docker）

仓库已提供 `docker-compose.yml`，会启动 MySQL、Spring Boot 后端和 Nginx 前端。公网只开放网页的 80 端口；MySQL 和后端端口不映射到公网。数据库和用户上传的图片保存在 Docker 卷中，重建容器不会清空它们。

### 1. 启动 ECS 并配置安全组

在阿里云 ECS 控制台启动实例，等待状态变成“运行中”。安全组只添加：

- TCP 80：来源 `0.0.0.0/0`，供浏览器访问网页。
- TCP 22：来源设为“我的 IP”，用于 SSH；如果使用阿里云 Workbench，也按控制台提示配置。

不要开放 3306（MySQL）或 8080（后端）。当前通过公网 IP 的 HTTP 适合演示和测试，不要在未配置 HTTPS 前放真实个人数据。

### 2. 连接服务器并下载项目

通过阿里云 Workbench 或 SSH 登录 Ubuntu，然后逐行执行：

```bash
sudo apt update
sudo apt install -y git
git clone https://github.com/Qw886/campus-hub.git
cd campus-hub
cp .env.example .env
openssl rand -hex 24
openssl rand -hex 24
openssl rand -base64 48
nano .env
```

将这三条随机命令输出的三串内容，分别填入 `.env` 中对应的数据库 root 密码、应用数据库密码和 JWT 密钥；删掉 `REPLACE_WITH...` 占位文字后保存。`nano` 保存：按 `Ctrl+O`、回车，再按 `Ctrl+X`。不要把 `.env` 发到聊天、截图或提交到 GitHub。

### 3. 构建并启动

仍在 `campus-hub` 目录执行：

```bash
docker compose config -q
docker compose --parallel 1 up -d --build
docker compose ps
```

首次构建需要下载基础镜像和 Maven、Node 依赖，可能要等几分钟。等 `mysql` 显示 `healthy`，然后在浏览器打开 `http://你的ECS公网IP`。当前实例公网 IP 是 `47.110.79.243` 时，访问 `http://47.110.79.243`。

要查看后端启动情况：

```bash
docker compose logs -f backend
```

看到 Spring Boot 启动完成后按 `Ctrl+C` 退出日志查看（不会停止服务）。后续更新代码时，在项目目录执行 `git pull`，然后执行 `docker compose up -d --build`。

### 4. 创建第一个管理员

网页注册一个你自己选的用户名和密码（普通注册默认是学生）。然后在服务器项目目录执行 `docker compose exec mysql mysql -u root -p campus_hub`，按提示输入 `.env` 中的 `MYSQL_ROOT_PASSWORD`。进入 MySQL 后执行下面语句，把用户名换成刚注册的用户名：

```sql
UPDATE sys_user SET role = 'ADMIN' WHERE username = '你的管理员用户名';
```

执行 `exit` 退出数据库，再从网页退出并重新登录该账号；重新登录后才会拿到管理员权限。不要在生产服务器导入包含演示账号的 `sql/demo.sql`。以后组织者通过网页申请，管理员在管理页面审核；学生可自行注册测试报名。

### 常用维护

```bash
docker compose ps                 # 查看服务状态
docker compose logs --tail=100    # 查看最近日志
docker compose restart backend    # 重启后端
docker compose down               # 停止服务，保留数据库和上传图片
```

不要执行 `docker compose down -v`，`-v` 会删除数据库和图片数据卷。试用额度有限；暂时不用时可在 ECS 控制台停止实例，继续体验前再启动。

活动状态：1待审核、2报名中、3已结束、4已取消、5审核拒绝。报名和取消报名在一个事务内先锁定活动行，避免并发超卖；代价是同一活动的并发写操作会排队。

项目按 Asia/Shanghai 本地时间处理活动时间，部署时应用与数据库时区应保持一致。生产环境应使用受限数据库账号、HTTPS、备份和日志脱敏。

## 验证

```powershell
mvn -B test
```

测试不依赖开发库；报名并发、权限和完整主线应在独立测试库用三个演示角色实际联调。Git 推送不是项目运行前置条件。
