package cn.huohuas001;

import cn.huohuas001.Events.*;
import cn.huohuas001.client.BotClient;
import cn.huohuas001.client.ServerClient;
import cn.huohuas001.tools.*;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebSocketServer extends TextWebSocketHandler {
    // BotClient 连接
    private static final Map<String, CompletableFuture<JSONObject>> responseFutureList =
            new LinkedHashMap<String, CompletableFuture<JSONObject>>(1000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > 1000; // 超出容量自动移除最旧条目
                }
            };
    // 管理活动连接
    private final Map<String, SoftReference<WebSocketSession>> activeConnections =
            new ConcurrentHashMap<>(512);
    private static final ClientManager clientManager = new ClientManager();
    private final Map<ServerRecvEvent, ServerEvent> serverEventMapping = new HashMap<>();
    private final Map<BotClientRecvEvent, BotEvent> botEventMapping = new HashMap<>();


    public WebSocketServer() {
        // Server Event
        registerServerProcess(ServerRecvEvent.heart, new Server_handleHeart(clientManager));
        registerServerProcess(ServerRecvEvent.success, new Server_handleResponeMsg(clientManager));
        registerServerProcess(ServerRecvEvent.error, new Server_handleResponeMsg(clientManager));
        registerServerProcess(ServerRecvEvent.queryWl, new Server_handleResponeWhiteList(clientManager));
        registerServerProcess(ServerRecvEvent.queryOnline, new Server_handleResponeOnlineList(clientManager));
        registerServerProcess(ServerRecvEvent.bindConfirm, new Server_handleBindConfirm(clientManager));
        registerServerProcess(ServerRecvEvent.chat, new Server_handleChat(clientManager));

        //Bot Event
        registerBotProcess(BotClientRecvEvent.SEND_MSG_BY_SERVER_ID, new Bot_handleSendPack2Server(clientManager));
        registerBotProcess(BotClientRecvEvent.QUERY_CLIENT_LIST, new Bot_handleQueryClientList(clientManager));
        registerBotProcess(BotClientRecvEvent.HEART, new Bot_handleHeart(clientManager));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        activeConnections.put(session.getId(), new SoftReference<>(session));

        // 添加10秒握手超时检测
        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
            try {
                if (session.isOpen()) {
                    ServerPackage serverPackage = clientManager.getServerPackageBySession(session);
                    BotClient botClient = clientManager.getBotClient();
                    if (serverPackage == null || !clientManager.isRegisteredServer(serverPackage.getServerId())) {
                        //log.warn("[Websocket] 客户端({})10秒内未完成握手，强制断开", session.getRemoteAddress());
                        if (botClient != null && !botClient.equals(session)) {
                            session.close(CloseStatus.NORMAL.withReason("握手超时"));
                        }
                    }
                }
            } catch (IOException e) {
                log.error("[Websocket] 关闭超时连接时出错", e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        activeConnections.remove(session.getId());
        ServerPackage serverPackage = clientManager.getServerPackageBySession(session);
        if(serverPackage != null){
            if (ClientManager.getInstance().isRegisteredServer(serverPackage.getServerId())) {
                log.info("[Websocket]  客户端({})断开连接, ServerId: {}", session.getRemoteAddress(), serverPackage.getServerId());
            }
            clientManager.unRegisterServer(serverPackage.getServerId());
        }
    }

    public static void addCallback(String id, CompletableFuture<JSONObject> callback) {
        responseFutureList.put(id, callback);
    }

    private void registerServerProcess(ServerRecvEvent eventType, ServerEvent event) {
        serverEventMapping.put(eventType, event);
    }

    private void registerBotProcess(BotClientRecvEvent eventType, BotEvent event) {
        botEventMapping.put(eventType, event);
    }

    private void handleServerMessage(WebSocketSession session, MessagePack messagePack) {
        String packId = messagePack.packId;
        String msgType = messagePack.msgType;
        JSONObject body = messagePack.body;

        ServerRecvEvent eventType = ServerRecvEvent.find(msgType);
        if (eventType == null) {
            log.error("[Websocket]  未找到对应Server事件: {}", msgType);
            return;
        }
        if (eventType == ServerRecvEvent.shakeHand) {
            ServerMsgPack msgPack = new ServerMsgPack(eventType, body, packId, null);
            Server_handleShakeHand handler = new Server_handleShakeHand(clientManager);
            handler.setData(msgPack);
            handler.shakeHandRunner(session);
            return;
        }
        ServerEvent event = serverEventMapping.get(eventType);
        if (event != null) {
            ServerClient client = clientManager.getServerPackageBySession(session).getServerClient();
            BotClient botClient = clientManager.getBotClient();
            if (botClient == null) {
                if (client != null) {
                    client.shutdown(1003, "BotClient连接出现问题，请联系机器人管理员");
                }
                return;
            }

            if (client == null && !botClient.equals(session)) {
                log.warn("[Websocket] 无效的客户端连接");
                try {
                    CloseStatus status = new CloseStatus(1000, "无效的客户端连接");
                    session.close(status);
                } catch (IOException e) {
                    log.error("[Websocket]  处理无效客户端连接时发生错误:", e);
                }
                return;
            }
            ServerMsgPack msgPack = new ServerMsgPack(eventType, body, packId, client);
            event.EventCall(msgPack);
        } else {
            log.error("[Websocket]  未找到Server处理程序: {}", msgType);
        }
    }

    private void handleBotMessage(WebSocketSession session, MessagePack messagePack) {
        String packId = messagePack.packId;
        String msgType = messagePack.msgType;
        JSONObject body = messagePack.body;

        BotClientRecvEvent eventType = BotClientRecvEvent.findByValue(msgType);
        if (eventType == null) {
            log.error("[Websocket]  未找到对应Bot事件: {}", msgType);
            return;
        }
        if (eventType == BotClientRecvEvent.SHAKE) {
            BotMsgPack msgPack = new BotMsgPack(eventType, body, packId);
            Bot_ShakeHand shakeHandEvent = new Bot_ShakeHand(clientManager);
            shakeHandEvent.setData(msgPack);
            shakeHandEvent.runShake(session);
            return;
        }
        BotEvent event = botEventMapping.get(eventType);
        if(event != null) {
            BotMsgPack msgPack = new BotMsgPack(eventType, body, packId);
            event.EventCall(msgPack);
        }else{
            log.error("[Websocket]  未找到Bot处理程序: {}", msgType);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        try {
            Map<String, Enum<?>> eventMapping = new HashMap<>();
            for (ServerRecvEvent event : ServerRecvEvent.values())  {
                eventMapping.put(event.name(),  event);
            }
            for (BotClientRecvEvent event : BotClientRecvEvent.values())  {
                eventMapping.put(event.getValue(),  event);
            }

            JSONObject data = JSONObject.parseObject(payload);
            JSONObject header = data.getJSONObject("header");
            JSONObject body = data.getJSONObject("body");
            String msgType = header.getString("type");
            String packId = header.getString("id");

            //封包
            MessagePack messagePack = new MessagePack(packId, msgType, body);

            //执行回调消息
            if (responseFutureList.containsKey(packId)) {
                log.debug("[Websocket]  收到response消息: {}", payload);
                log.debug("处理事件回调 {}", packId);
                CompletableFuture<JSONObject> responseFuture = responseFutureList.get(packId);
                if (responseFuture != null && !responseFuture.isDone()) {
                    responseFuture.complete(body);
                }
                responseFutureList.remove(packId);
                return;
            }

            //判断是否是Bot发过来的消息
            if (msgType.startsWith("BotClient.")) {
                handleBotMessage(session, messagePack);
            } else {
                handleServerMessage(session, messagePack);
            }
        } catch (Exception e) {
            log.error("[Websocket]  处理消息时发生错误", e);
        }
    }
}