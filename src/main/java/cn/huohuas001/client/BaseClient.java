package cn.huohuas001.client;

import cn.huohuas001.tools.ClientManager;
import cn.huohuas001.tools.Enums.ClientType;
import cn.huohuas001.tools.PackId;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BaseClient {
    WebSocketSession session;

    private final ClientType clientType;
    @Getter
    @Setter
    String serverId = null;

    private volatile long lastHeartbeatTime = System.currentTimeMillis();
    private final BlockingQueue<Runnable> messageQueue = new LinkedBlockingQueue<>(200); // 单客户端队列容量
    @Getter
    @Setter
    String hashKey = null;


    public BaseClient(WebSocketSession session, ClientType clientType) {
        this.session = session;
        this.clientType = clientType;
    }

    /**
     * 消息处理循环
     */
    private void messageProcessingLoop() {
        while (session.isOpen()) {
            try {
                Runnable task = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Message processing error", e);
            }
        }
        log.debug("Message loop exited for session: {}", session.getId());
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
    public boolean baseSendMessage(String type, JSONObject body, String packId) {
        Object sendLock = ClientManager.getInstance().getSendLock();
        synchronized (sendLock) {
            try {
                if (!session.isOpen() || session == null) return false; // 再次检查

                JSONObject pack = getPack(type, body, packId);
                session.sendMessage(new TextMessage(pack.toJSONString()));
                return true;
            } catch (IllegalStateException e) {
                // 处理 TEXT_PARTIAL_WRITING 或其他状态异常
                log.error("[Websocket] 发送消息时状态异常: {}", e.getMessage());
                close(1000, "Connection state invalid"); // 主动关闭会话
                return false;
            } catch (IOException e) {
                // 处理 Broken pipe 或其他 IO 错误
                log.error("[Websocket] 发送消息失败: {}", e.getMessage());
                return false;
            }
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
            messageQueue.clear(); // 清空未发送消息
            CloseStatus status = new CloseStatus(code, reason);
            if (session != null) {
                if (session.isOpen()) {
                    session.close(status);
                    if (ClientManager.getInstance().isRegisteredServer(getServerId())) {
                        log.info("[Websocket]  服务端主动关闭连接, ServerId: {}", getServerId());
                    }
                } else {
                    if (ClientManager.getInstance().isRegisteredServer(getServerId())) {
                        log.info("[Websocket]  服务端主动关闭连接时发现客户端已离线, ServerId: {}", getServerId());
                    }
                }
            }
        } catch (IOException e) {
            log.error("[Websocket]  服务端主动关闭连接时发生错误:", e);
        }
    }

    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
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
