package cn.huohuas001.events.Bot;

import cn.huohuas001.client.BotClient;
import cn.huohuas001.tools.BotMsgPack;
import cn.huohuas001.tools.ClientManager;
import com.alibaba.fastjson2.JSONObject;

public abstract class BotEvent {
    String packId;
    JSONObject body;
    BotClient botClient;
    ClientManager clientManager;

    public BotEvent(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    public boolean EventCall(BotMsgPack msgPack) {
        setData(msgPack);
        return run();
    }

    public void setData(BotMsgPack msgPack) {
        this.packId = msgPack.getPackId();
        this.body = msgPack.getBody();
        this.botClient = clientManager.getBotClient();
    }

    public abstract boolean run();
}
