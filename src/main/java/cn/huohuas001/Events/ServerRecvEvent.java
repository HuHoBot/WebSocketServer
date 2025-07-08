package cn.huohuas001.Events;

public enum ServerRecvEvent {
    sendMsg,
    heart,
    success,
    error,
    shakeHand,
    queryWl,
    queryOnline,
    bindConfirm,
    chat,
    unknown;

    /**
     * 根据字符串值查找对应的枚举项（不区分大小写）
     *
     * @param value 要查找的值
     * @return 对应的枚举项，找不到则返回 unknown
     */
    public static ServerRecvEvent find(String value) {
        if (value == null || value.isEmpty()) {
            return unknown;
        }

        // 遍历所有枚举值，不区分大小写比较
        for (ServerRecvEvent event : values()) {
            if (event.name().equalsIgnoreCase(value)) {
                return event;
            }
        }

        return unknown;
    }
}
