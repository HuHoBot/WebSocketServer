package cn.huohuas001.tools;

import cn.huohuas001.client.ServerClient;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;

public class ServerMsgPack {
    @Getter
    private final JSONObject body;
    @Getter
    private final String packId;
    private final Enum<?> type;
    private final ServerClient client;

    public ServerMsgPack(Enum<?> type, JSONObject body, String packId, ServerClient client) {
        this.body = body;
        this.type = type;
        this.client = client;
        this.packId = packId;
    }

    public Enum<?> getType() {
        return type;
    }

    public ServerClient getServerClient() {
        return client;
    }
}
