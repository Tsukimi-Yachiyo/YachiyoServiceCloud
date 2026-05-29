# Yachiyo Service Cloud API 文档

## 1. 项目概述

Yachiyo Service Cloud 是一个基于 Spring Boot + Spring Cloud 的微服务项目，使用 Nacos 作为服务注册/配置中心。

### 服务列表

| 服务名称             | 编程语言   | 主要职责                   |
| ----------------- | ------ | ---------------------- |
| GatewayService    | Java   | API 网关，JWT 认证，统一鉴权入口   |
| UserService       | Java   | 用户认证、注册、用户信息管理         |
| ContentService    | Java   | 内容管理（帖子、专栏、评论）         |
| AdminService      | Java   | 内容审核、专栏管理              |
| CoinService       | Java   | 金币系统、签到                |
| FileService       | Java   | 文件存储、下载（MinIO）         |
| WebSocketService  | Java   | WebSocket 实时通信（多人虚拟空间） |
| AIService         | Python | AI 聊天、分析               |
| ChatService       | Python | 聊天服务                   |
| RecordService     | Go     | 记录/分数记录服务              |
| QQBotService      | Java   | QQ 机器人服务               |

---

## 2. 统一响应格式

所有接口统一返回 Result<T> 格式：

```json
{
  "code": "200",
  "message": "success",
  "data": {},
  "detail": null
}
```

### 状态码说明

| 状态码 | 说明    |
| --- | ----- |
| 200 | 成功    |
| 400 | 参数错误  |
| 401 | 未认证   |
| 403 | 无权限   |
| 404 | 资源不存在 |
| 500 | 系统错误  |

---

## 3. JWT 认证机制

### 3.1 认证说明

- Token 格式：`Authorization: Bearer <token>`
- Token 过期时间：1 小时
- 密钥：`12345678901234567890123456789012`

### 3.2 白名单路径（无需认证）

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/api/v1/auth/send-code`
- `/api/v1/auth/login-by-email`
- `/api/v1/auth/change-password`
- `/api/v1/auth/refresh-token`
- `/file/**`
- `/api/yachiyo/168/mini/admin/login`
- `/actuator/health/**`
- `/ap1/v3/**`

### 3.3 未认证响应（网关返回）

```json
{
  "error": 401,
  "message": "无效的令牌/令牌过期"
}
```

---

## 4. API 接口文档

### 4.1 用户认证接口 (UserService)

#### 4.1.1 登录

**接口**：POST /api/v1/auth/login

**功能**：使用用户名和密码登录

**请求体**：

```json
{
  "username": "testuser",
  "password": "123456"
}
```

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "detail": null
}
```

---

#### 4.1.2 注册

**接口**：POST /api/v1/auth/register

**功能**：注册新用户

**请求体**：

```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "code": "123456"
}
```

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "detail": null
}
```

---

#### 4.1.3 发送验证码

**接口**：POST /api/v1/auth/send-code

**功能**：发送验证码到邮箱

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| email | String | 是 | 邮箱地址 |

**请求示例**：`POST /api/v1/auth/send-code?email=test@example.com`

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": true,
  "detail": null
}
```

---

#### 4.1.4 邮箱验证码登录

**接口**：POST /api/v1/auth/login-by-email

**功能**：使用邮箱验证码登录

**请求体**：

```json
{
  "email": "test@example.com",
  "code": "123456"
}
```

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "detail": null
}
```

---

#### 4.1.5 刷新 Token

**接口**：POST /api/v1/auth/refresh-token

**功能**：刷新 Token

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| refreshToken | String | 是 | 刷新 Token |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "detail": null
}
```

---

#### 4.1.6 获取 WebSocket Token（需要认证）

**接口**：GET /api/v1/auth/ws-token

**功能**：获取 WebSocket 连接 Token

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": "ws-token-value",
  "detail": null
}
```

---

#### 4.1.7 登出（需要认证）

**接口**：POST /api/v1/auth/logout

**功能**：用户登出

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": true,
  "detail": null
}
```

---

#### 4.1.8 冻结账户（需要认证）

**接口**：POST /api/v1/auth/freeze

**功能**：冻结账户

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": true,
  "detail": null
}
```

---

### 4.2 用户详情接口 (UserService)

#### 4.2.1 获取用户详情（需要认证）

**接口**：GET /api/v2/user/detail/{detail_type}

**功能**：获取指定用户详情

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| detail_type | String | 是 | 用户详情类型（SELF/POSTER/SEARCH/FOLLOW/PUBLIC/FRIEND） |

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| userId | Long | 是 | 用户 ID |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": {
    "userName": "昵称",
    "userIntroduction": "介绍",
    "userCity": "城市",
    "userAvatar": "头像URL",
    "userGender": "性别",
    "userPhone": "手机号",
    "userQQ": "QQ号",
    "userMail": "邮箱",
    "userBirthday": "1990-01-01",
    "followerCount": 100,
    "followeeCount": 50,
    "isFollowing": true,
    "isFollowed": false
  },
  "detail": null
}
```

---

#### 4.2.2 搜索用户（需要认证）

**接口**：POST /api/v2/user/search

**功能**：搜索用户

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| userName | String | 是 | 用户名 |
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页大小 |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": [
    {
      "userName": "用户1",
      "userAvatar": "头像URL"
    }
  ],
  "detail": null
}
```

---

#### 4.2.3 获取关注列表（需要认证）

**接口**：GET /api/v2/user/followee

**功能**：获取当前用户关注列表

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": [1, 2, 3],
  "detail": null
}
```

---

#### 4.2.4 获取粉丝列表（需要认证）

**接口**：GET /api/v2/user/follower

**功能**：获取当前用户粉丝列表

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": [4, 5, 6],
  "detail": null
}
```

---

#### 4.2.5 关注用户（需要认证）

**接口**：POST /api/v2/user/follow

**功能**：关注用户

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| followeeId | Long | 是 | 被关注用户 ID |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": true,
  "detail": null
}
```

---

#### 4.2.6 获取自己的头像（需要认证）

**接口**：GET /api/v2/user/avatar

**功能**：获取当前用户头像

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": "头像URL",
  "detail": null
}
```

---

#### 4.2.7 更新头像（需要认证）

**接口**：PUT /api/v2/user/avatar

**功能**：更新用户头像

**请求格式**：multipart/form-data

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| avatar | File | 是 | 头像文件 |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": true,
  "detail": null
}
```

---

#### 4.2.8 更新用户详情（需要认证）

**接口**：PUT /api/v2/user/detail

**功能**：更新用户详情

**请求体**：

```json
{
  "userName": "昵称",
  "userIntroduction": "介绍",
  "userCity": "城市",
  "userAvatar": "头像URL",
  "userGender": "性别",
  "userPhone": "手机号",
  "userQQ": "QQ号",
  "userMail": "邮箱",
  "userBirthday": "1990-01-01"
}
```

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": true,
  "detail": null
}
```

---

### 4.3 帖子接口 (ContentService)

#### 4.3.1 获取帖子详情

**接口**：GET /api/v2/posting/{id}

**功能**：获取帖子详情

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| id | Long | 是 | 帖子 ID |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": {
    "content": "帖子内容",
    "filenames": ["文件名1", "文件名2"],
    "files": ["文件URL1", "文件URL2"]
  },
  "detail": null
}
```

---

#### 4.3.2 获取帖子统计

**接口**：GET /api/v2/posting/stats

**功能**：获取帖子统计信息

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| postingId | Long | 是 | 帖子 ID |

---

#### 4.3.3 搜索帖子

**接口**：GET /api/v2/posting/search

**功能**：搜索帖子

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| keyword | String | 是 | 搜索关键词 |
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页大小 |

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": [
    {
      "title": "标题1",
      "posterId": 1,
      "coverImage": "封面URL"
    }
  ],
  "detail": null
}
```

---

#### 4.3.4 获取点赞的帖子（需要认证）

**接口**：POST /api/v2/posting/like

**功能**：获取点赞帖子列表

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": [1, 2, 3],
  "detail": null
}
```

---

#### 4.3.5 获取收藏的帖子（需要认证）

**接口**：POST /api/v2/posting/collection

**功能**：获取收藏帖子列表

**成功响应**：

```json
{
  "code": "200",
  "message": "success",
  "data": [4, 5, 6],
  "detail": null
}
```

---

#### 4.3.6 获取帖子简述

**接口**：GET /api/v2/posting/encapsulate

**功能**：获取帖子简述

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| postingId | Long | 是 | 帖子 ID |

---

#### 4.3.7 获取用户的帖子

**接口**：GET /api/v2/posting/user

**功能**：获取指定用户的帖子列表

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| userId | Long | 是 | 用户 ID |

---

#### 4.3.8 帖子互动（需要认证）

**接口**：POST /api/v2/posting/interaction

**功能**：处理帖子互动（点赞/收藏/投币）

**请求体**：

```json
{
  "postingId": 123,
  "type": "LIKE",
  "action": "ADD"
}
```

**枚举说明**：

- InteractionType：LIKE（点赞）、COLLECTION（收藏）、COIN（投币）
- InteractionAction：ADD（添加）、REMOVE（移除）、TOGGLE（切换）

---

#### 4.3.9 上传帖子（需要认证）

**接口**：POST /api/v2/posting/upload

**功能**：上传新帖子

**请求格式**：multipart/form-data

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| title | String | 是 | 标题 |
| content | String | 是 | 内容 |
| type | String | 是 | 类型 |
| coverImage | File | 否 | 封面图片 |
| files | File[] | 否 | 附件文件列表 |

---

#### 4.3.10 删除帖子（需要认证）

**接口**：DELETE /api/v2/posting/{id}

**功能**：删除帖子

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| id | Long | 是 | 帖子 ID |

---

#### 4.3.11 获取我的帖子（需要认证）

**接口**：GET /api/v2/posting/my

**功能**：获取当前用户的帖子列表

---

### 4.4 评论接口 (ContentService)

#### 4.4.1 获取评论列表

**接口**：GET /api/v2/posting/comment

**功能**：获取评论列表

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| postingId | Long | 是 | 帖子 ID |

---

#### 4.4.2 添加评论（需要认证）

**接口**：PUT /api/v2/posting/comment

**功能**：添加评论

**请求体**：

```json
{
  "postingId": 123,
  "content": "评论内容"
}
```

---

#### 4.4.3 删除评论（需要认证）

**接口**：DELETE /api/v2/posting/comment

**功能**：删除评论

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| commentId | Long | 是 | 评论 ID |

---

### 4.5 专栏接口 (ContentService)

#### 4.5.1 搜索专栏

**接口**：GET /api/v2/column/search

**功能**：搜索专栏

---

#### 4.5.2 获取专栏互动状态

**接口**：GET /api/v2/column/interaction

**功能**：获取互动状态

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| columnId | Long | 是 | 专栏 ID |

---

#### 4.5.3 专栏互动（需要认证）

**接口**：PUT /api/v2/column/interaction

**功能**：专栏互动（点赞/收藏）

---

### 4.6 金币接口 (CoinService)

#### 4.6.1 获取金币（需要认证）

**接口**：GET /api/v2/coin

**功能**：获取当前用户金币余额

---

#### 4.6.2 金币交易（需要认证）

**接口**：PUT /api/v2/coin

**功能**：金币交易

---

### 4.7 签到接口 (CoinService)

#### 4.7.1 签到（需要认证）

**接口**：POST /api/v2/sign/check-in

**功能**：签到

---

#### 4.7.2 获取签到状态（需要认证）

**接口**：POST /api/v2/sign/status

**功能**：获取签到状态

---

### 4.8 管理员接口 (AdminService)

#### 4.8.1 管理员登录

**接口**：POST /api/yachiyo/168/mini/admin/login

**功能**：管理员登录

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

---

#### 4.8.2 审核帖子（需要认证）

**接口**：POST /api/yachiyo/168/mini/admin/review

**功能**：审核帖子

**请求体**：

```json
{
  "postingId": 123,
  "action": "APPROVE",
  "reason": "拒绝原因（可选）"
}
```

**审核操作**：APPROVE（审核通过）、REJECT（审核拒绝）、DELETE（删除帖子）

---

#### 4.8.3 查询帖子（需要认证）

**接口**：POST /api/yachiyo/168/mini/admin/query-postings

**功能**：查询待审核帖子

---

#### 4.8.4 添加专栏（需要认证）

**接口**：POST /api/yachiyo/168/mini/admin/add-column

**功能**：添加专栏

---

#### 4.8.5 删除专栏（需要认证）

**接口**：DELETE /api/yachiyo/168/mini/admin/delete-column

**功能**：删除专栏

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| id | Long | 是 | 专栏 ID |

---

### 4.9 文件接口 (FileService)

#### 4.9.1 下载公开文件

**接口**：GET /file/public

**功能**：下载公开文件

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| fileName | String | 是 | 文件名 |
| bucket | String | 否 | 存储桶，默认 public |

**响应**：文件流

---

#### 4.9.2 下载上传文件（需签名）

**接口**：GET /file/download/upload

**功能**：下载上传文件（需签名验证）

---

#### 4.9.3 大文件下载（需签名）

**接口**：GET /file/download/save

**功能**：大文件下载（支持断点续传，需签名验证）

---

### 4.10 测试接口 (UserService)

#### 4.10.1 测试接口

**接口**：GET /api/v3/test/hello

**功能**：测试接口

**成功响应**：

```
Hello World!
```

---

## 5. WebSocket 微服务文档

### 5.1 连接参数

| 项 | 说明 |
| ---- | ---- |
| 协议 | ws:// 或 wss:// |
| 端点 | /ws/room |
| 连接 URL 格式 | ws://host/ws/room?roomId=<roomId>&userId=<userId>&token=<token> |

### 5.2 认证流程

1. 先调用 `/api/v1/auth/ws-token` 获取 WebSocket Token
2. Token 会被保存到 Redis（Key：user:{userId}:ws-token）
3. WebSocket 连接时验证该 Token
4. 验证通过后加入房间

### 5.3 协议说明

使用 Protocol Buffers (protobuf) 二进制协议

### 5.4 OpCode 列表

| OpCode | 功能 | 方向 |
| ------ | ---- | ---- |
| 1 | 玩家移动 | Client → Server |
| 2 | 聊天 | Client ↔ Server |
| 3 | 地块交互 | Client → Server |
| 4 | 玩家加入/离开 | Server → Client |
| 5 | 玩家位置 | Server → Client |
| 100 | 帧同步广播 | Server → Client |

### 5.5 客户端连接示例（JavaScript）

```javascript
async function getWsToken() {
  const response = await fetch('/api/v1/auth/ws-token', {
    headers: { 'Authorization': 'Bearer ' + accessToken }
  });
  const data = await response.json();
  return data.data;
}

async function connectWebSocket(roomId, userId) {
  const token = await getWsToken();
  const url = `ws://localhost:8881/ws/room?roomId=${roomId}&userId=${userId}&token=${token}`;

  const ws = new WebSocket(url);

  ws.onopen = () => {
    console.log('Connected');
  };

  ws.onmessage = (event) => {
    const arrayBuffer = event.data;
    // 解析 protobuf
    console.log('Received:', arrayBuffer);
  };

  ws.onclose = (event) => {
    console.log('Disconnected:', event.code, event.reason);
  };

  return ws;
}

const ws = await connectWebSocket('room1', '123');
```

### 5.6 连接关闭状态码

| 状态码 | 说明 |
| ------ | ---- |
| 1000 | 正常关闭 |
| 1001 | 端点离开 |
| 1006 | 异常关闭 |
| 1011 | 服务器错误 |

---

## 6. 数据模型定义

### 6.1 用户详情类型枚举 (UserDetailType)

**定义位置**：UserService/src/main/java/com/yachiyo/UserService/dto/UserDetailType.java

```java
public enum UserDetailType {
    // 独立字段
    NAME, INTRODUCTION, CITY, GENDER, PHONE, QQ, BIRTHDAY, AVATAR,
    FOLLOWER_COUNT, FOLLOWEE_COUNT, IS_FOLLOWED, IS_FOLLOWING,

    // 聚合类型
    SELF(NAME, INTRODUCTION, CITY, GENDER, PHONE, QQ, BIRTHDAY, AVATAR, FOLLOWER_COUNT, FOLLOWEE_COUNT),
    POSTER(NAME, AVATAR),
    SEARCH(NAME, AVATAR, FOLLOWER_COUNT, FOLLOWEE_COUNT, IS_FOLLOWED, IS_FOLLOWING),
    FOLLOW(IS_FOLLOWED, IS_FOLLOWING),
    PUBLIC(INTRODUCTION, CITY, GENDER),
    FRIEND(NAME, AVATAR, INTRODUCTION, CITY, GENDER, PHONE, BIRTHDAY)
}
```

**类型说明**：

- SELF - 完整信息，包含所有字段
- POSTER - 发布者信息，包含昵称和头像
- SEARCH - 搜索结果，包含基本信息、头像、关注数、被关注数
- FOLLOW - 关注关系，包含是否关注、是否被关注
- PUBLIC - 公开信息，包含介绍、城市、性别
- FRIEND - 好友信息，包含完整好友信息

---

### 6.2 用户详情 DTO (UserDetailDTO)

**定义位置**：UserService/src/main/java/com/yachiyo/UserService/dto/UserDetailDTO.java

```json
{
  "userName": "昵称",
  "userIntroduction": "介绍",
  "userCity": "城市",
  "userAvatar": "头像URL",
  "userGender": "性别",
  "userPhone": "手机号",
  "userQQ": "QQ号",
  "userMail": "邮箱",
  "userBirthday": "1990-01-01",
  "followerCount": 100,
  "followeeCount": 50,
  "isFollowing": true,
  "isFollowed": false
}
```

**字段说明**：

- userName - 用户昵称（包含敏感词过滤）
- userIntroduction - 用户介绍（包含敏感词过滤）
- userCity - 用户城市（包含敏感词过滤）
- userAvatar - 用户头像
- userGender - 用户性别
- userPhone - 用户手机号
- userQQ - 用户QQ
- userMail - 用户邮箱
- userBirthday - 用户生日
- followerCount - 关注者数量
- followeeCount - 被关注者数量
- isFollowing - 是否关注
- isFollowed - 是否被关注

---

### 6.3 通用枚举

#### InteractionType 互动类型

- LIKE - 点赞
- COLLECTION - 收藏
- COIN - 投币

#### InteractionAction 互动操作

- ADD - 添加
- REMOVE - 移除
- TOGGLE - 切换

#### ReviewAction 审核操作

- APPROVE - 审核通过
- REJECT - 审核拒绝
- DELETE - 删除帖子

---

**文档创建时间**：2026-05-11
