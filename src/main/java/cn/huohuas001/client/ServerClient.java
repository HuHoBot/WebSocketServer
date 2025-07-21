package cn.huohuas001.client;

import cn.huohuas001.events.Server.EventEnum.ServerSendEvent;
import cn.huohuas001.tools.Enums.ClientType;
import cn.huohuas001.tools.PackId;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class ServerClient extends BaseClient{
    @Getter
    @Setter
    private String platform = "Unkown";
    @Getter
    @Setter
    private String name = "Server";
    @Setter
    @Getter
    private String version = "0.0.0";
    private WebSocketSession session;

    public ServerClient(WebSocketSession session) {
        super(session, ClientType.Server);
    }

    public boolean sendMessage(ServerSendEvent type, JSONObject body){
        String packId = PackId.getPackID();
        return sendMessage(type, body, packId);
    }

    public boolean sendMessage(ServerSendEvent type, JSONObject body, String packId){
        return baseSendMessage(type.toString(), body, packId);
    }

    /**
     * 关闭连接
     */
    public void shutdown(int code,String reason) {
        JSONObject body = new JSONObject();
        body.put("msg", reason);
        sendMessage(ServerSendEvent.shutdown, body);
        close(code, reason);
    }

}
