# CampusHub 前端

这是 CampusHub 的 Vue 3 + Vite 前端，开发服务器会把 `/api` 请求代理到正在运行的 Spring Boot 后端 `http://localhost:8080`。

## 启动

先在 IDEA 中启动 `CampusHubApplication`，再打开一个终端：

```powershell
cd D:\JavaProjects\campus-hub\campus-hub\frontend
npm install
npm run dev
```

浏览器打开终端显示的地址，默认是 `http://localhost:5173`。

## 已接入的功能

- 活动公开列表、关键词和分类筛选、分页
- 活动详情和学生报名/取消报名
- 注册、登录、退出登录和角色展示
- 学生“我的报名”
- 组织者发布活动、查看自己的活动和报名名单、取消活动
- 管理员查看活动并通过或拒绝审核
