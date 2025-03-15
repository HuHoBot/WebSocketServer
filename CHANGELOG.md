# HuHoBot WebSocket v1.0.2

feat(client): 增加心跳检测功能并优化连接管理

- 在 BaseClient 中添加心跳检测相关方法
- 在 ClientManager 中实现定时心跳检测逻辑
- 优化 ServerClient 的关闭连接方法
- 更新 handleHeart 和 handleShakeHand 事件处理
- 调整 WebSocketServer 中的连接管理