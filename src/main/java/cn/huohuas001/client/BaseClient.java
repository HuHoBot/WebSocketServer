package cn.huohuas001.client;

import cn.huohuas001.tools.PackId;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class BaseClient {
    WebSocketSession session;
    String serverId = null;
    String hashKey = null;
    private volatile long lastHeartbeatTime = System.currentTimeMillis();

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
        } catch (IOException e) {
            log.error("[Websocket]  发送消息失败", e);
            return false;
        }
    }

    /**
     * 更新最后一次心跳的时间
     */
    public void updateLastHeartbeatTime() {
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    /**
     * 判断是否超时
     *
     * @param timeoutMillis 超时时间
     * @return 是否超时 boolean
     */
    public boolean isTimeout(long timeoutMillis) {
        return (System.currentTimeMillis() - lastHeartbeatTime) > timeoutMillis;
    }

    /**
     * 关闭连接
     */
    public void close(int code, String reason) {
        try {
            CloseStatus status = new CloseStatus(code, reason);
            if (session != null) {
                if (session.isOpen()) {
                    session.close(status);
                    log.info("[Websocket]  服务端主动关闭连接, ServerId: {}", getServerId());
                } else {
                    log.info("[Websocket]  服务端主动关闭连接时发现客户端已离线, ServerId: {}", getServerId());
                }
            }
        } catch (IOException e) {
            log.error("[Websocket]  服务端主动关闭连接时发生错误:", e);
        }
    }

    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
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

    public boolean isConnecting() {
        return session.isOpen();
    }
}
