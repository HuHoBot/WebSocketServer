# HuHoBot WebSocket v1.1.0

refactor(client): 重构客户端连接和消息处理机制

- 新增 ClientType 枚举，用于区分不同类型的客户端
- 重构 BaseClient 类，增加 clientType 属性
- 新增 BotMsgPack 类，用于封装 Bot 客户端的消息包
- 新增 MessagePack 类，用于封装消息包
- 新增 MessageTarget 枚举，用于区分消息目标
- 重构 BotClient 和 ServerClient 类，使用新的消息包结构
- 新增心跳处理和重新握手逻辑
- 优化线程池和连接管理