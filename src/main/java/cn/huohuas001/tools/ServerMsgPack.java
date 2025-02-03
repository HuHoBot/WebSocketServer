package cn.huohuas001.tools;

import cn.huohuas001.Events.ServerRecvEvent;
import cn.huohuas001.Events.ServerSendEvent;
import cn.huohuas001.client.ServerClient;
import com.alibaba.fastjson2.JSONObject;

public class ServerMsgPack {
    private JSONObject body;
    private String packId;
    private Enum<?> type;
    private ServerClient client;

    public ServerMsgPack(Enum<?> type, JSONObject body, String packId, ServerClient client) {
        this.body = body;
        this.type = type;
        this.client = client;
        this.packId = packId;
    }

    public JSONObject getBody() {
        return body;
    }

    public String getPackId() {
        return packId;
    }

    public Enum<?> getType() {
        return type;
    }

    public ServerClient getServerClient() {
        return client;
    }
}
