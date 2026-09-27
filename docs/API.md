# CampusHub API

接口统一返回：`{"code":200,"message":"success","data":...}`。除公开接口外，登录后在请求头携带 `Authorization: Bearer <token>`。

## 认证

| 方法 | 地址 | 角色 | 说明 |
|---|---|---|---|
| POST | `/api/users/register` | 公开 | JSON：`username,password,nickname`，注册后角色固定为 USER |
| POST | `/api/users/login` | 公开 | JSON：`username,password`，返回用户资料和 JWT |
| GET | `/api/users/me` | 已登录 | 返回当前登录用户 |
| PUT | `/api/users/me` | 已登录 | 修改昵称、手机号和头像地址 |
| POST | `/api/uploads/images` | 已登录 | 上传不超过5MB的 JPG、PNG、WEBP 或 GIF 图片 |
| GET | `/api/users/me/organizer-application` | USER | 查询自己的最近一次组织者申请 |
| POST | `/api/users/me/organizer-application` | USER | JSON：`reason`（10-500字）；提交成为组织者的申请 |

## 公开活动

| 方法 | 地址 | 角色 | 说明 |
|---|---|---|---|
| GET | `/api/activity-categories` | 公开 | 返回启用的分类 |
| GET | `/api/activities?page=1&size=10&categoryId=1&keyword=篮球` | 公开 | 仅查询 status=2；categoryId、keyword 可省略；返回 `records,total,page,size` |
| GET | `/api/activities/{id}` | 公开 | 仅允许查看 status=2 或3 |

## 组织者

角色必须是 ORGANIZER。

| 方法 | 地址 | 说明 |
|---|---|---|
| POST | `/api/organizer/activities` | JSON：title、description、coverUrl、location、categoryId、maxParticipants、registrationStartTime、registrationEndTime、startTime、endTime；创建后为待审核 status=1 |
| PUT | `/api/organizer/activities/{id}` | 修改待审核或审核拒绝的活动；拒绝活动修改后重新进入待审核 |
| GET | `/api/organizer/activities?page=1&size=10&status=1` | 查询自己的活动 |
| GET | `/api/organizer/activities/{id}` | 查询自己的活动详情 |
| GET | `/api/organizer/activities/{id}/signups?page=1&size=10&status=1` | 查询自己活动的报名名单 |
| POST | `/api/organizer/activities/{id}/cancel` | 取消自己尚未开始的待审核/报名中活动 |

## 管理员审核

角色必须是 ADMIN。

| 方法 | 地址 | 说明 |
|---|---|---|
| GET | `/api/admin/activities?page=1&size=10&status=1` | 管理员活动列表，status 可省略 |
| GET | `/api/admin/activities/{id}` | 查看任意状态活动详情 |
| POST | `/api/admin/activities/{id}/audit` | JSON：`{"auditStatus":1,"reason":"..."}`；1 通过、2 拒绝；拒绝时 reason 必填 |
| GET | `/api/admin/organizer-applications?page=1&size=10` | 查询待处理的组织者申请 |
| POST | `/api/admin/organizer-applications/{id}/review` | JSON：`approved,reason`；拒绝时 reason 必填，通过后用户角色改为 ORGANIZER |

## 学生报名和签到

角色必须是 USER。

| 方法 | 地址 | 说明 |
|---|---|---|
| POST | `/api/activities/{id}/signups` | 在报名窗口内报名；名额不足或重复报名返回409 |
| DELETE | `/api/activities/{id}/signups` | 取消自己的有效报名；取消后可在截止前重新报名 |
| GET | `/api/users/me/signups?page=1&size=10&status=1` | 查询自己的报名历史，包含 activityStatus、signupStatus、checkedIn |
| GET | `/api/users/me/signups/{activityId}/state` | 查询这场活动是否已报名、是否已签到；活动详情页据此显示操作按钮 |
| GET | `/api/users/me/signups/{activityId}/detail` | 查询自己报名过的活动详情，包括已取消或已结束的活动 |
| POST | `/api/activities/{id}/checkins` | 活动开始前30分钟至结束前签到；必须有有效报名 |

## 状态和错误码

- 活动：1待审核、2报名中、3已结束、4已取消、5审核拒绝。
- 报名：1已报名、2已取消。
- 组织者申请：1待审核、2已通过、3已拒绝。
- `400` 参数格式或校验失败；`401` 未登录或凭证无效；`403` 角色不符；`404` 资源不存在或不属于当前用户；`409` 状态、时间、名额或重复操作冲突；`500` 未预期服务器错误。

报名和取消报名在一个事务内先锁定活动行，再修改报名记录和人数，利用数据库行锁避免超卖；代价是同一活动的并发写操作会排队。
