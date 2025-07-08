package cn.huohuas001.client;

import cn.huohuas001.Events.BotClientSendEvent;
import cn.huohuas001.WebSocketServer;
import cn.huohuas001.tools.Enums.ClientType;
import cn.huohuas001.tools.PackId;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class BotClient extends BaseClient{

    public BotClient(WebSocketSession session) {
        super(session, ClientType.Bot);
    }

    public boolean sendMessage(BotClientSendEvent type, JSONObject body) {
        String packId = PackId.getPackID();
        return sendMessage(type, body,packId);
    }

    public boolean sendMessage(BotClientSendEvent type, JSONObject body, String packId) {
        return baseSendMessage(type.getValue(), body, packId);
    }

    public CompletableFuture<JSONObject> sendRequestAndAwaitResponse(BotClientSendEvent type, JSONObject body) {
        String packId = PackId.getPackID();
        return sendRequestAndAwaitResponse(type, body, packId);
    }

    public CompletableFuture<JSONObject> sendRequestAndAwaitResponse(BotClientSendEvent type, JSONObject body, String packId) {
        CompletableFuture<JSONObject> responseFuture = new CompletableFuture<>();
        WebSocketServer.addCallback(packId, responseFuture);
        baseSendMessage(type.getValue(), body, packId);
        return responseFuture;
    }

    public void callBack(String msg,String packId){
        JSONObject body = new JSONObject();
        body.put("param",msg);
        sendMessage(BotClientSendEvent.CALLBACK_FUNC,body,packId);
    }

    public void callBack(JSONObject msg,String packId){
        JSONObject body = new JSONObject();
        body.put("param",msg);
        sendMessage(BotClientSendEvent.CALLBACK_FUNC,body,packId);
    }

    /**
     * 关闭连接
     */
    public void shutdown(int code, String reason) {
        JSONObject body = new JSONObject();
        body.put("msg", reason);
        sendMessage(BotClientSendEvent.SHUTDOWN, body);
        close(code, reason);
    }
}
