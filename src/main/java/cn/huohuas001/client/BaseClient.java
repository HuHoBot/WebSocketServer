package cn.huohuas001.client;

import cn.huohuas001.tools.ClientManager;
import cn.huohuas001.tools.PackId;
import cn.huohuas001.tools.WsThreadPool;
import com.alibaba.fastjson2.JSONObject;
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
    String serverId = null;
    String hashKey = null;
    private volatile long lastHeartbeatTime = System.currentTimeMillis();
    private final BlockingQueue<Runnable> messageQueue = new LinkedBlockingQueue<>(200); // 单客户端队列容量



    public BaseClient(WebSocketSession session) {
        this.session = session;
        // 注册到全局线程池
        WsThreadPool.submitTask(this::messageProcessingLoop);
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
    /*public boolean baseSendMessage(String type, JSONObject body, String packId) {
        if (!session.isOpen()) return false;

        return messageQueue.offer(() -> {
            try {
                JSONObject pack = new JSONObject();
                JSONObject header = new JSONObject();
                header.put("type", type);
                header.put("id", packId);
                pack.put("header", header);
                pack.put("body", body);

                synchronized (session) { // 针对同一session的发送同步
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(pack.toJSONString()));
                    }
                }
            } catch (IOException e) {
                log.error("Send message failed", e);
            }
        });
    }*/

    /**
     * 发送消息
     * @param type 事件类型
     * @param body 消息包
     */
    public boolean baseSendMessage(String type,JSONObject body, String packId) {
        Object sendLock = ClientManager.getInstance().getSendLock();
        synchronized (sendLock) {
            try {
                JSONObject pack = getPack(type, body, packId);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(pack.toJSONString()));
                    return true;
                }
                return false;
            } catch (IllegalStateException e) {
                log.error("[Websocket] 发送消息时状态异常: {}", e.getMessage());
                return false;
            } catch (IOException e) {
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
