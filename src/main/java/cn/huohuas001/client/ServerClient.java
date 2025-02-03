package cn.huohuas001.client;

import cn.huohuas001.Events.ServerSendEvent;
import cn.huohuas001.tools.PackId;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class ServerClient extends BaseClient{
    private String platform = "Unkown";
    private String name = "Server";
    private String version = "0.0.0";
    private WebSocketSession session;

    public ServerClient(WebSocketSession session) {
        super(session);
    }

    public boolean sendMessage(ServerSendEvent type, JSONObject body){
        String packId = PackId.getPackID();
        return sendMessage(type, body, packId);
    }

    public boolean sendMessage(ServerSendEvent type, JSONObject body, String packId){
        return baseSendMessage(type.toString(), body, packId);
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 关闭连接
     */
    public void shutdown(int code,String reason) {
        try {
            JSONObject body = new JSONObject();
            body.put("msg",reason);
            sendMessage(ServerSendEvent.shutdown, body);
            CloseStatus status = new CloseStatus(code, reason);
            if(session != null){
                if(session.isOpen()){
                    session.close(status);
                    log.info("[Websocket]  服务端主动关闭连接, ServerId: {}", getServerId());
                }else{
                    log.info("[Websocket]  服务端主动关闭连接时发现客户端已离线, ServerId: {}", getServerId());
                }
            }
        } catch (Exception e) {
            log.error("[Websocket]  服务端主动关闭连接时发生错误:", e);
        }
    }

}
