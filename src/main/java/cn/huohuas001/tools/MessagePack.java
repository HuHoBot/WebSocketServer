package cn.huohuas001.tools;

import cn.huohuas001.tools.Enums.MessageTarget;
import com.alibaba.fastjson2.JSONObject;

public class MessagePack {
    public MessageTarget msgTarget;
    public String packId;
    public String msgType;
    public JSONObject body;

    public MessagePack(String packId, String msgType, JSONObject body) {
        this.packId = packId;
        this.msgType = msgType;
        this.body = body;
    }
}
