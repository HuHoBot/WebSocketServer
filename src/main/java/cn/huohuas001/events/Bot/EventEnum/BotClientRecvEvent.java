package cn.huohuas001.events.Bot.EventEnum;

import java.util.HashMap;
import java.util.Map;

public enum BotClientRecvEvent{
    SEND_MSG_BY_SERVER_ID("BotClient.sendMsgByServerId"),
    QUERY_CLIENT_LIST("BotClient.queryClientList"),
    QUERY_STATUS("BotClient.queryStatus"),
    SHAKE("BotClient.shakeHand"),
    HEART("BotClient.heart");

    private final String value;

    BotClientRecvEvent(String value) {
        this.value  = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, BotClientRecvEvent> lookup = new HashMap<>();

    static {
        for (BotClientRecvEvent event : values()) {
            lookup.put(event.getValue(),  event);
        }
    }

    public static BotClientRecvEvent findByValue(String value) {
        return lookup.get(value);
    }
}
