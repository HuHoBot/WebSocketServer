package cn.huohuas001.tools;

import cn.huohuas001.Events.BotClientRecvEvent;
import com.alibaba.fastjson2.JSONObject;

public class BotMsgPack {
    private final JSONObject body;
    private final String packId;
    private final BotClientRecvEvent type;

    public BotMsgPack(BotClientRecvEvent type, JSONObject body, String packId) {
        this.body = body;
        this.type = type;
        this.packId = packId;
    }

    public JSONObject getBody() {
        return body;
    }

    public String getPackId() {
        return packId;
    }

    public BotClientRecvEvent getType() {
        return type;
    }
}
