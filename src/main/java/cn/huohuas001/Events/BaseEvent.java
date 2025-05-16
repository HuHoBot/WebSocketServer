package cn.huohuas001.Events;

import cn.huohuas001.client.ServerClient;
import cn.huohuas001.tools.ClientManager;
import cn.huohuas001.tools.ServerMsgPack;
import com.alibaba.fastjson2.JSONObject;

public abstract class BaseEvent {
    String packId;
    JSONObject body;
    ServerClient client;
    ClientManager clientManager;

    public BaseEvent(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public boolean EventCall(ServerMsgPack serverMsgPack) {
        setData(serverMsgPack);
        return run();
    }

    public void setData(ServerMsgPack serverMsgPack) {
        this.packId = serverMsgPack.getPackId();
        this.body = serverMsgPack.getBody();
        this.client = serverMsgPack.getServerClient();
    }

     public abstract boolean run();
}
