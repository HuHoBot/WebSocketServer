package cn.huohuas001;

import cn.huohuas001.Events.*;
import cn.huohuas001.client.BotClient;
import cn.huohuas001.client.ServerClient;
import cn.huohuas001.tools.ClientManager;
import cn.huohuas001.tools.ServerMsgPack;
import cn.huohuas001.tools.ServerPackage;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class WebSocketServer extends TextWebSocketHandler {
    // 管理活动连接
    private final Set<WebSocketSession> activeConnections = new CopyOnWriteArraySet<>();
    // BotClient 连接

    private static final Map<String, CompletableFuture<JSONObject>> responseFutureList = new HashMap<>();
    private static final ClientManager clientManager = new ClientManager();
    private final Map<Enum<?>,BaseEvent> eventMapping = new HashMap<>();

    public WebSocketServer() {
        registerProcess(ServerRecvEvent.heart,new handleHeart(clientManager));
        registerProcess(ServerRecvEvent.success,new handleResponeMsg(clientManager));
        registerProcess(ServerRecvEvent.error,new handleResponeMsg(clientManager));
        registerProcess(ServerRecvEvent.queryWl,new handleResponeWhiteList(clientManager));
        registerProcess(ServerRecvEvent.queryOnline,new handleResponeOnlineList(clientManager));
        registerProcess(ServerRecvEvent.bindConfirm,new handleBindConfirm(clientManager));
        registerProcess(BotClientRecvEvent.SEND_MSG_BY_SERVER_ID,new handleBotSendPack2Server(clientManager));
        registerProcess(BotClientRecvEvent.QUERY_CLIENT_LIST,new handleBotQueryClientList(clientManager));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeConnections.add(session);
        log.info("[Websocket]  新客户端({})连接,当前连接数:{}", session.getRemoteAddress(), activeConnections.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        activeConnections.remove(session);
        ServerPackage serverPackage = clientManager.getServerPackageBySession(session);
        if(serverPackage != null){
            clientManager.unRegisterServer(serverPackage.getServerId());
            log.info("[Websocket]  客户端({})断开连接, ServerId: {},当前连接数: {}", session.getRemoteAddress(), serverPackage.getServerId(), activeConnections.size());
        }
    }

    public static void addCallback(String id, CompletableFuture<JSONObject> callback) {
        responseFutureList.put(id, callback);
    }

    private void registerProcess(Enum<?> eventType,BaseEvent event){
        eventMapping.put(eventType,event);
    }

    private void handleProcess(ServerMsgPack serverMsgPack){
        BaseEvent event = eventMapping.get(serverMsgPack.getType());
        if(event != null) {
            event.EventCall(serverMsgPack);
        }else{
            log.error("[Websocket]  未找到处理程序: {}", serverMsgPack.getType());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
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

            Enum<?> eventType = eventMapping.get(msgType);

            if (eventType == null) {
                eventType = ServerRecvEvent.unknown;
            }
            ServerMsgPack serverMsgPack;

            if (responseFutureList.containsKey(packId)) {
                log.debug("[Websocket]  收到response消息: {}", payload);
                log.debug("处理事件回调", packId);
                CompletableFuture<JSONObject> responseFuture = responseFutureList.get(packId);
                if (responseFuture != null && !responseFuture.isDone()) {
                    responseFuture.complete(body);
                }
                responseFutureList.remove(packId);
                return;
            }

            if(eventType != ServerRecvEvent.heart){
                log.debug("[Websocket]  收到消息: {}", payload);
                log.debug("处理事件 {}", eventType);
            }

            if(eventType == ServerRecvEvent.shakeHand){
                serverMsgPack = new ServerMsgPack(eventType, body, packId,null);
                handleShakeHand handler = new handleShakeHand(clientManager);
                handler.setData(serverMsgPack);
                handler.shakeHandRunner(session);
                return;
            }

            ServerClient client = clientManager.getServerPackageBySession(session).getServerClient();
            BotClient botClient = clientManager.getBotClient();
            if(botClient == null){
                if (client != null) {
                    client.shutdown(1003, "BotClient连接出现问题，请联系机器人管理员");
                }
                return;
            }

            if(client == null && !botClient.equals(session)){
                return;
            }


            serverMsgPack = new ServerMsgPack(eventType, body, packId,client);
            handleProcess(serverMsgPack);

        } catch (Exception e) {
            log.error("[Websocket]  处理消息时发生错误", e);
        }
    }
}