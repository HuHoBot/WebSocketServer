package cn.huohuas001.Events;

import java.util.HashMap;
import java.util.Map;

public enum BotClientSendEvent {
    CALLBACK("BotClient.callback"),
    QUERY_BIND_SERVER_BY_ID("BotClient.queryBindServerById"),
    BIND_SERVER("BotClient.bindServer"),
    ADD_ADMIN("BotClient.addAdmin"),
    CALLBACK_FUNC("BotClient.callbackFunc"),
    SHOOK_HAND("shaked"),
    HEART("heart"),
    CHAT("BotClient.chat"),
    GET_CONFIRM_DATA("BotClient.getConfirmData");



    private final String value;

    BotClientSendEvent(String value) {
        this.value  = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, BotClientSendEvent> lookup = new HashMap<>();

    static {
        for (BotClientSendEvent event : values()) {
            lookup.put(event.getValue(),  event);
        }
    }

    public static BotClientSendEvent findByValue(String value) {
        return lookup.get(value);
    }
}
