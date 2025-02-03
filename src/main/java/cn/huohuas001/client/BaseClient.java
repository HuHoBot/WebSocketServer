package cn.huohuas001.client;

import cn.huohuas001.tools.PackId;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class BaseClient {
    WebSocketSession session;
    String serverId = null;
    String hashKey = null;

    public BaseClient(WebSocketSession session) {
        this.session = session;
    }

    private JSONObject getPack(String type, JSONObject body, String packId){
        JSONObject pack = new JSONObject();
        JSONObject header = new JSONObject();
        header.put("type", type);
        header.put("id", packId);
        pack.put("header", header);
        pack.put("body", body);
        return pack;
    }

    /**
     * 发送消息(无PackId)
     * @param type 事件类型
     * @param body 消息包
     */
    public void baseSendMessage(String type,JSONObject body){
        String packId = PackId.getPackID();
        baseSendMessage(type, body, packId);
    }

    /**
     * 发送消息
     * @param type 事件类型
     * @param body 消息包
     */
    public boolean baseSendMessage(String type,JSONObject body, String packId) {
        try {
            JSONObject pack = getPack(type, body, packId);
            if(session.isOpen()){
                session.sendMessage(new TextMessage(pack.toJSONString()));
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[Websocket]  发送消息失败", e);
            return false;
        }
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getServerId() {
        return serverId;
    }

    public String getHashKey() {
        return hashKey;
    }

    public boolean equals(Object obj){
        if(obj instanceof BaseClient){
            return session.equals(((BaseClient)obj).session);
        }else if(obj instanceof WebSocketSession){
            return session.equals(obj);
        }
        return false;
    }
}
